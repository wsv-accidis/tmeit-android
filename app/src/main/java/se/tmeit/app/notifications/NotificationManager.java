package se.tmeit.app.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
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
import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.Notification;
import se.tmeit.app.services.TmeitHttpClient;
import se.tmeit.app.services.TmeitServiceConfig;
import se.tmeit.app.storage.NotificationStorage;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.utils.AndroidUtils;
import se.tmeit.app.utils.DateTimeUtils;

/**
 * Fetches and displays notifications from TMEIT web services.
 */
public final class NotificationManager {
    private final static int MAX_COUNT = 100;
    private final static String NOTIFICATIONS = "notifications";
    private final static String NOTIFICATION_DELETED_ACTION = "se.tmeit.app.NOTIFICATION_DELETED_";
    private final static String NOTIFICATION_DELETED_SUMMARY_ACTION = "se.tmeit.app.NOTIFICATION_DELETED_SUMMARY";
    private final static String NOTIFICATION_GROUP = "tmeit";
    private final static int NOTIFICATION_ID_OFFSET = 100;
    private final static int NOTIFICATION_SUMMARY_ID = 1;
    private final static String TAG = NotificationManager.class.getSimpleName();
    private final Context mContext;
    private final Preferences mPrefs;
    private final NotificationStorage mStorage;

    public NotificationManager(Context context) {
        mContext = context;
        mStorage = NotificationStorage.getInstance(context);
        mPrefs = new Preferences(context);
    }

    public void createNotifications() {
        List<Notification> notifications = mStorage.get();
        boolean moreThanOne = notifications.size() > 1;

        // Create or update individual notifications for each notification
        // See: http://developer.android.com/training/notify-user/managing.html
        for (Notification notif : notifications) {
            createSingleNotification(notif, moreThanOne);
        }

        // Create or update a summary notification if there is more than one notification active
        // See: https://developer.android.com/training/wearables/notifications/stacks.html
        if (moreThanOne) {
            createSummaryNotification(notifications);
        }
    }

    public void updateFromServer() {
        Calendar lastNotif = getTimeOfLatestNotification();
        Response response;
        try {
            String json = createJsonForGetNotifications(mPrefs.getAuthenticatedUser(), mPrefs.getServiceAuthentication(), lastNotif, MAX_COUNT);

            Request request = new Request.Builder()
                    .url(TmeitServiceConfig.SERVICE_BASE_URL + "GetNotifications.php")
                    .post(RequestBody.create(TmeitServiceConfig.JSON_MEDIA_TYPE, json))
                    .build();

            response = TmeitHttpClient.getInstance().executeRequest(request);
        } catch (Exception ex) {
            Log.e(TAG, "Unexpected exception while getting notifications.", ex);
            return;
        }

        JSONObject responseBody = TmeitServiceConfig.getJsonBody(response, TAG);

        if (null == responseBody) {
            Log.e(TAG, "Got empty response from notifications request.");
        } else if (HttpStatus.SC_OK == response.code()) {
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

    private void createSingleNotification(Notification notif, boolean inGroup) {
        Intent deleteIntent = new Intent(mContext, NotificationDeletedReceiver.class);
        deleteIntent.setAction(NOTIFICATION_DELETED_ACTION + notif.getId());
        deleteIntent.putExtra(NotificationDeletedReceiver.NOTIFICATION_ID_EXTRA, new int[]{notif.getId()});
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(mContext, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            /*
             * To attach an action to the notification - we don't currently use this.
             * Attach to builder with .setContentIntent(resultPendingIntent)
             *
            Intent resultIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            */

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setAutoCancel(true)
                .setContentText(getContentTextFromNotification(notif))
                .setContentTitle(mContext.getString(R.string.notifications_notification_from_tmeit))
                .setDeleteIntent(deletePendingIntent)
                .setSmallIcon(R.drawable.ic_notification);

        if (inGroup) {
            builder.setGroup(NOTIFICATION_GROUP);
        }

        boolean isAndroidLollipopOrLater = AndroidUtils.hasApiLevel(Build.VERSION_CODES.LOLLIPOP);
        if (isAndroidLollipopOrLater) {
            builder.setCategory(android.app.Notification.CATEGORY_SOCIAL);
            Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_notification_large_circle);
            builder.setLargeIcon(largeIcon);
        }

        NotificationManagerCompat.from(mContext).notify(NOTIFICATION_ID_OFFSET + notif.getId(), builder.build());
    }

    private void createSummaryNotification(List<Notification> notifications) {
        int[] notificationIds = new int[notifications.size()];
        int idx = 0;
        for (Notification notif : notifications) {
            notificationIds[idx++] = notif.getId();
        }

        Intent deleteIntent = new Intent(mContext, NotificationDeletedReceiver.class);
        deleteIntent.setAction(NOTIFICATION_DELETED_SUMMARY_ACTION);
        deleteIntent.putExtra(NotificationDeletedReceiver.NOTIFICATION_ID_EXTRA, notificationIds);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(mContext, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        boolean isAndroidLollipopOrLater = AndroidUtils.hasApiLevel(Build.VERSION_CODES.LOLLIPOP);
        Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), (isAndroidLollipopOrLater ? R.drawable.ic_notification_large_circle : R.drawable.ic_notification_large));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setAutoCancel(true)
                .setContentTitle(String.format(mContext.getString(R.string.notifications_x_notifications_from_tmeit), notifications.size()))
                .setDeleteIntent(deletePendingIntent)
                .setGroup(NOTIFICATION_GROUP)
                .setGroupSummary(true)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(getInboxStyleFromNotifications(notifications));

        if (isAndroidLollipopOrLater) {
            builder.setCategory(android.app.Notification.CATEGORY_SOCIAL);
        }

        NotificationManagerCompat.from(mContext).notify(NOTIFICATION_SUMMARY_ID, builder.build());
    }

    private static CharSequence getContentTextFromNotification(Notification notif) {
        // body typically has this format: "Something happened: <span>Title</span>"
        // since the notifications aren't HTML we replace the spans with something native
        String body = notif.getBody();
        int indexOfStartSpan = body.indexOf("<span>");
        body = body.replace("<span>", "");
        int indexOfEndSpan = body.indexOf("</span>");
        body = body.replace("</span>", "");

        // indexOfEndSpan will be past the end of the string when the end tag was at the
        // end of it, but that's what setSpan wants. Keep it happy!
        if (indexOfStartSpan != -1 && indexOfEndSpan > indexOfStartSpan) {
            Spannable spannable = new SpannableString(body);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), indexOfStartSpan, indexOfEndSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannable;
        } else {
            return body;
        }
    }

    private NotificationCompat.Style getInboxStyleFromNotifications(List<Notification> notifications) {
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for (Notification notif : notifications) {
            style.addLine(getContentTextFromNotification(notif));
        }

        style.setBigContentTitle(String.format(mContext.getString(R.string.notifications_x_notifications_from_tmeit), notifications.size()));
        style.setSummaryText(mContext.getString(R.string.notifications_go_to_tmeit_se));
        return style;
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
                Log.d(TAG, "Added notification: " + notification.toString());

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
