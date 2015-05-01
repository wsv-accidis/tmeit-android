package se.tmeit.app;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;

import se.tmeit.app.services.TmeitHttpClient;

/**
 * Application class.
 */
public final class TmeitApplication extends Application {
    private static final String TAG = TmeitApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        installProviderIfNeeded();
        TmeitHttpClient.initializeCache(getApplicationContext());
    }

    private void installProviderIfNeeded() {
        // See https://developer.android.com/training/articles/security-gms-provider.html
        ProviderInstaller.installIfNeededAsync(getApplicationContext(), new ProviderInstaller.ProviderInstallListener() {
            @Override
            public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
                Log.e(TAG, "Google Play Services security provider failed to update automatically.");
                GoogleApiAvailability.getInstance().showErrorNotification(getApplicationContext(), errorCode);
            }

            @Override
            public void onProviderInstalled() {
                Log.i(TAG, "Google Play Services security provider was updated successfully.");
                TmeitHttpClient.initializeSslSocketFactory();
            }
        });
    }
}
