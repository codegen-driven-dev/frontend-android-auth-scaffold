package io.offscale.frontend_android_auth_scaffold.api;

import android.content.Context;

import io.offscale.frontend_android_auth_scaffold.utils.BaseApiClient;
import io.offscale.frontend_android_auth_scaffold.utils.CachedReq;
import okhttp3.Request;

public final class ServerClient extends BaseApiClient {
    private ServerClient(final Context context, final String hostname, final CachedReq cache) {
        super(context, hostname, cache);
    }

    public ServerClient(final Context context) {
        this(context, null, null);
    }

    public final Request get_version() {
        return new Request.Builder()
                .url(getBaseUri())
                .get()
                .build();
    }
}
