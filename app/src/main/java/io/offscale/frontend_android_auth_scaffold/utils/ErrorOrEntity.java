package io.offscale.frontend_android_auth_scaffold.utils;

import java.util.Locale;

import io.offscale.frontend_android_auth_scaffold.api.ErrorResponse;
import okhttp3.Headers;

/**
 * ErrorOrEntity class
 */
public final class ErrorOrEntity<E> {
    private final Exception mException;
    private final ErrorResponse mErrorResponse;
    private final Headers mHeaders;
    private final E mEntity;
    private final int mCode;

    public ErrorOrEntity(final Exception e,
                         final ErrorResponse errorResponse,
                         final Headers headers,
                         final int code,
                         final E entity) {
        mException = e;
        mErrorResponse = errorResponse;
        mHeaders = headers;
        mCode = code;
        mEntity = entity;
    }

    public ErrorOrEntity(final Exception e,
                         final ErrorResponse errorResponse,
                         final E entity) {
        this(e, errorResponse, null, -1, entity);
    }

    public ErrorOrEntity(final Exception e) {
        this(e, null, null, -1, null);
    }

    public final Exception getException() {
        return mException;
    }

    public final ErrorResponse getErrorResponse() {
        return mErrorResponse;
    }

    public final Headers getHeaders() {
        return mHeaders;
    }

    public final int getCode() {
        return mCode;
    }

    public final E getEntity() {
        return mEntity;
    }

    public final boolean success() {
        return mException == null && mErrorResponse == null;
    }

    @Override
    public final String toString() {
        return String.format(Locale.getDefault(),
                "ErrorOrEntity{mException: %s, mErrorResponse: %s, mEntity: %s}",
                getException(), getErrorResponse(), getEntity());
    }
}
