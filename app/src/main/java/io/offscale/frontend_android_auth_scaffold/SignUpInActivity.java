package io.offscale.frontend_android_auth_scaffold;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.offscale.frontend_android_auth_scaffold.api.AuthClient;
import io.offscale.frontend_android_auth_scaffold.api.ServerClient;
import io.offscale.frontend_android_auth_scaffold.utils.ActivityUtilsSingleton;
import io.offscale.frontend_android_auth_scaffold.utils.ApiClient;
import io.offscale.frontend_android_auth_scaffold.utils.ErrResResponse;
import io.offscale.frontend_android_auth_scaffold.utils.PrefSingleton;
import io.offscale.frontend_android_auth_scaffold.utils.ProgressHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.Manifest.permission.READ_CONTACTS;
import static io.offscale.frontend_android_auth_scaffold.utils.Formatters.ExceptionFormatter;

/**
 * A login screen that offers login via email/password.
 */
public final class SignUpInActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserSignInUpTask mSignInUpTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private TextInputEditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mServerStatusView;
    private TextView mErrorView;
    private final PrefSingleton mSharedPrefs = PrefSingleton.getInstance();
    private final ActivityUtilsSingleton mUtils = ActivityUtilsSingleton.getInstance();
    private ProgressHandler mProgressHandler;
    private static AuthClient mAuthClient;
    private static ServerClient mServerClient;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_in);

        mSharedPrefs.Init(getApplicationContext());
        mUtils.Init(savedInstanceState, getIntent(), mSharedPrefs);

        mServerStatusView = (TextView) findViewById(R.id.sign_server_status);
        mErrorView = (TextView) findViewById(R.id.sign_errors);

        final String error = mUtils.getFromLocalOrCache("error");
        if (error != null) {
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            mErrorView.setError("");
            mErrorView.setText(error);
        } else {
            final String access_token = mUtils.getFromLocalOrCache("access_token");
            if (access_token != null) {
                final Intent intent = new Intent(this, ContactsDashActivity.class);
                intent.putExtra("access_token", access_token);
                finish();
                startActivity(intent);
                return;
            }
        }

        mAuthClient = new AuthClient(this);
        mServerClient = new ServerClient(this);

        ApiClient.async(mAuthClient.getClient(), mServerClient.get_version(), new Callback() {
            @Override
            public final void onFailure(final Call call, final IOException e) {
                e.printStackTrace(System.err);
                runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {
                        mErrorView.setError("");
                        mErrorView.setText(ExceptionFormatter(e));
                    }
                });
            }

            @Override
            public final void onResponse(final Call call, final Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {
                        try {
                            if (!response.isSuccessful()) {
                                mErrorView.setError("");
                                mErrorView.setError(response.body().string());
                            } else mServerStatusView.setText(response.body().string());
                        } catch (final IOException | IllegalStateException e) {
                            e.printStackTrace(System.err);
                            mErrorView.setError("");
                            mErrorView.setError(ExceptionFormatter(e));
                        } finally {
                            response.close();
                        }
                    }
                });
            }
        });


        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.sign_email);
        populateAutoComplete();

        mPasswordView = (TextInputEditText) findViewById(R.id.sign_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView textView,
                                          final int id, final KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        final Button emailSignInButton = (Button) findViewById(R.id.sign_up_in_button);
        if (emailSignInButton != null)
            emailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View view) {
                    attemptLogin();
                }
            });

        mLoginFormView = findViewById(R.id.sign_form);
        mProgressView = findViewById(R.id.sign_progress);

        mProgressHandler = new ProgressHandler(mProgressView, mLoginFormView,
                getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public final void onRequestPermissionsResult(final int requestCode,
                                                 final @NonNull String[] permissions,
                                                 final @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mSignInUpTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an mError; don't attempt login and focus the first
            // form field with an mError.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mSignInUpTask = new UserSignInUpTask(this, email, password);
            mSignInUpTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(final String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(final String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
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
                new ArrayAdapter<>(SignUpInActivity.this,
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public static final class UserSignInUpTask extends AsyncTask<Void, Void,
            ErrResResponse<Exception, ResponseBody>> {

        private final WeakReference<SignUpInActivity> mWeakActivity;
        private final String mEmail;
        private final String mPassword;

        UserSignInUpTask(final SignUpInActivity activity, final String email, final String password) {
            mWeakActivity = new WeakReference<>(activity);
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected final ErrResResponse<Exception, ResponseBody> doInBackground(final Void... params) {
            try {
                final ErrResResponse<Exception, ResponseBody> err_res = ApiClient.sync(
                        mAuthClient.getClient(), mAuthClient.register(mEmail, mPassword));
                if (err_res.failure() && err_res.getResponse().body().string().contains(
                        "duplicate key value violates unique constraint"))
                    return ApiClient.sync(mAuthClient.getClient(),
                            mAuthClient.login(mEmail, mPassword));
                return err_res;
            } catch (IOException | IllegalStateException e) {
                return new ErrResResponse<>((Exception) e, null, null);
            }
        }

        @Override
        protected final void onPostExecute(final ErrResResponse<Exception, ResponseBody> err_res) {
            final SignUpInActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mSignInUpTask = null;
            activity.showProgress(false);
            if (err_res.success()) {
                final String access_token = err_res.getResponse().header("X-Access-Token");
                activity.mSharedPrefs.putString("access_token", access_token);
                activity.mSharedPrefs.putString("email", mEmail);
                activity.finish();
                final Intent intent = new Intent(activity, ContactsDashActivity.class);
                intent.putExtra("access_token", access_token);
                intent.putExtra("email", mEmail);
                activity.startActivity(intent);
            } else {
                activity.mErrorView.setError("");
                if (err_res.getResponse() != null)
                    try {
                        activity.mErrorView.setText(err_res.getResponse().body().string());
                    } catch (IOException | IllegalStateException e) {
                        e.printStackTrace(System.err);
                    }
                else if (err_res.getError() != null) {
                    err_res.getError().printStackTrace(System.err);
                    activity.mErrorView.setText(ExceptionFormatter(err_res.getError()));
                }
            }
        }

        @Override
        protected final void onCancelled() {
            final SignUpInActivity activity = mWeakActivity.get();
            if (activity == null || activity.isFinishing()) return;

            activity.mSignInUpTask = null;
            activity.showProgress(false);
        }
    }
}
