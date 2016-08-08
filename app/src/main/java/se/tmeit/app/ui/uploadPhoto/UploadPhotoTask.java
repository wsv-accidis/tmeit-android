package se.tmeit.app.ui.uploadPhoto;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import se.tmeit.app.services.TmeitHttpClient;
import se.tmeit.app.services.TmeitServiceConfig;
import se.tmeit.app.storage.Preferences;

/**
 * Asynchronous task for uploading a photo.
 */
public final class UploadPhotoTask extends AsyncTask<Void, Void, Boolean> {
	private static final int BUFFER_SIZE = 8192;
	private static final String TAG = UploadPhotoTask.class.getSimpleName();
	private final Context mContext;
	private final UploadPhotoResultListener mResultListener;
	private final Uri mSourceUri;
	private final String mUsername;
	private long mStartTime;

	public UploadPhotoTask(Context context, String username, Uri sourceUri, UploadPhotoResultListener resultListener) {
		mContext = context;
		mSourceUri = sourceUri;
		mUsername = username;
		mResultListener = resultListener;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			mStartTime = System.currentTimeMillis();
			String imageBase64 = encodeImageAsBase64();
			return uploadPhoto(imageBase64);
		} catch (Exception ignored) {
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (null != mResultListener) {
			if (result) {
				mResultListener.onSuccess();
			} else {
				mResultListener.onFailure();
			}
		}
	}

	private String createJsonForUploadPhoto(String uploadTo, String imageBase64, String serviceAuth, String authenticatedUser) throws JSONException {
		JSONObject json = new JSONObject();
		json.put(TmeitServiceConfig.SERVICE_AUTH_KEY, serviceAuth);
		json.put(TmeitServiceConfig.USERNAME_KEY, authenticatedUser);
		json.put(TmeitServiceConfig.UPLOAD_TO_KEY, uploadTo);
		json.put(TmeitServiceConfig.IMAGE_BASE64_KEY, imageBase64);
		return json.toString();
	}

	private String encodeImageAsBase64() throws Exception {
		InputStream inputStream = null;
		try {
			inputStream = mContext.getContentResolver().openInputStream(mSourceUri);
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			Base64OutputStream base64OutputStream = new Base64OutputStream(byteOutputStream, Base64.NO_WRAP);

			byte[] buffer = new byte[BUFFER_SIZE];
			int numBytes;
			while (-1 != (numBytes = inputStream.read(buffer))) {
				base64OutputStream.write(buffer, 0, numBytes);
			}

			base64OutputStream.close();
			return byteOutputStream.toString();

		} catch (FileNotFoundException ex) {
			Log.e(TAG, "Could not read photo because the URI is invalid, sourceUri = \"" + mSourceUri + "\".", ex);
			throw ex;
		} catch (Exception ex) {
			Log.e(TAG, "Could not read photo because an unexpected exception occurred.", ex);
			throw ex;
		} finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	private boolean uploadPhoto(String imageBase64) throws Exception {
		try {
			Preferences prefs = new Preferences(mContext);
			String authenticatedUser = prefs.getAuthenticatedUserName();
			String serviceAuth = prefs.getServiceAuthentication();

			Request request = new Request.Builder()
				.url(TmeitServiceConfig.SERVICE_BASE_URL + "UploadMemberPhoto.php")
				.post(RequestBody.create(TmeitServiceConfig.JSON_MEDIA_TYPE, createJsonForUploadPhoto(mUsername, imageBase64, serviceAuth, authenticatedUser)))
				.build();

			Response response = TmeitHttpClient.getInstance().newCall(request).execute();
			JSONObject responseBody = TmeitServiceConfig.getJsonBody(response, TAG);

			if (null == responseBody) {
				Log.e(TAG, "Got empty response from photo upload request.");
				return false;
			} else if (HttpURLConnection.HTTP_CREATED == response.code() && TmeitServiceConfig.isSuccessful(responseBody)) {
				long timer = System.currentTimeMillis() - mStartTime;
				Log.i(TAG, "Upload photo was completed successfully in " + timer + " ms.");
				return true;
			} else {
				String errorMessage = TmeitServiceConfig.getErrorMessage(responseBody);
				Log.e(TAG, "Protocol error in photo upload response. Error message = " + errorMessage);
				return false;
			}
		} catch (IOException ex) {
			Log.e(TAG, "Could not upload photo because an unexpected I/O exception occurred.", ex);
			throw ex;
		} catch (Exception ex) {
			Log.e(TAG, "Could not upload photo because an unexpected exception occurred.", ex);
			throw ex;
		}
	}

	public interface UploadPhotoResultListener {
		void onFailure();

		void onSuccess();
	}
}