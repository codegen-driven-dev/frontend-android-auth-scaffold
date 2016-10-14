package io.offscale.frontend_android_auth_scaffold.utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public final class AuthRequestInterceptor implements Interceptor {
    final String mAccessToken;

    AuthRequestInterceptor(final String accessToken) {
        mAccessToken = accessToken;
    }

    @Override
    public final Response intercept(Interceptor.Chain chain) throws IOException {
        final Request originalRequest = chain.request();
        final Request compressedRequest = originalRequest.newBuilder()
                .header("X-Access-Token", mAccessToken)
                .method(originalRequest.method(), originalRequest.body())
                .build();
        return chain.proceed(compressedRequest);
    }
}
