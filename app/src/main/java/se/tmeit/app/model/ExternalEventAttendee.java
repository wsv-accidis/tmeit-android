package se.tmeit.app.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Model object for attendees of an external event.
 */
public final class ExternalEventAttendee {
    private String mDob;
    private String mDrinkPrefs;
    private String mFoodPrefs;
    private String mName;
    private String mNotes;
    private int mId;

    public static ExternalEventAttendee fromJson(JSONObject json) {
        ExternalEventAttendee attendee = new ExternalEventAttendee();
        attendee.setDateOfBirth(json.optString(Keys.DOB));
        attendee.setDrinkPreferences(json.optString(Keys.DRINK_PREFS));
        attendee.setFoodPreferences(json.optString(Keys.FOOD_PREFS));
        attendee.setName(json.optString(Keys.NAME));
        attendee.setNotes(json.optString(Keys.NOTES));
        attendee.setId(json.optInt(Keys.ID));
        return attendee;
    }

    public static List<ExternalEventAttendee> fromJsonArray(JSONArray json) throws JSONException {
        ArrayList<ExternalEventAttendee> result = new ArrayList<>(json.length());
        for (int i = 0; i < json.length(); i++) {
            result.add(fromJson(json.getJSONObject(i)));
        }

        return result;
    }

    public String getDateOfBirth() {
        return mDob;
    }

    public void setDateOfBirth(String dob) {
        mDob = dob;
    }

    public String getDrinkPreferences() {
        return mDrinkPrefs;
    }

    public void setDrinkPreferences(String drink) {
        mDrinkPrefs = drink;
    }

    public String getFoodPreferences() {
        return mFoodPrefs;
    }

    public void setFoodPreferences(String food) {
        mFoodPrefs = food;
    }

    public String getName() {
        return mName;
    }

    private void setName(String name) {
        mName = name;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }

    public int getId() {
        return mId;
    }

    private void setId(int id) {
        mId = id;
    }

    public static class Keys {
        public static final String DOB = "dob";
        public static final String DRINK_PREFS = "drink_prefs";
        public static final String FOOD_PREFS = "food_prefs";
        public static final String ID = "id";
        public static final String NAME = "user_name";
        public static final String NOTES = "notes";

        private Keys() {
        }
    }
}
