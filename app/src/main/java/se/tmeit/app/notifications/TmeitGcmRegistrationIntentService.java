package se.tmeit.app.notifications;

import android.app.IntentService;
import android.content.Intent;

/**
 * Intent service to handle GCM registration.
 */
public final class TmeitGcmRegistrationIntentService extends IntentService {
	private static final String TAG = TmeitGcmRegistrationIntentService.class.getSimpleName();

	public TmeitGcmRegistrationIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		GcmRegistration.getInstance(getApplicationContext()).register();
	}
}
