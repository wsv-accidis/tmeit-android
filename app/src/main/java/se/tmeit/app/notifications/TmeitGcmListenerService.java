package se.tmeit.app.notifications;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Service which handles message syncing upon receipt of a GCM message.
 */
public final class TmeitGcmListenerService extends GcmListenerService {
	private final static String TAG = TmeitGcmListenerService.class.getSimpleName();

	@Override
	public void onMessageReceived(String from, Bundle data) {
		Log.i(TAG, "Message received from GCM.");

		NotificationManager notificationFetcher = new NotificationManager(this);
		notificationFetcher.updateFromServer();
		notificationFetcher.createNotifications();
	}
}
