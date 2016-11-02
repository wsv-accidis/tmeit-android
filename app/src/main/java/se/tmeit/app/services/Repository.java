package se.tmeit.app.services;

import android.util.Log;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import se.tmeit.app.R;
import se.tmeit.app.model.ExternalEvent;
import se.tmeit.app.model.ExternalEventAttendee;
import se.tmeit.app.model.InternalEvent;
import se.tmeit.app.model.InternalEventWorker;
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

    public void attendExternalEvent(int id, ExternalEventAttendee attendee, RepositoryResultHandler<Void> resultHandler) {
        try {
            Request request = new Request.Builder()
                    .url(TmeitServiceConfig.SERVICE_BASE_URL + "AttendExternalEvent.php")
                    .post(RequestBody.create(TmeitServiceConfig.JSON_MEDIA_TYPE, createJsonForAttendExternalEvent(id, attendee)))
                    .build();

            enqueueRequest(request, new AttendExternalEventCallback(resultHandler));
        } catch (JSONException ex) {
            Log.e(TAG, "Unexpected JSON exception while creating request.", ex);
            resultHandler.onError(R.string.network_error_unspecified_protocol);
        }
    }

    public void getExternalEventDetails(int id, boolean noCache, RepositoryResultHandler<ExternalEvent.RepositoryData> resultHandler) {
        Request.Builder requestBuilder = getRequestBuilder("GetExternalEventDetails.php/" + id);
        if (noCache) {
            requestBuilder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        enqueueRequest(requestBuilder.build(), new GetExternalEventDetailsCallback(resultHandler));
    }

    public void getExternalEvents(RepositoryResultHandler<List<ExternalEvent>> resultHandler, boolean noCache) {
        Request.Builder requestBuilder = getRequestBuilder("GetExternalEvents.php");
        if (noCache) {
            requestBuilder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        enqueueRequest(requestBuilder.build(), new GetExternalEventsCallback(resultHandler));
    }

    public void getInternalEventDetails(int id, boolean noCache, RepositoryResultHandler<InternalEvent.RepositoryData> resultHandler) {
        Request.Builder requestBuilder = getRequestBuilder("GetEventDetails.php/" + id);
        if (noCache) {
            requestBuilder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        enqueueRequest(requestBuilder.build(), new GetInternalEventDetailsCallback(resultHandler));
    }

    public void getInternalEvents(RepositoryResultHandler<List<InternalEvent>> resultHandler, boolean noCache) {
        Request.Builder requestBuilder = getRequestBuilder("GetEvents.php");
        if (noCache) {
            requestBuilder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        enqueueRequest(requestBuilder.build(), new GetInternalEventsCallback(resultHandler));
    }

    public void getMembers(RepositoryResultHandler<Member.RepositoryData> resultHandler, boolean noCache) {
        Request.Builder requestBuilder = getRequestBuilder("GetMembers.php");
        if (noCache) {
            requestBuilder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        enqueueRequest(requestBuilder.build(), new GetMembersCallback(resultHandler));
    }

    public void workInternalEvent(int id, InternalEventWorker worker, RepositoryResultHandler<Void> resultHandler) {
        try {
            Request request = new Request.Builder()
                    .url(TmeitServiceConfig.SERVICE_BASE_URL + "WorkEvent.php")
                    .post(RequestBody.create(TmeitServiceConfig.JSON_MEDIA_TYPE, createJsonForWorkInternalEvent(id, worker)))
                    .build();

            enqueueRequest(request, new AttendExternalEventCallback(resultHandler));
        } catch (JSONException ex) {
            Log.e(TAG, "Unexpected JSON exception while creating request.", ex);
            resultHandler.onError(R.string.network_error_unspecified_protocol);
        }
    }

    private Map<Integer, String> deserializeIdTitleMap(JSONArray jsonArray) throws JSONException {
        Map<Integer, String> result = new LinkedHashMap<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            result.put(obj.getInt(Keys.ID), obj.getString(Keys.TITLE));
        }
        return result;
    }

    private String createJsonForAttendExternalEvent(int id, ExternalEventAttendee attendee) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(TmeitServiceConfig.USERNAME_KEY, mUsername);
        json.put(TmeitServiceConfig.SERVICE_AUTH_KEY, mServiceAuth);
        json.put(Keys.EVENT_ID, id);

        if (null != attendee) {
            JSONObject attending = new JSONObject();
            attending.put(Keys.DOB, attendee.getDateOfBirth());
            attending.put(Keys.DRINK_PREFS, attendee.getDrinkPreferences());
            attending.put(Keys.FOOD_PREFS, attendee.getFoodPreferences());
            attending.put(Keys.NOTES, attendee.getNotes());
            json.put(Keys.ATTENDING, attending);
        }

        return json.toString();
    }

    private String createJsonForWorkInternalEvent(int id, InternalEventWorker worker) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(TmeitServiceConfig.USERNAME_KEY, mUsername);
        json.put(TmeitServiceConfig.SERVICE_AUTH_KEY, mServiceAuth);
        json.put(Keys.EVENT_ID, id);
        json.put(Keys.COMMENT, worker.getComment());
        json.put(Keys.WORKING, InternalEventWorker.Working.toInt(worker.getWorking()));

        if (worker.hasRange()) {
            json.put(Keys.RANGE_START, worker.getRangeStart());
            json.put(Keys.RANGE_END, worker.getRangeEnd());
        }

        return json.toString();
    }

	private Call enqueueRequest(Request request, Callback callback) {
		Call call = TmeitHttpClient.getInstance().newCall(request);
		call.enqueue(callback);
		return call;
	}

    private Request.Builder getRequestBuilder(String relativeUrl) {
        return new Request.Builder()
                .url(TmeitServiceConfig.SERVICE_BASE_URL + relativeUrl)
                .addHeader(HEADER_USERNAME, mUsername)
                .addHeader(HEADER_SERVICE_AUTH, mServiceAuth);
    }

    private final class AttendExternalEventCallback extends GetResultCallback<Void> {
        private AttendExternalEventCallback(RepositoryResultHandler<Void> resultHandler) {
            super(resultHandler);
        }

        @Override
        protected Void getResult(JSONObject responseBody) throws JSONException {
            return null;
        }
    }

    private final class GetExternalEventDetailsCallback extends GetResultCallback<ExternalEvent.RepositoryData> {
		private GetExternalEventDetailsCallback(RepositoryResultHandler<ExternalEvent.RepositoryData> resultHandler) {
            super(resultHandler);
        }

        @Override
        protected ExternalEvent.RepositoryData getResult(JSONObject responseBody) throws JSONException {
            JSONObject jsonEvent = responseBody.getJSONObject(Keys.EVENT);
            JSONObject jsonAttendee = responseBody.getJSONObject(Keys.ATTENDEE);
            JSONArray jsonAttendees = responseBody.getJSONArray(Keys.ATTENDEES);
            return new ExternalEvent.RepositoryData(ExternalEvent.fromJson(jsonEvent),
                    ExternalEventAttendee.fromJson(jsonAttendee),
                    ExternalEventAttendee.fromJsonArray(jsonAttendees));
        }
    }

    private final class GetExternalEventsCallback extends GetResultCallback<List<ExternalEvent>> {
        private static final String EVENTS = "events";

		private GetExternalEventsCallback(RepositoryResultHandler<List<ExternalEvent>> resultHandler) {
            super(resultHandler);
        }

        @Override
        protected List<ExternalEvent> getResult(JSONObject responseBody) throws JSONException {
            JSONArray jsonArray = responseBody.getJSONArray(EVENTS);

            ArrayList<ExternalEvent> events = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                events.add(ExternalEvent.fromJson(json));
            }

            return events;
        }
    }

    private final class GetInternalEventDetailsCallback extends GetResultCallback<InternalEvent.RepositoryData> {
		private GetInternalEventDetailsCallback(RepositoryResultHandler<InternalEvent.RepositoryData> resultHandler) {
            super(resultHandler);
        }

        @Override
        protected InternalEvent.RepositoryData getResult(JSONObject responseBody) throws JSONException {
            JSONObject jsonEvent = responseBody.getJSONObject(Keys.EVENT);
            JSONArray jsonWorkers = responseBody.getJSONArray(Keys.WORKERS);
            return new InternalEvent.RepositoryData(InternalEvent.fromJson(jsonEvent),
                    InternalEventWorker.fromJsonArray(jsonWorkers));
        }
    }

    private final class GetInternalEventsCallback extends GetResultCallback<List<InternalEvent>> {
        private static final String EVENTS = "events";

		private GetInternalEventsCallback(RepositoryResultHandler<List<InternalEvent>> resultHandler) {
            super(resultHandler);
        }

        @Override
        protected List<InternalEvent> getResult(JSONObject responseBody) throws JSONException {
            JSONArray jsonArray = responseBody.getJSONArray(EVENTS);

            ArrayList<InternalEvent> events = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                events.add(InternalEvent.fromJson(json));
            }

            return events;
        }
    }

    private final class GetMembersCallback extends GetResultCallback<Member.RepositoryData> {
		private GetMembersCallback(RepositoryResultHandler<Member.RepositoryData> resultHandler) {
            super(resultHandler);
        }

        @Override
        protected Member.RepositoryData getResult(JSONObject responseBody) throws JSONException {
            Map<Integer, String> groups = deserializeIdTitleMap(responseBody.getJSONArray(Keys.GROUPS));
            Map<Integer, String> teams = deserializeIdTitleMap(responseBody.getJSONArray(Keys.TEAMS));
            Map<Integer, String> titles = deserializeIdTitleMap(responseBody.getJSONArray(Keys.TITLES));

            JSONArray jsonArray = responseBody.getJSONArray(Keys.USERS);

            ArrayList<Member> members = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                members.add(Member.fromJson(json));
            }

            return new Member.RepositoryData(members, groups, teams, titles);
        }
    }

    private abstract class GetResultCallback<TResult> implements Callback {
		private final RepositoryResultHandler<TResult> mResultHandler;

        protected GetResultCallback(RepositoryResultHandler<TResult> resultHandler) {
            mResultHandler = resultHandler;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            Log.e(TAG, "Downloading data failed due to an IO error.", e);
            mResultHandler.onError(R.string.repository_error_unspecified_network);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Log.i(TAG, "Download response received with HTTP status = " + response.code() + ", cached = " + (null == response.networkResponse()) + ".");

            if (!response.isSuccessful()) {
                Log.e(TAG, "Downloading data failed because an unsuccessful HTTP status code (" + response.code() + ") was returned.");
                mResultHandler.onError(R.string.repository_error_unspecified_protocol);
                return;
            }

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

    private static class Keys {
        public static final String ATTENDEE = "attendee";
        public static final String ATTENDEES = "attendees";
		private static final String ATTENDING = "attending";
		private static final String COMMENT = "comment";
		private static final String DOB = "dob";
		private static final String DRINK_PREFS = "drink_prefs";
        public static final String EVENT = "event";
		private static final String EVENT_ID = "event_id";
		private static final String FOOD_PREFS = "food_prefs";
        public static final String GROUPS = "groups";
        public static final String ID = "id";
		private static final String NOTES = "notes";
		private static final String RANGE_END = "range_end";
		private static final String RANGE_START = "range_start";
        public static final String TEAMS = "teams";
		private static final String TITLE = "title";
        public static final String TITLES = "titles";
        public static final String USERS = "users";
        public static final String WORKERS = "workers";
		private static final String WORKING = "working";

        private Keys() {
        }
    }
}
