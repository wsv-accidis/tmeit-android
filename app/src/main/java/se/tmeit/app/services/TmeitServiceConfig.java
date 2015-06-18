package se.tmeit.app.services;

import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Constants and utility methods for working the TMEIT web services.
 */
public final class TmeitServiceConfig {
    public static final String ERROR_MESSAGE_KEY = "errorMessage";
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    public static final String MAX_COUNT_KEY = "maxCount";
    public static final String NEW_SINCE_KEY = "newSince";
    public static final String REGISTRATION_ID_KEY = "registrationId";
    public static final String ROOT_URL = "https://tmeit-se.loopiasecure.com/";
    public static final String SERVICE_BASE_URL = ROOT_URL + "w/tmeit-ws/";
    public static final String ROOT_URL_INSECURE = "http://tmeit.se/";
    public static final String SERVICE_AUTH_KEY = "serviceAuth";
    public static final String SUCCESS_KEY = "success";
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
