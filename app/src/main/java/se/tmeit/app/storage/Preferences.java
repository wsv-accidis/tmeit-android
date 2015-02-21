package se.tmeit.app.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Wilhelm Svenselius on 2015-02-21.
 */
public final class Preferences {
    private static final String PREFERENCES_FILE = "TmeitPreferences";
    private final SharedPreferences mPrefs;

    public Preferences(Context context) {
        mPrefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public String getServiceAuthentication() {
        return mPrefs.getString(Keys.SERVICE_AUTHENTICATION, "");
    }

    public boolean hasServiceAuthentication() {
        return !getServiceAuthentication().isEmpty();
    }

    public void setServiceAuthentication(String serviceAuth) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Keys.SERVICE_AUTHENTICATION, serviceAuth);
        editor.commit();
    }

    private static interface Keys {
        public static final String SERVICE_AUTHENTICATION = "serviceAuthentication";
    }
}
