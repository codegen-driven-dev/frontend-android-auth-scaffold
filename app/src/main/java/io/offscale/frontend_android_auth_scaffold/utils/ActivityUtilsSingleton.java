package io.offscale.frontend_android_auth_scaffold.utils;

import android.content.Intent;
import android.os.Bundle;

public final class ActivityUtilsSingleton {
    private Bundle mSavedInstanceState;
    private Intent mIntent;
    private PrefSingleton mSharedPref;
    private static ActivityUtilsSingleton mInstance;

    private ActivityUtilsSingleton() {
    }

    public final void Init(final Bundle savedInstanceState, final Intent intent,
                     final PrefSingleton sharedPrefs) {
        mSavedInstanceState = savedInstanceState;
        mIntent = intent;
        mSharedPref = sharedPrefs;
    }

    public static synchronized ActivityUtilsSingleton getInstance() {
        if (mInstance == null) mInstance = new ActivityUtilsSingleton();
        return mInstance;
    }

    public final String getFromLocalOrCache(final String key) {
        if (mSavedInstanceState == null) {
            final Bundle extras = mIntent.getExtras();
            if (extras != null)
                return extras.getString(key) == null ?
                        mSharedPref.getString(key) :
                        extras.getString(key);
        } else return mSavedInstanceState.getSerializable(key) == null ?
                mSharedPref.getString(key) :
                (String) mSavedInstanceState.getSerializable(key);
        return mSharedPref.getString(key);
    }
}
