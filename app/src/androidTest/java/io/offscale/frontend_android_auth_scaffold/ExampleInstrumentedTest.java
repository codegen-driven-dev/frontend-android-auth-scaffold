package io.offscale.frontend_android_auth_scaffold;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public final class ExampleInstrumentedTest {
    @Test
    public final void useAppContext() throws Exception {
        // Context of the app under test.
        final Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("io.offscale.frontend_android_auth_scaffold", appContext.getPackageName());
    }
}
