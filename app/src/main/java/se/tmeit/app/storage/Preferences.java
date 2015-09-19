package se.tmeit.app.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import se.tmeit.app.utils.DateTimeUtils;

/**
 * Wrapper for Android shared preferences. Used to store application data between launches.
 */
public final class Preferences {
    private static final String PREFERENCES_FILE = "TmeitPreferences";
    private static final String TAG = Preferences.class.getSimpleName();
    private final SharedPreferences mPrefs;

    public Preferences(Context context) {
        mPrefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public String getAuthenticatedUserName() {
        return mPrefs.getString(Keys.AUTHENTICATED_USER_NAME, "");
    }

    public int getAuthenticatedUserId() {
        return mPrefs.getInt(Keys.AUTHENTICATED_USER_ID, 0);
    }

    public void setAuthenticatedUser(String username, int userId) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Keys.AUTHENTICATED_USER_NAME, username);
        editor.putInt(Keys.AUTHENTICATED_USER_ID, userId);
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

    public Calendar getLatestNotification() {
        String str = mPrefs.getString(Keys.LATEST_NOTIFICATION, "");
        return DateTimeUtils.parseIso8601(str);
    }

    public void setLatestNotification(Calendar date) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Keys.LATEST_NOTIFICATION, DateTimeUtils.formatIso8601(date));
        editor.commit();
    }

    public Set<Integer> getMembersListGroupsFilter() {
        return deserializeIntSet(mPrefs.getString(Keys.MEMBERS_LIST_GROUPS_FILTER, ""));
    }

    public Set<Integer> getMembersListTeamsFilter() {
        return deserializeIntSet(mPrefs.getString(Keys.MEMBERS_LIST_TEAMS_FILTER, ""));
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

    public void setMembersListFilters(Set<Integer> groupsFilter, Set<Integer> teamsFilter) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Keys.MEMBERS_LIST_GROUPS_FILTER, serializeIntSet(groupsFilter));
        editor.putString(Keys.MEMBERS_LIST_TEAMS_FILTER, serializeIntSet(teamsFilter));
        editor.commit();
    }

    private Set<Integer> deserializeIntSet(String str) {
        if (TextUtils.isEmpty(str)) {
            return Collections.emptySet();
        }

        try {
            Set<Integer> result = new HashSet<>();
            JSONArray array = new JSONArray(str);
            for (int i = 0; i < array.length(); i++) {
                result.add(array.getInt(i));
            }
            return result;
        } catch (JSONException ex) {
            Log.w(TAG, "Deserializing JSON integer array from preferences failed with an exception.", ex);
            return Collections.emptySet();
        }
    }

    private String serializeIntSet(Set<Integer> values) {
        if (null == values || values.isEmpty()) {
            return "";
        }

        JSONArray array = new JSONArray();
        for (Integer value : values) {
            array.put(value);
        }

        return array.toString();
    }

    private static class Keys {
        public static final String AUTHENTICATED_USER_NAME = "authenticatedUser";
        public static final String AUTHENTICATED_USER_ID = "authenticatedUserId";
        public static final String GCM_REGISTRATION_ID = "gcmRegistrationId";
        public static final String GCM_REGISTRATION_VERSION = "gcmRegistrationVersion";
        public static final String LATEST_NOTIFICATION = "latestNotification";
        public static final String MEMBERS_LIST_GROUPS_FILTER = "membersListGroupsFilter";
        public static final String MEMBERS_LIST_TEAMS_FILTER = "membersListTeamsFilter";
        public static final String SERVICE_AUTHENTICATION = "serviceAuthentication";

        private Keys() {
        }
    }
}
