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
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


}
