package se.tmeit.app.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model object for internal events.
 */
public class InternalEvent {
    private int mId;
    private int mWorkersCount;
    private int mWorkersMax;
    private int mTeamId;
    private boolean mIsPast;
    private boolean mIsReported;
    private String mTeamTitle;
    private String mStartDate;
    private String mStartTime;
    private String mLocation;
    private String mTitle;

    public static InternalEvent fromJson(JSONObject obj) throws JSONException {
        InternalEvent event = new InternalEvent();

        event.setId(obj.getInt(Keys.ID));
        event.setWorkersCount(obj.getInt(Keys.WORKERS_COUNT));
        event.setmWorkersMax(obj.getInt(Keys.WORKERS_MAX));
        event.setTeamId(obj.getInt(Keys.TEAM_ID));
        event.setPast(obj.getBoolean(Keys.IS_PAST));
        event.setReported(obj.getBoolean(Keys.IS_REPORTED));
        event.setTeamTitle(obj.getString(Keys.TEAM_TITLE));
        event.setStartDate(obj.getString(Keys.START_DATE));
        event.setStartTime(obj.getString(Keys.START_TIME));
        event.setLocation(obj.getString(Keys.LOCATION));
        event.setTitle(obj.getString(Keys.TITLE));

        return event;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getWorkersCount() {
        return mWorkersCount;
    }

    public void setWorkersCount(int value) {
        mWorkersCount = value;
    }

    public int getmWorkersMax() {
        return mWorkersMax;
    }

    public void setmWorkersMax(int value) {
        mWorkersMax = value;
    }

    public int getTeamId() {
        return mTeamId;
    }

    public void setTeamId(int value) {
        mTeamId = value;
    }

    public boolean isPast() {
        return mIsPast;
    }

    public void setPast(boolean value) {
        mIsPast = value;
    }

    public boolean isReported() {
        return mIsReported;
    }

    public void setReported(boolean value) {
        mIsReported = value;
    }

    public String getTeamTitle() {
        return mTeamTitle;
    }

    public void setTeamTitle(String value) {
        mTeamTitle = value;
    }

    public String getStartDate() {
        return mStartDate;
    }

    public void setStartDate(String date) {
        mStartDate = date;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String time) {
        mStartTime = time;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String value) {
        mLocation = value;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String value) {
        mTitle = value;
    }

    public static class Keys {
        public static final String TITLE = "title";
        public static final String ID = "id";
        public static final String LOCATION = "location";
        public static final String START_DATE = "start_date";
        public static final String START_TIME = "start_time";
        public static final String WORKERS_COUNT = "workers_count";
        public static final String WORKERS_MAX = "workers_max";
        public static final String TEAM_ID = "team_id";
        public static final String TEAM_TITLE = "team_title";
        public static final String IS_PAST = "is_past";
        public static final String IS_REPORTED = "is_reported";

        private Keys() {
        }
    }
}
