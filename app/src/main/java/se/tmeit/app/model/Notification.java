package se.tmeit.app.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

import se.tmeit.app.utils.DateTimeUtils;

/**
 * Model object for notifications.
 */
@AutoValue
public abstract class Notification {
	private static Notification create(int id, String body, String url, Calendar created) {
		return new AutoValue_Notification(id, body, url, created);
	}

	public static Notification fromJson(JSONObject obj) throws JSONException {
		return create(
			obj.getInt(Keys.ID),
			obj.getString(Keys.BODY),
			obj.optString(Keys.URL, ""),
			DateTimeUtils.parseIso8601(obj.optString(Keys.CREATED)));
	}

	public abstract int id();

	public abstract String body();

	public abstract String url();

	@Nullable
	public abstract Calendar created();

	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(Keys.ID, id());
		json.put(Keys.BODY, body());
		json.put(Keys.URL, url());
		json.put(Keys.CREATED, DateTimeUtils.formatIso8601(created()));
		return json;
	}

	@NonNull
	@Override
	public String toString() {
		return String.format(Locale.getDefault(), "[%d] %s (%s)", id(), body(), DateTimeUtils.formatIso8601(created()));
	}

	private static class Keys {
		private static final String BODY = "body";
		private static final String CREATED = "created";
		private static final String ID = "id";
		private static final String URL = "url";

		private Keys() {
		}
	}
}
