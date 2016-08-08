package se.tmeit.app;

import android.app.Application;

import se.tmeit.app.services.TmeitHttpClient;

/**
 * Application class.
 */
public final class TmeitApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
		TmeitHttpClient.initialize(getApplicationContext());
    }
}
