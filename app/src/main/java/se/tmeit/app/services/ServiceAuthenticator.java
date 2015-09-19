package se.tmeit.app.services;

import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import se.tmeit.app.R;

/**
 * Handles authentication of the service auth code against TMEIT web services.
 */
public final class ServiceAuthenticator {
    private static final int SERVICE_AUTH_LENGTH_MAX = 96;
    private static final int SERVICE_AUTH_LENGTH_MIN = 48;
    private static final char SERVICE_AUTH_SEPARATOR = '%';
    private static final String TAG = ServiceAuthenticator.class.getSimpleName();
    private static final int USERNAME_LENGTH_MAX = 16;
    private static final int USERNAME_LENGTH_MIN = 3;

    public void authenticateFromCode(String authCode, AuthenticationResultHandler resultHandler) {
        if (!checkSanity(authCode)) {
            resultHandler.onAuthenticationError(R.string.auth_error_code_not_valid);
            return;
        }

        int indexOfSeparator = authCode.indexOf(SERVICE_AUTH_SEPARATOR);
        String username = authCode.substring(0, indexOfSeparator).toLowerCase();
        String serviceAuth = authCode.substring(indexOfSeparator + 1);

        authenticate(username, serviceAuth, resultHandler);
    }

    public void authenticateFromCredentials(String username, String serviceAuth, AuthenticationResultHandler resultHandler) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(serviceAuth)) {
            resultHandler.onAuthenticationError(R.string.auth_error_invalid_data);
            return;
        }

        authenticate(username, serviceAuth, resultHandler);
    }

    private void authenticate(String username, String serviceAuth, AuthenticationResultHandler resultHandler) {
        try {
            Request request = new Request.Builder()
                    .url(TmeitServiceConfig.SERVICE_BASE_URL + "ValidateAuth.php")
                    .post(RequestBody.create(TmeitServiceConfig.JSON_MEDIA_TYPE, createJsonForValidateAuth(username, serviceAuth)))
                    .build();

            TmeitHttpClient.getInstance().enqueueRequest(request, new AuthenticationCallback(resultHandler, serviceAuth, username));

        } catch (JSONException ex) {
            // If we end up here, there's probably a bug - most normal error conditions would end up in the async failure handler instead
            Log.e(TAG, "Unexpected JSON exception while authenticating.", ex);
            resultHandler.onProtocolError(R.string.auth_error_unspecified_protocol);
        }
    }

    private static boolean checkSanity(String authCode) {
        // Expected format is {username 3-16 chars)%{service auth 48-96 chars}

        // The actual validation happens on the server side, so the fact that it would be trivial to construct
        // code that passes the sanity checking is not a security concern.

        if (null == authCode || authCode.isEmpty()) {
            Log.d(TAG, "Failed sanity checks: Code code is null or empty.");
            return false;
        }

        int indexOfSeparator = authCode.indexOf(SERVICE_AUTH_SEPARATOR);
        if (-1 == indexOfSeparator || indexOfSeparator < USERNAME_LENGTH_MIN || indexOfSeparator >= USERNAME_LENGTH_MAX) {
            Log.d(TAG, "Failed sanity checks: Separator not found or not in expected range.");
            return false;
        }

        int lengthOfServiceAuth = authCode.length() - indexOfSeparator;
        if (lengthOfServiceAuth < SERVICE_AUTH_LENGTH_MIN || lengthOfServiceAuth > SERVICE_AUTH_LENGTH_MAX) {
            Log.d(TAG, "Failed sanity checks: Service auth key length is not in expected range.");
            return false;
        }

        Log.d(TAG, "Passed sanity checks.");
        return true;
    }

    private static String createJsonForValidateAuth(String username, String serviceAuth) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(TmeitServiceConfig.SERVICE_AUTH_KEY, serviceAuth);
        json.put(TmeitServiceConfig.USERNAME_KEY, username);
        return json.toString();
    }

    private static class AuthenticationCallback implements Callback {
        private final AuthenticationResultHandler mResultHandler;
        private final String mServiceAuth;
        private final String mUsername;

        public AuthenticationCallback(AuthenticationResultHandler resultHandler, String serviceAuth, String username) {
            mResultHandler = resultHandler;
            mServiceAuth = serviceAuth;
            mUsername = username;
        }

        @Override
        public void onFailure(Request request, IOException e) {
            Log.e(TAG, "Authentication failed due to an IO error.", e);
            mResultHandler.onNetworkError(R.string.auth_error_unspecified_network);
        }

        @Override
        public void onResponse(Response response) throws IOException {
            Log.i(TAG, "Authentication response received with HTTP status = " + response.code() + ".");

            JSONObject responseBody = TmeitServiceConfig.getJsonBody(response, TAG);
            if (null == responseBody) {
                Log.e(TAG, "Got empty response from authentication request.");
                mResultHandler.onProtocolError(R.string.auth_error_unspecified_protocol);
            } else if (HttpURLConnection.HTTP_OK == response.code() && TmeitServiceConfig.isSuccessful(responseBody)) {
                int userId = responseBody.optInt(TmeitServiceConfig.USER_ID_KEY);
                mResultHandler.onSuccess(mServiceAuth, mUsername, userId);
            } else if (HttpURLConnection.HTTP_FORBIDDEN == response.code()) {
                mResultHandler.onAuthenticationError(R.string.auth_error_code_denied);
            } else {
                String errorMessage = TmeitServiceConfig.getErrorMessage(responseBody);
                Log.e(TAG, "Protocol error in authentication response. Error message = " + errorMessage);
                mResultHandler.onProtocolError(R.string.auth_error_unspecified_protocol);
            }
        }
    }
}
