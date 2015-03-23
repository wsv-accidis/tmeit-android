package se.tmeit.app.services;

import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;

/**
 * Downloads data entities from TMEIT web services.
 */
public final class Repository {
    private static final String HEADER_SERVICE_AUTH = "X-TMEIT-Service-Auth";
    private static final String HEADER_USERNAME = "X-TMEIT-Username";
    private static final String TAG = Repository.class.getSimpleName();
    private final String mServiceAuth;
    private final String mUsername;

    public Repository(String username, String serviceAuth) {
        mUsername = username;
        mServiceAuth = serviceAuth;
    }

    public void getMembers(RepositoryResultHandler<List<Member>> resultHandler) {
        Request request = getRequestBuilder("GetMembers.php").build();
        HttpClient.enqueueRequest(request, new GetMembersCallback(resultHandler));
    }

    private Request.Builder getRequestBuilder(String relativeUrl) {
        return new Request.Builder()
                .url(TmeitServiceConfig.BASE_URL + relativeUrl)
                .addHeader(HEADER_USERNAME, mUsername)
                .addHeader(HEADER_SERVICE_AUTH, mServiceAuth)
                .get();
    }

    public static interface RepositoryResultHandler<TResult> {
        public void onError(int errorMessage);

        public void onSuccess(TResult result);
    }

    private final class GetMembersCallback extends GetResultCallback<List<Member>> {
        private static final String USERS = "users";

        public GetMembersCallback(RepositoryResultHandler<List<Member>> resultHandler) {
            super(resultHandler);
        }

        @Override
        protected List<Member> getResult(JSONObject responseBody) throws JSONException {
            JSONArray jsonUsers = responseBody.getJSONArray(USERS);

            ArrayList<Member> members = new ArrayList<>();
            for (int i = 0; i < jsonUsers.length(); i++) {
                JSONObject jsonUser = jsonUsers.getJSONObject(i);
                members.add(Member.fromJson(jsonUser));
            }

            return members;
        }
    }

    private abstract class GetResultCallback<TResult> implements Callback {
        protected final RepositoryResultHandler<TResult> mResultHandler;

        protected GetResultCallback(RepositoryResultHandler<TResult> resultHandler) {
            mResultHandler = resultHandler;
        }

        @Override
        public void onFailure(Request request, IOException e) {
            Log.e(TAG, "Downloading data failed due to an IO error.", e);
            mResultHandler.onError(R.string.repository_error_unspecified_network);
        }

        @Override
        public void onResponse(Response response) throws IOException {
            Log.i(TAG, "Download response received with HTTP status = " + response.code() + ", cached = " + (null == response.networkResponse()) + ".");

            try {
                JSONObject responseBody = TmeitServiceConfig.getJsonBody(response, TAG);
                if (null != responseBody) {
                    TResult result = getResult(responseBody);
                    mResultHandler.onSuccess(result);
                } else {
                    mResultHandler.onError(R.string.repository_error_unspecified_protocol);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Downloading data failed due to a JSON error.", e);
                mResultHandler.onError(R.string.repository_error_unspecified_protocol);
            }
        }

        protected abstract TResult getResult(JSONObject responseBody) throws JSONException;
    }
}
