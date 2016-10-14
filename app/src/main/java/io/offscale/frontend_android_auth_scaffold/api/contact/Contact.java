package io.offscale.frontend_android_auth_scaffold.api.contact;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

import static io.offscale.frontend_android_auth_scaffold.utils.GsonSingleton.getGson;

public final class Contact implements Cloneable {
    @SerializedName("name")
    private String mName;
    @SerializedName("email")
    private String mEmail;
    @SerializedName("owner")
    private final String mOwner;

    public Contact(final String name, final String email, final String owner) {
        mName = name;
        mEmail = email;
        mOwner = owner;
    }

    public Contact(final String name, final String email) {
        this(name, email, null);
    }

    @NonNull
    public final String getName() {
        return mName == null ? "" : mName;
    }

    @NonNull
    public final String getEmail() {
        return mEmail != null ? mEmail : "";
    }

    public final void setName(final String name) {
        mName = name;
    }

    public final void setEmail(final String email) {
        mEmail = email;
    }

    @NonNull
    public final String getNameOrEmail() {
        return getName().equals("") ? getEmail() : getName();
    }

    public final String getOwner() {
        return mOwner != null ? mOwner : "";
    }

    @Override
    @NonNull
    public final String toString() {
        return String.format(Locale.getDefault(), "Contact{\"name\": \"%s\"," +
                        " \"email\": \"%s\", \"owner\": \"%s\"}",
                mName, mEmail, mOwner);
    }

    public static Contact fromString(final String s) {
        return s == null ? null : getGson().fromJson(s.startsWith("{") ? s :
                s.substring(s.indexOf("{")), Contact.class);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            e.printStackTrace(System.err);
            return this;
        }
    }
}
