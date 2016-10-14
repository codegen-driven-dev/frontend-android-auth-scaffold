package io.offscale.frontend_android_auth_scaffold;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.lang.ref.WeakReference;
import java.util.Locale;

import io.offscale.frontend_android_auth_scaffold.api.contact.Contact;
import io.offscale.frontend_android_auth_scaffold.api.contact.ContactClient;
import io.offscale.frontend_android_auth_scaffold.utils.ActivityUtilsSingleton;
import io.offscale.frontend_android_auth_scaffold.utils.CommonErrorHandlerRedirector;
import io.offscale.frontend_android_auth_scaffold.utils.ErrorOrEntity;
import io.offscale.frontend_android_auth_scaffold.utils.PrefSingleton;
import io.offscale.frontend_android_auth_scaffold.utils.ProgressHandler;

import static io.offscale.frontend_android_auth_scaffold.utils.Formatters.ExceptionFormatter;

public final class ContactActivity extends AppCompatActivity {
    private final PrefSingleton mSharedPrefs = PrefSingleton.getInstance();
    private final ActivityUtilsSingleton mUtils = ActivityUtilsSingleton.getInstance();
    private View mProgressView;
    private ViewSwitcher mViewSwitcher;
    private TextView mReadContactName;
    private TextView mReadContactEmail;
    private TextInputEditText mEditContactName;
    private AutoCompleteTextView mEditContactEmail;
    private Button mEditContactButton;
    private TextView mErrorView;
    private CommonErrorHandlerRedirector mCommonErrorHandlerRedirector;
    private static ContactClient mContactClient;
    private ProgressHandler mProgressHandler;
    private UpdateContactTask mUpdateContactsTask = null;
    private DeleteContactTask mDeleteContactsTask = null;

    private void showReadView() {
        if (isUpdateView()) mViewSwitcher.setDisplayedChild(0);
    }

    private void showUpdateView() {
        if (!isUpdateView()) mViewSwitcher.setDisplayedChild(1);
    }

    private boolean isUpdateView() {
        return mViewSwitcher.getDisplayedChild() == 1;
    }

    @Override
    public final void onBackPressed() {
        if (isUpdateView()) {
            showReadView();
            return;
        }
        finish();
        super.onBackPressed();
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mSharedPrefs.Init(getApplicationContext());
        mUtils.Init(savedInstanceState, getIntent(), mSharedPrefs);

        final String accessToken = mUtils.getFromLocalOrCache("access_token");
        if (accessToken == null) {
            startActivity(new Intent(this, SignUpInActivity.class));
            return;
        }

        mContactClient = new ContactClient(this, accessToken);
        final Contact contact = Contact.fromString(mUtils.getFromLocalOrCache("current_contact"));
        mCommonErrorHandlerRedirector = new CommonErrorHandlerRedirector(this, mSharedPrefs);

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.activity_contact_view_switcher);
        showReadView();

        mReadContactName = (TextView) findViewById(R.id.activity_contact_item_name);
        mReadContactEmail = (TextView) findViewById(R.id.activity_contact_item_email);

