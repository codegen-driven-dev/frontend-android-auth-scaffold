package io.offscale.frontend_android_auth_scaffold.api;

import android.content.Context;

import io.offscale.frontend_android_auth_scaffold.utils.BaseApiClient;
import io.offscale.frontend_android_auth_scaffold.utils.CachedReq;
import okhttp3.Request;
import okhttp3.RequestBody;

import static io.offscale.frontend_android_auth_scaffold.utils.IMimeTypes.MEDIA_TYPE_JSON;

public final class AuthClient extends BaseApiClient {
    private AuthClient(final Context context, final String hostname, final CachedReq cache) {
        super(context, hostname, cache);
    }

    public AuthClient(final Context context) {
        this(context, null, null);
    }

    public final Request register(final String email, final String password) {
        return new Request.Builder()
                .url(getBaseUri() + "/user")
                .post(RequestBody.create(MEDIA_TYPE_JSON,
                        String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password)))
                .build();
    }

    public final Request login(final String email, final String password) {
        return new Request.Builder()
                .url(getBaseUri() + "/auth")
                .post(RequestBody.create(MEDIA_TYPE_JSON,
                        String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password)))
                .build();
    }
}
