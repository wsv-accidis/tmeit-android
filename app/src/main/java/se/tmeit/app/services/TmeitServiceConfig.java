package se.tmeit.app.services;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Constants and utility methods for working the TMEIT web services.
 */
public final class TmeitServiceConfig {
	private static final String ERROR_MESSAGE_KEY = "errorMessage";
    public static final String IMAGE_BASE64_KEY = "image_base64";
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
	public static final String ROOT_URL = "https://tmeit.se/";
    public static final String SERVICE_AUTH_KEY = "serviceAuth";
    public static final String SERVICE_BASE_URL = ROOT_URL + "w/tmeit-ws/";
    private static final String SUCCESS_KEY = "success";
    public static final String UPLOAD_TO_KEY = "upload_to";
    public static final String USERNAME_KEY = "username";
    public static final String USER_ID_KEY = "id";

    private TmeitServiceConfig() {
    }

    public static String getErrorMessage(JSONObject body) {
        return body.optString(TmeitServiceConfig.ERROR_MESSAGE_KEY);
    }

    public static JSONObject getJsonBody(Response response, String logTag) {
        if (null == response || null == response.body()) {
            return null;
        }

        String str = null;
        try {
            str = response.body().string();
            return new JSONObject(str);

        } catch (JSONException ex) {
            Log.e(logTag, "Error parsing JSON from body of response.", ex);
            Log.d(logTag, "Response = " + str);
            return null;
        } catch (IOException ex) {
            Log.e(logTag, "Error reading response.", ex);
            return null;
        }
    }

    public static boolean isSuccessful(JSONObject body) {
        return body.optBoolean(TmeitServiceConfig.SUCCESS_KEY);
    }
}
