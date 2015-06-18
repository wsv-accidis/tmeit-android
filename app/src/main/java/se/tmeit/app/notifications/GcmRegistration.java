package se.tmeit.app.notifications;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import se.tmeit.app.R;
import se.tmeit.app.services.TmeitHttpClient;
import se.tmeit.app.services.TmeitServiceConfig;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.utils.AndroidUtils;

/**
 * Handles registering/unregistering with GCM and the TMEIT backend to receive notifications.
 */
public final class GcmRegistration {
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
        if (mPrefs.hasGcmRegistrationId()) {
            int registeredAppVersion = mPrefs.getGcmRegistrationVersion();
            int actualAppVersion = AndroidUtils.getAppVersionCode(mContext);
            return (registeredAppVersion == actualAppVersion);
        } else {
            return false;
        }
    }

    public void register(RegistrationResultHandler resultHandler) {
        if (mIsBusy) {
            Log.w(TAG, "Register called while another task is in progress. The call was dropped.");
            return;
        }

        if (checkForGooglePlayServices(resultHandler)) {
            mIsBusy = true;
            RegisterTask task = new RegisterTask(resultHandler);
            task.execute();
        }
    }

    public void registerIfRegistrationExpired(RegistrationResultHandler resultHandler) {
        if (isRegistered() || !mPrefs.hasGcmRegistrationId()) {
            return;
        }

        // If we have a registration ID but isRegistered is false, then we need to reregister because the ID has expired
        Log.w(TAG, "Registration has expired. Will try to re-register automatically.");
        register(resultHandler);
    }

    public void unregister(RegistrationResultHandler resultHandler) {
        if (mIsBusy) {
            Log.w(TAG, "Unregister called while another task is in progress. The call was dropped.");
            return;
        }

        mIsBusy = true;
        UnregisterTask task = new UnregisterTask(resultHandler);
        task.execute();
    }

    private boolean checkForGooglePlayServices(RegistrationResultHandler resultHandler) {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            resultHandler.onGoogleServicesError(resultCode, GoogleApiAvailability.getInstance().isUserResolvableError(resultCode));
            return false;
        }
        return true;
    }

    public static interface RegistrationResultHandler {
        public void onError(int errorMessage);

        public void onGoogleServicesError(int resultCode, boolean canRecover);

        public void onSuccess();
    }

    private final class RegisterTask extends AsyncTask<Void, Void, Void> {
        private final RegistrationResultHandler mResultHandler;

        public RegisterTask(RegistrationResultHandler resultHandler) {
            mResultHandler = resultHandler;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, "Registering with Google Cloud Messaging.");

            // Clear old registration data, so we're not left registered if this fails
            mPrefs.setGcmRegistrationId("");
            mPrefs.setGcmRegistrationVersion(0);

            String registrationId;
            try {
                String senderId = mContext.getString(R.string.gcm_sender_id);
                registrationId = GoogleCloudMessaging.getInstance(mContext).register(senderId);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to register with Google Cloud Messaging.", ex);
                mResultHandler.onError(R.string.notifications_error_unspecified_gcm);
                return null;
            }

            Log.v(TAG, "Successfully registered with Google Cloud Messaging. RegistrationId = " + registrationId + ".");

            boolean tmeitSuccessful;
            try {
                String authenticatedUser = mPrefs.getAuthenticatedUserName();
                String serviceAuth = mPrefs.getServiceAuthentication();
                tmeitSuccessful = mTmeit.registerGcm(registrationId, serviceAuth, authenticatedUser);
            } catch (Exception ex) {
                Log.e(TAG, "Exception while trying to register with TMEIT.", ex);
                tmeitSuccessful = false;
            }

            if (!tmeitSuccessful) {
                mResultHandler.onError(R.string.notifications_error_unspecified_tmeit);
                return null;
            }

            int appVersion = AndroidUtils.getAppVersionCode(mContext);
            Log.d(TAG, "Registered successfully! RegistrationId = " + registrationId + ", appVersion = " + appVersion + ".");
            mPrefs.setGcmRegistrationId(registrationId);
            mPrefs.setGcmRegistrationVersion(appVersion);

            mResultHandler.onSuccess();
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

            Response response = TmeitHttpClient.getInstance().executeRequest(request);
            JSONObject responseBody = TmeitServiceConfig.getJsonBody(response, TAG);

            if (null == responseBody) {
                Log.e(TAG, "Got empty response from registration request.");
                return false;
            } else if (HttpStatus.SC_OK == response.code() && TmeitServiceConfig.isSuccessful(responseBody)) {
                return true;
            } else {
                String errorMessage = TmeitServiceConfig.getErrorMessage(responseBody);
                Log.e(TAG, "Protocol error in registration response. Error message = " + errorMessage);
                return false;
            }
        }
    }

    private final class UnregisterTask extends AsyncTask<Void, Void, Void> {
        private final RegistrationResultHandler mResultHandler;

        public UnregisterTask(RegistrationResultHandler resultHandler) {
            mResultHandler = resultHandler;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, "Unregistering from Google Cloud Messaging.");

            String registrationId = mPrefs.getGcmRegistrationId();
            mPrefs.setGcmRegistrationId("");
            mPrefs.setGcmRegistrationVersion(0);

            try {
                GoogleCloudMessaging.getInstance(mContext).unregister();
            } catch (Exception ex) {
                Log.e(TAG, "Failed to unregister from Google Cloud Messaging.", ex);
                mResultHandler.onError(R.string.notifications_error_unspecified_gcm);
                return null;
            }

            Log.v(TAG, "Successfully unregistered from Google Cloud Messaging.");

            boolean tmeitSuccessful;
            try {
                String authenticatedUser = mPrefs.getAuthenticatedUserName();
                String serviceAuth = mPrefs.getServiceAuthentication();
                tmeitSuccessful = mTmeit.unregisterGcm(registrationId, serviceAuth, authenticatedUser);
            } catch (Exception ex) {
                Log.e(TAG, "Exception while trying to unregister from TMEIT.", ex);
                tmeitSuccessful = false;
            }

            if (!tmeitSuccessful) {
                mResultHandler.onError(R.string.notifications_error_unspecified_tmeit);
                return null;
            }

            Log.d(TAG, "Unregistered successfully!");
            mResultHandler.onSuccess();
            return null;
        }

        @Override
        protected void onPostExecute(Void ignored) {
            super.onPostExecute(ignored);
            mIsBusy = false;
        }
    }
}
