package io.offscale.frontend_android_auth_scaffold.utils;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Response;

/**
 * ErrRes class
 */
public class ErrResResponse<F, S> extends ErrRes<F, S> {
    private final Response mResponse;

    public ErrResResponse(final F error, final S result, final Response response) {
        super(error, result);
        mResponse = response;
    }

    public final Response getResponse() {
        return mResponse;
    }

    @Override
    public String toString() {
        final String r = String.format(Locale.getDefault(),
                "{mError: %s, mResult: %s, mResponse: {code: %d",
                getError(), getResult(), mResponse.code());

        try {
            return String.format(Locale.getDefault(), "%s, body: %s} }",
                    r, mResponse.body().string());
        } catch (IOException | IllegalStateException e) {
            return r + "} }";
        }
    }
}
