package io.offscale.frontend_android_auth_scaffold.api.contact;

import android.content.Context;

import com.google.gson.Gson;

import okhttp3.Request;

import io.offscale.frontend_android_auth_scaffold.utils.BaseApiClient;
import io.offscale.frontend_android_auth_scaffold.utils.ErrorOrEntity;
import io.offscale.frontend_android_auth_scaffold.utils.ApiClient;
import okhttp3.RequestBody;

import static io.offscale.frontend_android_auth_scaffold.utils.GsonSingleton.getGson;
import static io.offscale.frontend_android_auth_scaffold.utils.IMimeTypes.MEDIA_TYPE_JSON;

public final class ContactsClient extends BaseApiClient {
    private final String mApiPrefix = "/contact";
    private final Gson mGson = getGson();

    public ContactsClient(final Context context, final String accessToken) {
        super(context, null, null, null, null, true, accessToken);
    }

    public final Request get() {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix)
                .get()
                .build();
    }

    public final Request post(final Contact contact) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix)
                .post(RequestBody.create(MEDIA_TYPE_JSON, mGson.toJson(contact)))
                .build();
    }

    public final ErrorOrEntity<Contact> postSync(final Contact contact) {
        return ApiClient.sync(getClient(), post(contact), Contact.class);
    }

    public final ErrorOrEntity<ListContacts> getSync() {
        return ApiClient.sync(getClient(), get(), ListContacts.class);
    }
}
