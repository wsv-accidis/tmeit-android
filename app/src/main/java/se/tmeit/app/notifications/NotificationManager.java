package se.tmeit.app.notifications;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

import se.tmeit.app.model.Notification;
import se.tmeit.app.services.HttpClient;
import se.tmeit.app.services.TmeitServiceConfig;
import se.tmeit.app.storage.NotificationStorage;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.utils.DateTimeUtils;

/**
 * Displays and updates notifications based on data received from TMEIT web services.
 */
public final class NotificationManager {
    private final static int MAX_COUNT = 100;
    private final static String NOTIFICATIONS = "notifications";
    private final static String TAG = NotificationManager.class.getSimpleName();
    private final Preferences mPrefs;
    private final NotificationStorage mStorage;

    public NotificationManager(Context context) {
        mStorage = NotificationStorage.getInstance(context);
        mPrefs = new Preferences(context);
    }

    public void updateFromServer() {
        Calendar lastNotif = getTimeOfLatestNotification();
        Response response;
        try {
            String json = createJsonForGetNotifications(mPrefs.getAuthenticatedUser(), mPrefs.getServiceAuthentication(), lastNotif, MAX_COUNT);

            Request request = new Request.Builder()
                    .url(TmeitServiceConfig.BASE_URL + "GetNotifications.php")
                    .post(RequestBody.create(TmeitServiceConfig.JSON_MEDIA_TYPE, json))
                    .build();

            response = HttpClient.executeRequest(request);
        } catch (Exception ex) {
            Log.e(TAG, "Unexpected exception while getting notifications.", ex);
            return;
        }

        JSONObject responseBody = TmeitServiceConfig.getJsonBody(response, TAG);

        if (null == responseBody) {
            Log.e(TAG, "Got empty response from notifications request.");
        } else if (HttpStatus.SC_OK == response.code() && TmeitServiceConfig.isSuccessful(responseBody)) {
            handleNotifications(responseBody.optJSONArray(NOTIFICATIONS));
        } else {
            String errorMessage = TmeitServiceConfig.getErrorMessage(responseBody);
            Log.e(TAG, "Protocol error in notifications response. Error message = " + errorMessage);
        }

    }

    private static String createJsonForGetNotifications(String username, String serviceAuth, Calendar lastNotif, int maxCount) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(TmeitServiceConfig.SERVICE_AUTH_KEY, serviceAuth);
        json.put(TmeitServiceConfig.USERNAME_KEY, username);
        json.put(TmeitServiceConfig.NEW_SINCE_KEY, DateTimeUtils.formatIso8601(lastNotif));
        json.put(TmeitServiceConfig.MAX_COUNT_KEY, maxCount);
        return json.toString();
    }

    private Calendar getTimeOfLatestNotification() {
        Calendar lastNotif = mPrefs.getLatestNotification();
        if (null == lastNotif) {
            lastNotif = GregorianCalendar.getInstance();
            lastNotif.set(2000, 1, 1, 0, 0, 0); // set a date far in the past, so we'll get all notifications
        }
        return lastNotif;
    }

    private void handleNotifications(JSONArray jsonArray) {
        Calendar lastNotif = getTimeOfLatestNotification();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                Notification notification = Notification.fromJson(jsonObj);
                mStorage.add(notification);

                if (null != notification.getCreated() && -1 == lastNotif.compareTo(notification.getCreated())) {
                    lastNotif = notification.getCreated();
                }
            }
        } catch (JSONException ex) {
            Log.e(TAG, "Error parsing notifications from response.", ex);
            Log.d(TAG, "JSON that failed to parse: " + jsonArray.toString());
        } finally {
            mStorage.commit();
            mPrefs.setLatestNotification(lastNotif);
        }
    }
}
