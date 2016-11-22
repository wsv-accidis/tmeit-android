package se.tmeit.app.model;

import com.google.auto.value.AutoValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Model object for attendees of an external event.
 */
@AutoValue
public abstract class ExternalEventAttendee {
	public static ExternalEventAttendee fromJson(JSONObject json) throws JSONException {
		return builder()
			.setDateOfBirth(json.optString(Keys.DOB, ""))
			.setDrinkPreferences(json.optString(Keys.DRINK_PREFS, ""))
			.setFoodPreferences(json.optString(Keys.FOOD_PREFS, ""))
			.setName(json.optString(Keys.NAME, ""))
			.setNotes(json.optString(Keys.NOTES, ""))
			.setId(json.optInt(Keys.ID))
			.build();
	}

	public static List<ExternalEventAttendee> fromJsonArray(JSONArray json) throws JSONException {
		ArrayList<ExternalEventAttendee> result = new ArrayList<>(json.length());
		for (int i = 0; i < json.length(); i++) {
			result.add(fromJson(json.getJSONObject(i)));
		}

		return result;
	}

	public static Builder builder() {
		return new AutoValue_ExternalEventAttendee.Builder()
			.setId(0)
			.setName("");
	}

	public abstract String dateOfBirth();

	public abstract String drinkPreferences();

	public abstract String foodPreferences();

	public abstract String name();

	public abstract String notes();

	public abstract int id();

	@AutoValue.Builder
	public abstract static class Builder {
		public abstract Builder setDateOfBirth(String value);

		public abstract Builder setDrinkPreferences(String value);

		public abstract Builder setFoodPreferences(String value);

		abstract Builder setName(String value);

		public abstract Builder setNotes(String value);

		abstract Builder setId(int id);

		public abstract ExternalEventAttendee build();
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
