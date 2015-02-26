package se.tmeit.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import se.tmeit.app.storage.NotificationStorage;

/**
 * Cleans up notifications that are deleted by the user.
 */
public final class NotificationDeletedReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_ID_EXTRA = "NotificationId";
    private static final String TAG = NotificationDeletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got notification deleted broadcast.");

        NotificationStorage storage = NotificationStorage.getInstance(context);
        int[] notificationsToDelete = intent.getIntArrayExtra(NOTIFICATION_ID_EXTRA);
        if (null != notificationsToDelete) {
            for (int notificationId : notificationsToDelete) {
                Log.v(TAG, "Removing notification. id = " + notificationId);
                storage.remove(notificationId);
            }
            storage.commit();
        } else {
            Log.w(TAG, "Notifications to delete was null. This is probably a bug!");
        }
    }
}
