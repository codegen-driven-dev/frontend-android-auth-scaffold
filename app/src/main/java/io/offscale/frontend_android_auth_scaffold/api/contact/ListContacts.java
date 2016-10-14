package io.offscale.frontend_android_auth_scaffold.api.contact;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Locale;

public final class ListContacts {
    @SerializedName("contacts")
    private Contact[] mContacts;
    @SerializedName("owner")
    private String mOwner;

    private ListContacts() {
    }

    public ListContacts(final String owner, final Contact[] contacts) {
        mOwner = owner;
        mContacts = contacts;
    }

    public final Contact[] getContacts() {
        return mContacts;
    }

    public final String getOwner() {
        return mOwner;
    }

    @Override
    public final String toString() {
        return String.format(Locale.getDefault(), "ListContacts{owner %s, contacts: %s}",
                mOwner, Arrays.toString(mContacts));
    }
}
