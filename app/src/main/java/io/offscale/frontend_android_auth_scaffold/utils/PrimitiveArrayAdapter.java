package io.offscale.frontend_android_auth_scaffold.utils;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;


public class PrimitiveArrayAdapter<E> extends ArrayAdapter<E> {
    private PrimitiveArrayAdapter(final Context context, final ArrayList<E> arrayList) {
        super(context, 0, arrayList);
    }

    public PrimitiveArrayAdapter(final Context context, final E[] primitiveArray) {
        this(context, new ArrayList<>(Arrays.asList(primitiveArray)));
    }
}
