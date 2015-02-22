package se.tmeit.app.notifications;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import se.tmeit.app.storage.Preferences;

/**
 * Handles registering/unregistering with GCM and the TMEIT backend to receive notifications.
 */
public final class GcmRegistration {
    private static final String TAG = GcmRegistration.class.getSimpleName();
    private final Context mContext;
    private final Preferences mPrefs;
    private static GcmRegistration mInstance;
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
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            resultHandler.onGoogleServicesError(resultCode, GooglePlayServicesUtil.isUserRecoverableError(resultCode));
            return false;
        }
        return true;
    }

    public static interface RegistrationResultHandler {
        public void onGoogleServicesError(int resultCode, boolean canRecover);
    }

    private final class RegisterTask extends AsyncTask<Void, Void, Void> {
        private final RegistrationResultHandler mResultHandler;

        public RegisterTask(RegistrationResultHandler resultHandler) {
            mResultHandler = resultHandler;
        }

        @Override
        protected Void doInBackground(Void... params) {


            return null;
        }

        @Override
        protected void onPostExecute(Void ignored) {
            super.onPostExecute(ignored);
            mIsBusy = false;
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
            }

            // TODO Inform TMEIT backend
            return null;
        }

        @Override
        protected void onPostExecute(Void ignored) {
            super.onPostExecute(ignored);
            mIsBusy = false;
        }
    }
}
