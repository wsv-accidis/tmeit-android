package se.tmeit.app.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Model object for internal events.
 */
public final class InternalEvent {
    private int mId;
    private boolean mIsPast;
    private boolean mIsReported;
    private String mLocation;
    private String mStartDate;
    private String mStartTime;
    private int mTeamId;
    private String mTeamTitle;
    private String mTitle;
    private int mWorkersCount;
    private int mWorkersMax;

    public static InternalEvent fromJson(JSONObject obj) throws JSONException {
        InternalEvent event = new InternalEvent();
        event.setId(obj.getInt(Keys.ID));
        event.setLocation(obj.getString(Keys.LOCATION));
        event.setPast(obj.getBoolean(Keys.IS_PAST));
        event.setReported(obj.optBoolean(Keys.IS_REPORTED));
        event.setStartDate(obj.optString(Keys.START_DATE));
        event.setStartTime(obj.optString(Keys.START_TIME));
        event.setTeamId(obj.getInt(Keys.TEAM_ID));
        event.setTeamTitle(obj.optString(Keys.TEAM_TITLE));
        event.setTitle(obj.getString(Keys.TITLE));
        event.setWorkersCount(obj.optInt(Keys.WORKERS_COUNT));
        event.setWorkersMax(obj.optInt(Keys.WORKERS_MAX));
        return event;
    }

    public int getId() {
        return mId;
    }

    private void setId(int id) {
        mId = id;
    }

    public String getLocation() {
        return mLocation;
    }

    private void setLocation(String value) {
        mLocation = value;
    }

    public String getStartDate() {
        return mStartDate;
    }

    private void setStartDate(String date) {
        mStartDate = date;
    }

    public String getStartTime() {
        return mStartTime;
    }

    private void setStartTime(String time) {
        mStartTime = time;
    }

    public int getTeamId() {
        return mTeamId;
    }

    private void setTeamId(int value) {
        mTeamId = value;
    }

    public String getTeamTitle() {
        return mTeamTitle;
    }

    private void setTeamTitle(String value) {
        mTeamTitle = value;
    }

    public String getTitle() {
        return mTitle;
    }

    private void setTitle(String value) {
        mTitle = value;
    }

    public int getWorkersCount() {
        return mWorkersCount;
    }

    public void setWorkersCount(int value) {
        mWorkersCount = value;
    }

    public int getWorkersMax() {
        return mWorkersMax;
    }

    public void setWorkersMax(int value) {
        mWorkersMax = value;
    }

    public boolean isPast() {
        return mIsPast;
    }

    private void setPast(boolean value) {
        mIsPast = value;
    }

    public boolean isReported() {
        return mIsReported;
    }

    private void setReported(boolean value) {
        mIsReported = value;
    }

    public static class Keys {
        public static final String ID = "id";
        public static final String IS_PAST = "is_past";
        public static final String IS_REPORTED = "is_reported";
        public static final String LOCATION = "location";
        public static final String START_DATE = "start_date";
        public static final String START_TIME = "start_time";
        public static final String TEAM_ID = "team_id";
        public static final String TEAM_TITLE = "team_title";
        public static final String TITLE = "title";
        public static final String WORKERS_COUNT = "workers_count";
        public static final String WORKERS_MAX = "workers_max";

        private Keys() {
        }
    }

    public static class RepositoryData {
        private final InternalEvent mEvent;
        private final List<InternalEventWorker> mWorkers;

        public RepositoryData(InternalEvent internalEvent, List<InternalEventWorker> workers) {
            mEvent = internalEvent;
            mWorkers = workers;
        }

        public InternalEvent getEvent() {
            return mEvent;
        }

        public List<InternalEventWorker> getWorkers() {
            return mWorkers;
        }
    }
}
