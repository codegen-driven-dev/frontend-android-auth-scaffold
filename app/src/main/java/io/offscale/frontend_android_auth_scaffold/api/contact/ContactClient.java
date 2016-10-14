package io.offscale.frontend_android_auth_scaffold.api.contact;

import android.content.Context;

import com.google.gson.Gson;

import io.offscale.frontend_android_auth_scaffold.utils.ApiClient;
import io.offscale.frontend_android_auth_scaffold.utils.BaseApiClient;
import io.offscale.frontend_android_auth_scaffold.utils.CachedReq;
import io.offscale.frontend_android_auth_scaffold.utils.ErrorOrEntity;

import okhttp3.Request;
import okhttp3.RequestBody;

import static io.offscale.frontend_android_auth_scaffold.utils.GsonSingleton.getGson;
import static io.offscale.frontend_android_auth_scaffold.utils.IMimeTypes.MEDIA_TYPE_JSON;

public final class ContactClient extends BaseApiClient {
    private final String mApiPrefix = "/contact";
    private final Gson mGson = getGson();

    private ContactClient(final Context context, final String hostname, final CachedReq cache) {
        super(context, hostname, cache);
    }

    public ContactClient(final Context context) {
        this(context, null, null);
    }

    public ContactClient(final Context context, final String accessToken) {
        super(context, null, null, null, null, true, accessToken);
    }

    public final Request get(final String nameOrEmail) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix + "/" + nameOrEmail)
                .get()
                .build();
    }

    public final ErrorOrEntity<Contact> getSync(final String nameOrEmail) {
        return ApiClient.sync(getClient(), get(nameOrEmail), Contact.class);
    }

    public final Request get(final Contact contact) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix + "/" + contact.getEmail())
                .get()
                .build();
    }

    public final ErrorOrEntity<Contact> getSync(final Contact contact) {
        return ApiClient.sync(getClient(), get(contact), Contact.class);
    }

    public final Request put(final Contact prevContact, final Contact newContact) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix + "/" + prevContact.getEmail())
                .put(RequestBody.create(MEDIA_TYPE_JSON, mGson.toJson(newContact)))
                .build();
    }

    public final ErrorOrEntity<Contact> putSync(final Contact prevContact,
                                                final Contact newContact) {
        return ApiClient.sync(getClient(), put(prevContact, newContact), Contact.class);
    }

    public final Request del(final Contact contact) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix + "/" + contact.getEmail())
                .delete()
                .build();
    }

    public final ErrorOrEntity<Contact> delSync(final Contact contact) {
        return ApiClient.sync(getClient(), del(contact), Contact.class);
    }
}
