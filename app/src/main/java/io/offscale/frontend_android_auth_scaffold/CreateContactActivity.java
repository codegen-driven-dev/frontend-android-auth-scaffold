package io.offscale.frontend_android_auth_scaffold;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.offscale.frontend_android_auth_scaffold.api.contact.Contact;
import io.offscale.frontend_android_auth_scaffold.api.contact.ContactsClient;
import io.offscale.frontend_android_auth_scaffold.utils.ActivityUtilsSingleton;
import io.offscale.frontend_android_auth_scaffold.utils.ProgressHandler;
import io.offscale.frontend_android_auth_scaffold.utils.CommonErrorHandlerRedirector;
import io.offscale.frontend_android_auth_scaffold.utils.ErrorOrEntity;
import io.offscale.frontend_android_auth_scaffold.utils.PrefSingleton;

import static android.Manifest.permission.READ_CONTACTS;
import static io.offscale.frontend_android_auth_scaffold.utils.Formatters.ExceptionFormatter;

/**
 * A login screen that offers login via email/password.
 */
public final class CreateContactActivity
        extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private CreateContactTask mCreateContactTask = null;

    // UI references.
    private TextView mNameView;
    private AutoCompleteTextView mEmailView;
    private View mProgressView;
    private View mCreateContactFormView;
    private TextView mErrorView;
    private CommonErrorHandlerRedirector mCommonErrorHandlerRedirector;
    private ProgressHandler mProgressHandler;
    private final PrefSingleton mSharedPrefs = PrefSingleton.getInstance();
    private final ActivityUtilsSingleton mUtils = ActivityUtilsSingleton.getInstance();
    private static ContactsClient mContactsClient;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);

        mSharedPrefs.Init(getApplicationContext());
        mUtils.Init(savedInstanceState, getIntent(), mSharedPrefs);

        final String accessToken = mUtils.getFromLocalOrCache("access_token");
        if (accessToken == null) {
            startActivity(new Intent(this, SignUpInActivity.class));
            return;
        }

        // Set up the form.
        mErrorView = (TextView) findViewById(R.id.create_contact_errors);
        mNameView = (TextView) findViewById(R.id.create_contact_name);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.create_contact_email);
        populateAutoComplete();

        final Button mEmailSignInButton = (Button) findViewById(R.id.create_contact_button);
        if (mEmailSignInButton != null)
            mEmailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View view) {
                    attemptCreate();
                }
            });

        mCreateContactFormView = findViewById(R.id.create_contact_form);
        mProgressView = findViewById(R.id.create_contact_progress);
        mProgressHandler = new ProgressHandler(mProgressView, mCreateContactFormView,
                getResources().getInteger(android.R.integer.config_shortAnimTime));

        mContactsClient = new ContactsClient(this, accessToken);
        mCommonErrorHandlerRedirector = new CommonErrorHandlerRedirector(this, mSharedPrefs);
    }

    @Override
    public final void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts())
            return;

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        else if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            return true;
        else if (shouldShowRequestPermissionRationale(READ_CONTACTS))
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(final View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        else
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public final void onRequestPermissionsResult(final int requestCode,
                                                 final @NonNull String[] permissions,
                                                 final @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            populateAutoComplete();
    }


    private void attemptCreate() {
        if (mCreateContactTask != null)
            return;

        // Reset errors.
        mNameView.setError(null);
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        final String name = mNameView.getText().toString();
        final String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel)
            focusView.requestFocus();
        else {
            showProgress(true);
            mCreateContactTask = new CreateContactTask(this, new Contact(name, email));
            mCreateContactTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressHandler.showProgress(show);
    }

    @Override
    public final Loader<Cursor> onCreateLoader(final int i, final Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public final void onLoadFinished(final Loader<Cursor> cursorLoader, final Cursor cursor) {
        final List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public final void onLoaderReset(final Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        final ArrayAdapter<String> adapter =
                new ArrayAdapter<>(CreateContactActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public static final class CreateContactTask extends AsyncTask<Void, Void,
            ErrorOrEntity<Contact>> {

        private final Contact mContact;
        private final WeakReference<CreateContactActivity> mWeakActivity;

        CreateContactTask(final CreateContactActivity activity, final Contact contact) {
            mContact = contact;
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        protected final ErrorOrEntity<Contact> doInBackground(final Void... params) {
            return mContactsClient.postSync(mContact);
        }

        @Override
        protected final void onPostExecute(final ErrorOrEntity<Contact> err_res) {
            final CreateContactActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mCreateContactTask = null;
            activity.showProgress(false);

            if (err_res.success()) {
                activity.finish();
                final Intent intent = new Intent(activity.getApplicationContext(),
                        ContactsDashActivity.class);
                intent.putExtra("new_contact", mContact.toString());
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
            final CreateContactActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mCreateContactTask = null;
            activity.showProgress(false);
        }
    }
}
