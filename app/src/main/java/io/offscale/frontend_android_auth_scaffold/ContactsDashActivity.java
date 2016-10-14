package io.offscale.frontend_android_auth_scaffold;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.lang.ref.WeakReference;

import io.offscale.frontend_android_auth_scaffold.api.contact.Contact;
import io.offscale.frontend_android_auth_scaffold.api.contact.ContactAdapter;
import io.offscale.frontend_android_auth_scaffold.api.contact.ContactsClient;
import io.offscale.frontend_android_auth_scaffold.api.contact.ListContacts;
import io.offscale.frontend_android_auth_scaffold.utils.ActivityUtilsSingleton;
import io.offscale.frontend_android_auth_scaffold.utils.ErrorOrEntity;
import io.offscale.frontend_android_auth_scaffold.utils.CommonErrorHandlerRedirector;
import io.offscale.frontend_android_auth_scaffold.utils.PrefSingleton;
import io.offscale.frontend_android_auth_scaffold.utils.ProgressHandler;

import static io.offscale.frontend_android_auth_scaffold.utils.Formatters.ExceptionFormatter;

public final class ContactsDashActivity extends AppCompatActivity {
    private final PrefSingleton mSharedPrefs = PrefSingleton.getInstance();
    private final ActivityUtilsSingleton mUtils = ActivityUtilsSingleton.getInstance();
    private static ContactsClient mContactsClient;
    private LoadContactsTask mLoadContactsTask = null;
    private View mProgressView;
    private TextView mInfoMsg;
    private ViewSwitcher mContentViewSwitcher;
    private ListView mContentListView;
    private CommonErrorHandlerRedirector mCommonErrorHandlerRedirector;
    private ProgressHandler mProgressHandler;
    //private View mContactItemView;
    //private TextView getError()View;

    private void showEmptyContactsView() {
        mContentViewSwitcher.setDisplayedChild(0);
    }

    private void showListOfContactsView() {
        mContentViewSwitcher.setDisplayedChild(1);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_dash);

        mSharedPrefs.Init(getApplicationContext());
        mUtils.Init(savedInstanceState, getIntent(), mSharedPrefs);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.contacts_dash_toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton send_btn = (FloatingActionButton) findViewById(R.id.contacts_dash_send_btn);
        if (send_btn != null) send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Toast toast = Toast.makeText(getApplicationContext(), "TODO", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        final FloatingActionButton add_btn = (FloatingActionButton) findViewById(R.id.contacts_dash_add_btn);
        if (add_btn != null) add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                startActivity(new Intent(ContactsDashActivity.this, CreateContactActivity.class));
            }
        });

        final String accessToken = mUtils.getFromLocalOrCache("access_token");
        final String email = mUtils.getFromLocalOrCache("email");

        if (accessToken == null || email == null) {
            System.err.println("access_token or email is null at ContactsDashActivity. " +
                    "Redirecting...");
            startActivity(new Intent(this, SignUpInActivity.class));
            return;
        }

        mContactsClient = new ContactsClient(this, accessToken);

        mProgressView = findViewById(R.id.contacts_dash_progress);

        mInfoMsg = (TextView) findViewById(R.id.contacts_dash_info_msg);
        mContentViewSwitcher = (ViewSwitcher) findViewById(R.id.contacts_dash_content_view_switcher);
        mContentListView = (ListView) findViewById(R.id.contacts_dash_list);
        mContentListView.setOnItemClickListener(new ItemClick());
        mCommonErrorHandlerRedirector = new CommonErrorHandlerRedirector(this, mSharedPrefs);
        mProgressHandler = new ProgressHandler(mProgressView, mContentViewSwitcher,
                getResources().getInteger(android.R.integer.config_shortAnimTime));
        //getError()View = (TextView) findViewById(R.id.errors);

        loadContacts(Contact.fromString(mUtils.getFromLocalOrCache("new_contact")));
    }

    final class ItemClick implements AdapterView.OnItemClickListener {
        public final void onItemClick(final AdapterView<?> parent, final View view,
                                      final int position, final long id) {
            final Intent intent = new Intent(getApplicationContext(), ContactActivity.class);
            intent.putExtra("current_contact",
                    mContentListView.getItemAtPosition(position).toString());
            startActivity(intent);
        }
    }

    @Override
    public final void onBackPressed() {
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressHandler.showProgress(show);
    }

    private void loadContacts(final Contact newContact) {
        if (mLoadContactsTask != null)
            return;

        showProgress(true);
        mLoadContactsTask = new LoadContactsTask(this, newContact);
        mLoadContactsTask.execute((Void) null);
    }

    /**
     * Represents an asynchronous task
     */
    public static final class LoadContactsTask extends AsyncTask<Void, Void,
            ErrorOrEntity<ListContacts>> {

        final Contact mNewContact;
        private final WeakReference<ContactsDashActivity> mWeakActivity;

        LoadContactsTask(final ContactsDashActivity activity, final Contact newContact) {
            mNewContact = newContact;
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        protected final ErrorOrEntity<ListContacts> doInBackground(final Void... params) {
            return mContactsClient.getSync();
        }

        @Override
        protected final void onPostExecute(final ErrorOrEntity<ListContacts> err_res) {
            final ContactsDashActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mLoadContactsTask = null;
            activity.showProgress(false);

            if (err_res.success()) {
                final ContactAdapter contactAdapter = new ContactAdapter(
                        activity.getApplicationContext(), err_res.getEntity().getContacts());
                if (contactAdapter.getCount() > 0) {
                    activity.mContentListView.setAdapter(contactAdapter);
                    activity.showListOfContactsView();
                } else {
                    activity.mInfoMsg.setText(activity.getString(R.string.empty_contacts));
                    activity.showEmptyContactsView();
                }
            } else {
                activity.mCommonErrorHandlerRedirector.process(err_res);
                activity.showEmptyContactsView();
                if (err_res.getErrorResponse() == null) {
                    err_res.getException().printStackTrace(System.err);
                    activity.mInfoMsg.setTextColor(Color.RED);
                    activity.mInfoMsg.setText(ExceptionFormatter(err_res.getException()));
                } else activity.mInfoMsg.setText(err_res.getCode() == 404 ?
                        activity.getString(R.string.empty_contacts) :
                        err_res.getErrorResponse().toString());
            }
        }

        @Override
        protected final void onCancelled() {
            final ContactsDashActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mLoadContactsTask = null;
            activity.showProgress(false);
        }
    }
}
