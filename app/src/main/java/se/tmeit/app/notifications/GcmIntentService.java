package se.tmeit.app.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Service which handles message syncing upon receipt of a GCM message.
 */
public final class GcmIntentService extends IntentService {

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

            NotificationManager notificationFetcher = new NotificationManager(this);
            notificationFetcher.updateFromServer();
            notificationFetcher.createNotifications();

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
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


}
