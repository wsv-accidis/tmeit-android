package se.tmeit.app.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Wrapper for Android shared preferences. Used to store application data between launches.
 */
public final class Preferences {
    private static final String PREFERENCES_FILE = "TmeitPreferences";
    private final SharedPreferences mPrefs;

    public Preferences(Context context) {
        mPrefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public String getAuthenticatedUser() {
        return mPrefs.getString(Keys.AUTHENTICATED_USER, "");
    }

    public void setAuthenticatedUser(String username) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Keys.AUTHENTICATED_USER, username);
        editor.commit();
    }

    public String getGcmRegistrationId() {
        return mPrefs.getString(Keys.GCM_REGISTRATION_ID, "");
    }

    public void setGcmRegistrationId(String registrationId) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Keys.GCM_REGISTRATION_ID, registrationId);
        editor.commit();
    }

    public int getGcmRegistrationVersion() {
        return mPrefs.getInt(Keys.GCM_REGISTRATION_VERSION, 0);
    }

    public void setGcmRegistrationVersion(int registrationVersion) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(Keys.GCM_REGISTRATION_VERSION, registrationVersion);
        editor.commit();
    }

    public String getServiceAuthentication() {
        return mPrefs.getString(Keys.SERVICE_AUTHENTICATION, "");
    }

    public void setServiceAuthentication(String serviceAuth) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Keys.SERVICE_AUTHENTICATION, serviceAuth);
        editor.commit();
    }

    public boolean hasGcmRegistrationId() {
        return !getGcmRegistrationId().isEmpty();
    }

    public boolean hasServiceAuthentication() {
        return !getServiceAuthentication().isEmpty();
    }

    private static interface Keys {
        public static final String AUTHENTICATED_USER = "authenticatedUser";
        public static final String GCM_REGISTRATION_ID = "gcmRegistrationId";
        public static final String GCM_REGISTRATION_VERSION = "gcmRegistrationVersion";
        public static final String SERVICE_AUTHENTICATION = "serviceAuthentication";
    }
}
