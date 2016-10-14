package io.offscale.frontend_android_auth_scaffold.utils;


public class Formatters {
    public static String ExceptionFormatter(final Exception e) {
        return e.getClass().getCanonicalName() + ": " +  e.getMessage();
    }
}
