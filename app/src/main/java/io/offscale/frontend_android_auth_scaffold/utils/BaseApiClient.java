package io.offscale.frontend_android_auth_scaffold.utils;

import android.content.Context;

import io.offscale.frontend_android_auth_scaffold.R;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;


public class BaseApiClient {
    private final OkHttpClient mClient;
    private final String mBaseUri;

    public BaseApiClient(final Context context,
                         final String hostname,
                         final CachedReq cache,
                         final Authenticator authenticator,
                         final Interceptor[] interceptors,
                         final boolean authInterceptor,
                         final String accessToken) {
        final OkHttpClient.Builder builder = cache == null ?
                (new CachedReq(context.getFilesDir().getPath(), getClass().getCanonicalName())
                ).getClientBuilder() : cache.getClientBuilder();
        if (authenticator != null)
            builder.authenticator(authenticator);
        if (interceptors != null && interceptors.length > 0)
            for (final Interceptor interceptor : interceptors)
                builder.addInterceptor(interceptor);
        if (authInterceptor && accessToken != null)
            builder.addInterceptor(new AuthRequestInterceptor(accessToken));
        mClient = builder.build();
        mBaseUri = (hostname == null ? context.getString(R.string.api_prefix) : hostname) + "/api";
    }

    public BaseApiClient(final Context context,
                         final String hostname,
                         final CachedReq cache) {
        this(context, hostname, cache, null, null, false, null);
    }

    public final OkHttpClient getClient() {
        return mClient;
    }

    protected final String getBaseUri() {
        return mBaseUri;
    }
}
