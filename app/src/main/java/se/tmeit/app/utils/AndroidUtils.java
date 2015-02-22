package se.tmeit.app.utils;

import android.os.Build;

/**
 * Generic utility methods for Android.
 */
public final class AndroidUtils {
    private AndroidUtils() {
    }

    public static boolean hasApiLevel(int apiLevel) {
        return (Build.VERSION.SDK_INT >= apiLevel);
    }

    public static boolean isInEmulator() {
        // This is extremely unlikely to detect all emulators. For development use only.
        return Build.PRODUCT.equals("sdk_google_phone_x86");
    }
}
