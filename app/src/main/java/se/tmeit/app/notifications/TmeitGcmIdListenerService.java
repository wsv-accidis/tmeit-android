package se.tmeit.app.notifications;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Service which handles creation and updating of GCM registration tokens.
 */
public final class TmeitGcmIdListenerService extends InstanceIDListenerService {
	@Override
	public void onTokenRefresh() {
		Intent intent = new Intent(this, TmeitGcmRegistrationIntentService.class);
		startService(intent);
	}
}
