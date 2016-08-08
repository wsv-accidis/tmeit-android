package se.tmeit.app.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import se.tmeit.app.R;
import se.tmeit.app.services.TmeitHttpClient;
import se.tmeit.app.services.TmeitServiceConfig;
import se.tmeit.app.storage.Preferences;

/**
 * Handles registering/unregistering with GCM and the TMEIT backend to receive notifications.
 */
public final class GcmRegistration {
	public final static String REGISTRATION_COMPLETE_BROADCAST = "gcmRegistrationComplete";
	public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final String TAG = GcmRegistration.class.getSimpleName();
	private static GcmRegistration mInstance;
	private final Context mContext;
	private final Preferences mPrefs;
	private final TmeitServiceHelper mTmeit = new TmeitServiceHelper();
	private boolean mIsBusy;

	private GcmRegistration(Context context) {
		mContext = context;
		mPrefs = new Preferences(context);
	}

	public static synchronized GcmRegistration getInstance(Context context) {
		if (null == mInstance) {
			mInstance = new GcmRegistration(context.getApplicationContext());
		}
		return mInstance;
	}

	public boolean isBusy() {
		return mIsBusy;
	}

	public boolean isRegistered() {
		return mPrefs.hasGcmRegistrationId();
	}

	public void register() {
		if (mIsBusy) {
			Log.w(TAG, "Register called while another task is in progress. The call was dropped.");
			return;
		}

		mIsBusy = true;
		RegisterTask task = new RegisterTask();
		task.execute();

	}

	public void unregister() {
		if (mIsBusy) {
			Log.w(TAG, "Unregister called while another task is in progress. The call was dropped.");
			return;
		}

		mIsBusy = true;
		UnregisterTask task = new UnregisterTask();
		task.execute();
	}

	private void broadcastRegistrationComplete() {
		Intent registrationComplete = new Intent(REGISTRATION_COMPLETE_BROADCAST);
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(registrationComplete);
	}

	private final class RegisterTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			Log.i(TAG, "Registering with Google Cloud Messaging.");

			// Clear old registration data, so we're not left registered if this fails
			mPrefs.setGcmRegistrationId("");

			String registrationId;
			try {
				InstanceID instanceID = InstanceID.getInstance(mContext);
				registrationId = instanceID.getToken(mContext.getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			} catch (Exception ex) {
				Log.e(TAG, "Failed to register with Google Cloud Messaging.", ex);
				return null;
			}

			Log.v(TAG, "Successfully registered with Google Cloud Messaging. RegistrationId = \"" + registrationId + "\".");

			try {
				String authenticatedUser = mPrefs.getAuthenticatedUserName();
				String serviceAuth = mPrefs.getServiceAuthentication();
				if (!mTmeit.registerGcm(registrationId, serviceAuth, authenticatedUser)) {
					return null;
				}
			} catch (Exception ex) {
				Log.e(TAG, "Exception while trying to register with TMEIT.", ex);
				return null;
			}

			mPrefs.setGcmRegistrationId(registrationId);
			broadcastRegistrationComplete();
			return null;
		}

		@Override
		protected void onPostExecute(Void ignored) {
			super.onPostExecute(ignored);
			mIsBusy = false;
		}
	}

	private static final class TmeitServiceHelper {
		public boolean registerGcm(String registrationId, String serviceAuth, String authenticatedUser) throws IOException, JSONException {
			return execute("RegisterGcm.php", registrationId, serviceAuth, authenticatedUser);
		}

		public boolean unregisterGcm(String registrationId, String serviceAuth, String authenticatedUser) throws IOException, JSONException {
			return execute("UnregisterGcm.php", registrationId, serviceAuth, authenticatedUser);
		}

		private String createJsonForRegisterGcm(String registrationId, String serviceAuth, String authenticatedUser) throws JSONException {
			JSONObject json = new JSONObject();
			json.put(TmeitServiceConfig.REGISTRATION_ID_KEY, registrationId);
			json.put(TmeitServiceConfig.SERVICE_AUTH_KEY, serviceAuth);
			json.put(TmeitServiceConfig.USERNAME_KEY, authenticatedUser);
			return json.toString();
		}

		private boolean execute(String relativeUrl, String registrationId, String serviceAuth, String authenticatedUser) throws IOException, JSONException {
			Request request = new Request.Builder()
				.url(TmeitServiceConfig.SERVICE_BASE_URL + relativeUrl)
				.post(RequestBody.create(TmeitServiceConfig.JSON_MEDIA_TYPE, createJsonForRegisterGcm(registrationId, serviceAuth, authenticatedUser)))
				.build();

			Response response = TmeitHttpClient.getInstance().newCall(request).execute();
			JSONObject responseBody = TmeitServiceConfig.getJsonBody(response, TAG);

			if (null == responseBody) {
				Log.e(TAG, "Got empty response from registration request.");
				return false;
			} else if (HttpURLConnection.HTTP_OK == response.code() && TmeitServiceConfig.isSuccessful(responseBody)) {
				return true;
			} else {
				String errorMessage = TmeitServiceConfig.getErrorMessage(responseBody);
				Log.e(TAG, "Protocol error in registration response. Error message = " + errorMessage);
				return false;
			}
		}
	}

	private final class UnregisterTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			Log.i(TAG, "Unregistering from Google Cloud Messaging.");

			String registrationId = mPrefs.getGcmRegistrationId();
			mPrefs.setGcmRegistrationId("");

			try {
				InstanceID instanceID = InstanceID.getInstance(mContext);
				instanceID.deleteToken(mContext.getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE);
			} catch (Exception ex) {
				Log.e(TAG, "Failed to unregister from Google Cloud Messaging.", ex);
				return null;
			}

			Log.v(TAG, "Successfully unregistered from Google Cloud Messaging.");

			try {
				String authenticatedUser = mPrefs.getAuthenticatedUserName();
				String serviceAuth = mPrefs.getServiceAuthentication();
				if (!mTmeit.unregisterGcm(registrationId, serviceAuth, authenticatedUser)) {
					return null;
				}
			} catch (Exception ex) {
				Log.e(TAG, "Exception while trying to unregister from TMEIT.", ex);
				return null;
			}

			broadcastRegistrationComplete();
			return null;
		}

		@Override
		protected void onPostExecute(Void ignored) {
			super.onPostExecute(ignored);
			mIsBusy = false;
		}
	}
}
