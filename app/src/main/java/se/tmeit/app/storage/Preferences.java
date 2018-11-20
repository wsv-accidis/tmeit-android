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

    public int getAuthenticatedUserId() {
        return mPrefs.getInt(Keys.AUTHENTICATED_USER_ID, 0);
    }

    public String getAuthenticatedUserName() {
        return mPrefs.getString(Keys.AUTHENTICATED_USER_NAME, "");
    }

    public String getGcmRegistrationId() {
        return mPrefs.getString(Keys.GCM_REGISTRATION_ID, "");
    }

    public void setGcmRegistrationId(String registrationId) {
        mPrefs.edit().putString(Keys.GCM_REGISTRATION_ID, registrationId).apply();
    }

    public Calendar getLatestNotification() {
        String str = mPrefs.getString(Keys.LATEST_NOTIFICATION, "");
        return DateTimeUtils.parseIso8601(str);
    }

    public void setLatestNotification(Calendar date) {
        mPrefs.edit().putString(Keys.LATEST_NOTIFICATION, DateTimeUtils.formatIso8601(date)).apply();
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
        mPrefs.edit().putString(Keys.SERVICE_AUTHENTICATION, serviceAuth).apply();
    }

    public boolean hasGcmRegistrationId() {
        return !getGcmRegistrationId().isEmpty();
    }

    public boolean hasServiceAuthentication() {
        return !getServiceAuthentication().isEmpty();
    }

    public void setAuthenticatedUser(String username, int userId) {
        mPrefs.edit()
                .putString(Keys.AUTHENTICATED_USER_NAME, username)
                .putInt(Keys.AUTHENTICATED_USER_ID, userId)
                .apply();
    }

    public void setMembersListFilters(Set<Integer> groupsFilter, Set<Integer> teamsFilter) {
        mPrefs.edit()
                .putString(Keys.MEMBERS_LIST_GROUPS_FILTER, serializeIntSet(groupsFilter))
                .putString(Keys.MEMBERS_LIST_TEAMS_FILTER, serializeIntSet(teamsFilter))
                .apply();
    }

    public void setShouldRefreshExternalEvents(boolean value) {
        mPrefs.edit().putBoolean(Keys.REFRESH_EXTERNAL_EVENTS, value).apply();
    }

    public void setShouldRefreshInternalEvents(boolean value) {
        mPrefs.edit().putBoolean(Keys.REFRESH_INTERNAL_EVENTS, value).apply();
    }

    public void setShouldRefreshMembers(boolean value) {
        mPrefs.edit().putBoolean(Keys.REFRESH_MEMBERS, value).apply();
    }

    public boolean shouldRefreshExternalEvents() {
        return mPrefs.getBoolean(Keys.REFRESH_EXTERNAL_EVENTS, false);
    }

    public boolean shouldRefreshInternalEvents() {
        return mPrefs.getBoolean(Keys.REFRESH_INTERNAL_EVENTS, false);
    }

    public boolean shouldRefreshMembers() {
        return mPrefs.getBoolean(Keys.REFRESH_MEMBERS, false);
    }

    private Set<Integer> deserializeIntSet(String str) {
        if (TextUtils.isEmpty(str)) {
            return Collections.emptySet();
        }

        try {
            final Set<Integer> result = new HashSet<>();
            final JSONArray array = new JSONArray(str);

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

        final JSONArray array = new JSONArray();
        for (Integer value : values) {
            array.put(value);
        }

        return array.toString();
    }

    private static class Keys {
        public static final String AUTHENTICATED_USER_ID = "authenticatedUserId";
        public static final String AUTHENTICATED_USER_NAME = "authenticatedUser";
        public static final String GCM_REGISTRATION_ID = "gcmRegistrationId";
        public static final String LATEST_NOTIFICATION = "latestNotification";
        public static final String MEMBERS_LIST_GROUPS_FILTER = "membersListGroupsFilter";
        public static final String MEMBERS_LIST_TEAMS_FILTER = "membersListTeamsFilter";
        public static final String REFRESH_EXTERNAL_EVENTS = "refreshExternalEvents";
        public static final String REFRESH_INTERNAL_EVENTS = "refreshInternalEvents";
        public static final String REFRESH_MEMBERS = "refreshMembers";
        public static final String SERVICE_AUTHENTICATION = "serviceAuthentication";

        private Keys() {
        }
    }
}