        mEditContactName = (TextInputEditText) findViewById(R.id.activity_contact_update_name);
        mEditContactEmail = (AutoCompleteTextView) findViewById(R.id.activity_contact_update_email);
        mEditContactButton = (Button) findViewById(R.id.activity_contact_update_button);
        if (mEditContactButton != null)
            mEditContactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(final View view) {
                    showProgress(true);
                    final Contact newContact = (Contact) contact.clone();
                    newContact.setName(mEditContactName.getText().toString());
                    newContact.setEmail(mEditContactEmail.getText().toString());
                    mUpdateContactsTask = new UpdateContactTask(ContactActivity.this, contact, newContact);
                    mUpdateContactsTask.execute((Void) null);
                }
            });

        final Button contactEditBtn = (Button) findViewById(R.id.activity_contact_item_owner_edit_btn);
        if (contactEditBtn != null) contactEditBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        showUpdateView();
                    }
                });

        final Button contactDelBtn = (Button) findViewById(R.id.activity_contact_item_owner_del_btn);
        if (contactDelBtn != null) contactDelBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        mDeleteContactsTask = new DeleteContactTask(ContactActivity.this, contact);
                        mDeleteContactsTask.execute((Void) null);
                    }
                });

        mProgressView = findViewById(R.id.activity_contact_update_progress);
        mErrorView = (TextView) findViewById(R.id.activity_contact_update_errors);

        mProgressHandler = new ProgressHandler(mProgressView, mViewSwitcher,
                getResources().getInteger(android.R.integer.config_shortAnimTime));

        setContactInView(contact);
        setEditContactInView(contact);
    }

    private void setContactInView(@NonNull final Contact contact) {
        mReadContactName.setText(contact.getName());
        mReadContactEmail.setText(contact.getEmail());
    }

    private void setEditContactInView(@NonNull final Contact contact) {
        mEditContactName.setText(contact.getName());
        mEditContactEmail.setText(contact.getEmail());
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressHandler.showProgress(show);
    }

    public static final class UpdateContactTask extends AsyncTask<Void, Void,
            ErrorOrEntity<Contact>> {

        private final Contact mPrevContact, mNewContact;
        private final WeakReference<ContactActivity> mWeakActivity;

        UpdateContactTask(final ContactActivity activity, final Contact contact,
                          final Contact newContact) {
            mPrevContact = contact;
            mNewContact = newContact;
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        protected final ErrorOrEntity<Contact> doInBackground(final Void... params) {
            return mContactClient.putSync(mPrevContact, mNewContact);
        }

        @Override
        protected final void onPostExecute(final ErrorOrEntity<Contact> err_res) {
            final ContactActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mUpdateContactsTask = null;
            activity.showProgress(false);

            if (err_res.success()) {
                activity.finish();
                final Intent intent = new Intent(activity.getApplicationContext(),
                        ContactsDashActivity.class);
                intent.putExtra("new_contact", err_res.getEntity().toString());
                activity.startActivity(intent);
            } else {
                activity.mCommonErrorHandlerRedirector.process(err_res);
                if (err_res.getErrorResponse() == null) {
                    err_res.getException().printStackTrace(System.err);
                    activity.mErrorView.setText(ExceptionFormatter(err_res.getException()));
                } else activity.mErrorView.setText(String.format(Locale.getDefault(),
                        "[%d] %s", err_res.getCode(), err_res.getErrorResponse().toString()));
            }
        }

        @Override
        protected final void onCancelled() {
            final ContactActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mUpdateContactsTask = null;
            activity.showProgress(false);
        }
    }

    public static final class DeleteContactTask extends AsyncTask<Void, Void,
            ErrorOrEntity<Contact>> {

        private final Contact mContact;
        private final WeakReference<ContactActivity> mWeakActivity;

        DeleteContactTask(final ContactActivity activity, final Contact contact) {
            mContact = contact;
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        protected final ErrorOrEntity<Contact> doInBackground(final Void... params) {
            return mContactClient.delSync(mContact);
        }

        @Override
        protected final void onPostExecute(final ErrorOrEntity<Contact> err_res) {
            final ContactActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mDeleteContactsTask = null;
            activity.showProgress(false);

            if (err_res.success()) {
                activity.finish();
                activity.startActivity(new Intent(activity.getApplicationContext(),
                        ContactsDashActivity.class));
            } else {
                activity.mCommonErrorHandlerRedirector.process(err_res);
                if (err_res.getErrorResponse() == null) {
                    err_res.getException().printStackTrace(System.err);
                    activity.mErrorView.setText(ExceptionFormatter(err_res.getException()));
                } else activity.mErrorView.setText(String.format(Locale.getDefault(),
                        "[%d] %s", err_res.getCode(), err_res.getErrorResponse().toString()));
            }
        }

        @Override
        protected final void onCancelled() {
            final ContactActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mUpdateContactsTask = null;
            activity.showProgress(false);
        }
    }
}
