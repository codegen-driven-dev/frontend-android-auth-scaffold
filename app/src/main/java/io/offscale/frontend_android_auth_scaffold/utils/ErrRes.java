package io.offscale.frontend_android_auth_scaffold.utils;

import android.util.Pair;

import java.util.Locale;


/**
 * ErrRes class
 */
public class ErrRes<F, S> extends Pair<F, S> {
    public ErrRes(final F error, final S result) {
        super(error, result);
    }

    public final boolean failure() {
        return !success();
    }

    public final boolean success() {
        return getError() == null && getResult() != null;
    }

    public final F getError() {
        return first;
    }

    public final S getResult() {
        return second;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "ErrRes{error: %s, res: %s}",
                getError(), getResult());
    }
}
