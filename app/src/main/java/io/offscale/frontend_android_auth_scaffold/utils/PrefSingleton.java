package io.offscale.frontend_android_auth_scaffold.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefSingleton {
    private static PrefSingleton mInstance;
    private SharedPreferences mPref;

    private PrefSingleton() {
    }

    public static synchronized PrefSingleton getInstance() {
        if (mInstance == null) mInstance = new PrefSingleton();
        return mInstance;
    }

    public final void Init(final Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public final void putString(final String key, final String value) {
        final SharedPreferences.Editor e = mPref.edit();
        e.putString(key, value);
        e.commit();
    }

    public final void delString(final String key) {
        final SharedPreferences.Editor e = mPref.edit();
        e.remove(key);
        e.commit();
    }

    final String getString(final String key) {
        return mPref.getString(key, null);
    }

    public final SharedPreferences getSharedPreferences() {
        return mPref;
    }

    public final boolean contains(final String key) {
        return mPref.contains(key);
    }
}
