package se.tmeit.app.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Model objects for workers at an internal event.
 */
public final class InternalEventWorker {
    private int mId;
    private String mName;
    private Working mWorking;
    private String mComment;
    private boolean mHasRange;
    private int mRangeStart;
    private int mRangeEnd;

    public static List<InternalEventWorker> filterByWorking(List<InternalEventWorker> list, Working filter) {
        ArrayList<InternalEventWorker> result = new ArrayList<>();
        for (InternalEventWorker worker : list) {
            if (worker.getWorking() == filter) {
                result.add(worker);
            }
        }

        return result;
    }

    public static InternalEventWorker fromJson(JSONObject json) throws JSONException {
        InternalEventWorker worker = new InternalEventWorker();
        worker.setComment(json.optString(Keys.COMMENT));
        worker.setId(json.optInt(Keys.ID));
        worker.setName(json.optString(Keys.NAME));
        worker.setWorking(Working.fromInt(json.optInt(Keys.WORKING)));

        if (json.optBoolean(Keys.HAS_RANGE)) {
            worker.setRange(json.getInt(Keys.RANGE_START), json.getInt(Keys.RANGE_END));
        }

        return worker;
    }

    public static List<InternalEventWorker> fromJsonArray(JSONArray json) throws JSONException {
        ArrayList<InternalEventWorker> result = new ArrayList<>(json.length());
        for (int i = 0; i < json.length(); i++) {
            result.add(fromJson(json.getJSONObject(i)));
        }

        return result;
    }

    public int getId() {
        return mId;
    }

    private void setId(int id) {
        mId = id;
    }

    public String getComment() {
        return mComment;
    }

    private void setComment(String comment) {
        mComment = comment;
    }

    public String getName() {
        return mName;
    }

    private void setName(String name) {
        mName = name;
    }

    public Working getWorking() {
        return mWorking;
    }

    private void setWorking(Working working) {
        mWorking = working;
    }

    public boolean hasRange() {
        return mHasRange;
    }

    public int getRangeStart() {
        return mRangeStart;
    }

    public int getRangeEnd() {
        return mRangeEnd;
    }

    public void setRange(int start, int end) {
        mRangeStart = start;
        mRangeEnd = end;
        mHasRange = true;
    }

    public enum Working {
        YES, NO, MAYBE;

        public static Working fromInt(int i) {
            switch (i) {
                case 2:
                    return YES;
                case 1:
                    return MAYBE;
                default:
                    return NO;
            }
        }
    }

    public static class Keys {
        public static final String COMMENT = "comment";
        public static final String HAS_RANGE = "has_range";
        public static final String ID = "id";
        public static final String NAME = "realname";
        public static final String RANGE_START = "range_start";
        public static final String RANGE_END = "range_end";
        public static final String WORKING = "working";

        private Keys() {
        }
    }
}
