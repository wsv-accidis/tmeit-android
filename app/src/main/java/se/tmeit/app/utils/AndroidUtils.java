package se.tmeit.app.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * Generic utility methods for Android.
 */
public final class AndroidUtils {
    private AndroidUtils() {
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static boolean hasApiLevel(int apiLevel) {
        return (Build.VERSION.SDK_INT >= apiLevel);
    }

    public static boolean isInEmulator() {
        // This is extremely unlikely to detect all emulators. For development use only.
        return Build.PRODUCT.equals("sdk_google_phone_x86");
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }
}
