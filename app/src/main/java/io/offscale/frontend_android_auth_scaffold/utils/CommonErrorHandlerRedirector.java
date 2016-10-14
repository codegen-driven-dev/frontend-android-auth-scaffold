package io.offscale.frontend_android_auth_scaffold.utils;

import android.content.Context;
import android.content.Intent;

import io.offscale.frontend_android_auth_scaffold.SignUpInActivity;


public class CommonErrorHandlerRedirector {
    private final Context mContext;
    private final PrefSingleton mSharedPrefs;

    public CommonErrorHandlerRedirector(final Context context, final PrefSingleton sharedPrefs) {
        mContext = context;
        mSharedPrefs = sharedPrefs;
    }

    public void process(final ErrorOrEntity<?> err_res) {
        switch (err_res.getCode()) {
            case 401:
            case 403:
                final Intent intent = new Intent(mContext, SignUpInActivity.class);
                try {
                    intent.putExtra("error", err_res.getErrorResponse().toString());
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                } finally {
                    mSharedPrefs.delString("access_token");
                    mContext.startActivity(intent);
                }
            /*default:
                if (err_res.getCode() >= 300)
                    try {
                        return new Pair<>(err_res.getCode(), response.body().string());
                    } catch (IOException | IllegalStateException e) {
                        e.printStackTrace(System.err);
                        return new ErrorOrEntity<?>(err_res.getCode(),
                                String.format(Locale.getDefault(), "%d (non string)", err_res.getCode()));
                    }
                return new Pair<>(err_res.getCode(), null);
            */
        }
    }
}
