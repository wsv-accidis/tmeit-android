package se.tmeit.app.services;

import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

        try {
            Request request = new Request.Builder()
                    .url(TmeitServiceConfig.BASE_URL + "ValidateAuth.php")
                    .post(RequestBody.create(TmeitServiceConfig.JSON_MEDIA_TYPE, createJsonForValidateAuth(username, serviceAuth)))
                    .build();

            HttpClient.enqueueRequest(request, new AuthenticateFromQrCallback(resultHandler, serviceAuth, username));

        } catch (Exception ex) {
            // If we end up here, there's probably a bug - most normal error conditions would end up in the async failure handler instead
            Log.e(TAG, "Unexpected exception while authenticating.", ex);
            resultHandler.onProtocolError(R.string.auth_error_unspecified_protocol);
        }
    }

    private static boolean checkSanity(String qrCode) {
        // Expected format is {username 3-16 chars)%{service auth 48-96 chars}

        // This is mainly to ensure that someone doesn't go scanning random QR codes off the web,
        // we don't want to waste resources sending codes to the server that will never actually
        // validate to anything. The actual validation happens on the server side, so the fact
        // that it would be trivial to construct a QR code that passes the sanity checking is
        // not a security concern.

        if (null == qrCode || qrCode.isEmpty()) {
            Log.d(TAG, "Failed sanity checks: QR code is null or empty.");
            return false;
        }

        int indexOfSeparator = qrCode.indexOf(SERVICE_AUTH_SEPARATOR);
        if (-1 == indexOfSeparator || indexOfSeparator < USERNAME_LENGTH_MIN || indexOfSeparator >= USERNAME_LENGTH_MAX) {
            Log.d(TAG, "Failed sanity checks: Separator not found or not in expected range.");
            return false;
        }

        int lengthOfServiceAuth = qrCode.length() - indexOfSeparator;
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

    public static interface AuthenticationResultHandler {
        public void onAuthenticationError(int errorMessage);

        public void onNetworkError(int errorMessage);

        public void onProtocolError(int errorMessage);

        public void onSuccess(String serviceAuth, String authenticatedUser);
    }

    private static class AuthenticateFromQrCallback implements Callback {
        private final AuthenticationResultHandler mResultHandler;
        private final String mServiceAuth;
        private final String mUsername;

        public AuthenticateFromQrCallback(AuthenticationResultHandler resultHandler, String serviceAuth, String username) {
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
            Log.i(TAG, "Authentication response received with HTTP status = " + response.code());

            JSONObject responseBody = TmeitServiceConfig.getJsonBody(response, TAG);
            if (null == responseBody) {
                Log.e(TAG, "Got empty response from authentication request.");
                mResultHandler.onProtocolError(R.string.auth_error_unspecified_protocol);
            } else if (HttpStatus.SC_OK == response.code() && TmeitServiceConfig.isSuccessful(responseBody)) {
                mResultHandler.onSuccess(mServiceAuth, mUsername);
            } else if (HttpStatus.SC_FORBIDDEN == response.code()) {
                mResultHandler.onAuthenticationError(R.string.auth_error_code_denied);
            } else {
                String errorMessage = TmeitServiceConfig.getErrorMessage(responseBody);
                Log.e(TAG, "Protocol error in authentication response. Error message = " + errorMessage);
                mResultHandler.onProtocolError(R.string.auth_error_unspecified_protocol);
            }
        }
    }
}
