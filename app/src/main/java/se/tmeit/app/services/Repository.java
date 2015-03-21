package se.tmeit.app.services;

import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.tmeit.app.model.Member;

/**
 * Downloads data entities from TMEIT web services.
 */
public final class Repository {
    private static final String TAG = Repository.class.getSimpleName();
    private final String mServiceAuth;
    private final String mUsername;

    public Repository(String username, String serviceAuth) {
        mUsername = username;
        mServiceAuth = serviceAuth;
    }

    public void getMembers(RepositoryResultHandler<List<Member>> resultHandler) {
        try {
            Request request = new Request.Builder()
                    .url(TmeitServiceConfig.BASE_URL + "GetMembers.php")
                    .post(RequestBody.create(TmeitServiceConfig.JSON_MEDIA_TYPE, createJson()))
                    .build();

            HttpClient.enqueueRequest(request, new GetMembersCallback(resultHandler));

        } catch (Exception ex) {
            // If we end up here, there's probably a bug - most normal error conditions would end up in the async failure handler instead
            Log.e(TAG, "Unexpected exception while downloading the list of members.", ex);
            resultHandler.onError(0); // TODO
        }
    }

    private String createJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(TmeitServiceConfig.SERVICE_AUTH_KEY, mServiceAuth);
        json.put(TmeitServiceConfig.USERNAME_KEY, mUsername);
        return json.toString();
    }

    public static interface RepositoryResultHandler<T> {
        public void onError(int errorMessage);

        public void onSuccess(T result);
    }

    private final class GetMembersCallback implements Callback {
        private static final String USERS = "users";
        private final RepositoryResultHandler<List<Member>> mResultHandler;

        public GetMembersCallback(RepositoryResultHandler<List<Member>> resultHandler) {
            mResultHandler = resultHandler;
        }

        @Override
        public void onFailure(Request request, IOException e) {
            Log.e(TAG, "Downloading the list of members failed due to an IO error.", e);
            mResultHandler.onError(0); // TODO
        }

        @Override
        public void onResponse(Response response) throws IOException {
            Log.i(TAG, "List of members response received with HTTP status = " + response.code());

            try {
                JSONObject responseBody = TmeitServiceConfig.getJsonBody(response, TAG);
                if (null != responseBody) {
                    JSONArray jsonUsers = responseBody.getJSONArray(USERS);

                    ArrayList<Member> members = new ArrayList<>();
                    for (int i = 0; i < jsonUsers.length(); i++) {
                        JSONObject jsonUser = jsonUsers.getJSONObject(i);
                        members.add(Member.fromJson(jsonUser));
                    }

                    mResultHandler.onSuccess(members);
                } else {
                    mResultHandler.onError(0); // TODO (same as JSONException)
                }
            } catch (JSONException e) {
                Log.e(TAG, "Downloading the list of members failed due to a JSON error.", e);
                mResultHandler.onError(0); // TODO
            }
        }
    }
}
