package io.offscale.frontend_android_auth_scaffold.api;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;


public class ErrorResponse {
    @SerializedName("error")
    private String mError;
    @SerializedName("error_message")
    private String mErrorMessage;

    private ErrorResponse() {
    }

    public ErrorResponse(final String error, final String errorMessage) {
        mError = error;
        mErrorMessage = errorMessage;
    }

    @Override
    public final String toString() {
        return String.format(Locale.getDefault(), "%s: %s", mError, mErrorMessage);
    }
}
