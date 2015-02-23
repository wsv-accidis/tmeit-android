package se.tmeit.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Cleans up notifications that are deleted by the user.
 */
public final class NotificationDeletedReceiver extends BroadcastReceiver {
    private static final String TAG = NotificationDeletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notification cleared.");

        // TODO
    }
}
