package io.offscale.frontend_android_auth_scaffold.api;

import android.content.Context;

import io.offscale.frontend_android_auth_scaffold.utils.BaseApiClient;
import io.offscale.frontend_android_auth_scaffold.utils.CachedReq;
import okhttp3.Request;
import okhttp3.RequestBody;

import static io.offscale.frontend_android_auth_scaffold.utils.IMimeTypes.MEDIA_TYPE_JSON;

public final class MessageClient extends BaseApiClient {
    private final String mApiPrefix = "/message/";

    private MessageClient(final Context context, final String hostname, final CachedReq cache) {
        super(context, hostname, cache);
    }

    public MessageClient(final Context context) {
        this(context, null, null);
    }

    public final Request get(final String to) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix + to)
                .get()
                .build();
    }

    public final Request post(final String to, final String message) {
        return new Request.Builder()
                .url(getBaseUri() + mApiPrefix + to)
                .post(RequestBody.create(MEDIA_TYPE_JSON,
                        String.format("{\"to\": \"%s\", \"message\": \"%s\"}", to, message)))
                .build();
    }
}
