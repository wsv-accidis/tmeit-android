package se.tmeit.app.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import se.tmeit.app.R;
import se.tmeit.app.utils.AndroidUtils;

/**
 * Service which handles message syncing upon receipt of a GCM message.
 */
public final class GcmIntentService extends IntentService {
    private final static String NOTIFICATION_DELETED_ACTION = "se.tmeit.app.NOTIFICATION_DELETED";
    private final static String NOTIFICATION_GROUP = "tmeit";
    private final static int NOTIFICATION_ID = 1;
    private final static String TAG = GcmIntentService.class.getSimpleName();

    public GcmIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            Log.i(TAG, "Message received from GCM.");

            NotificationManager notificationManager = new NotificationManager(this);
            notificationManager.updateFromServer();

            // TODO Create notifications based on the actual notifications received
            // See http://developer.android.com/training/notify-user/managing.html
            // See https://developer.android.com/training/wearables/notifications/stacks.html#AddSummary

            /*
             * To attach an action to the notification - we don't currently use this.
             * Attach to builder with .setContentIntent(resultPendingIntent)
             *
            Intent resultIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            */

            Intent deleteIntent = new Intent(NOTIFICATION_DELETED_ACTION);
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(this, 0, deleteIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)
                    .setContentText(getString(R.string.notifications_message_has_no_content))
                    .setContentTitle(getString(R.string.notifications_notification_from_tmeit))
                    .setDeleteIntent(deletePendingIntent)
                    .setStyle(new NotificationCompat.InboxStyle())
                    .setGroup(NOTIFICATION_GROUP)
                    .setSmallIcon(R.drawable.ic_notification);

            if (AndroidUtils.hasApiLevel(Build.VERSION_CODES.LOLLIPOP)) {
                builder.setCategory(Notification.CATEGORY_SOCIAL);
            }

            NotificationManagerCompat.from(this)
                    .notify(NOTIFICATION_ID, builder.build());
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


}
