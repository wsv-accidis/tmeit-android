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

    public static List<InternalEventWorker> filterByWorking(List<InternalEventWorker> list, Working filter) {
        ArrayList<InternalEventWorker> result = new ArrayList<>();
        for (InternalEventWorker worker : list) {
            if (worker.getWorking() == filter) {
                result.add(worker);
            }
        }

        return result;
    }

    public static InternalEventWorker fromJson(JSONObject json) {
        InternalEventWorker worker = new InternalEventWorker();
        worker.setName(json.optString(Keys.NAME));
        worker.setId(json.optInt(Keys.ID));
        worker.setWorking(Working.fromInt(json.optInt(Keys.WORKING)));
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
        public static final String ID = "id";
        public static final String NAME = "realname";
        public static final String WORKING = "working";

        private Keys() {
        }
    }
}
