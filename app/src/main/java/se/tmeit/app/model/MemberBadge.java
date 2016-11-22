package se.tmeit.app.model;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model object for a member's badge.
 */
@AutoValue
public abstract class MemberBadge implements Parcelable {
	private static MemberBadge create(String title, String src) {
		return new AutoValue_MemberBadge(title, src);
	}

	public static MemberBadge fromJson(JSONObject obj) throws JSONException {
		return create(obj.getString(Keys.TITLE), obj.getString(Keys.SRC));
	}

	public abstract String title();

	public abstract String src();

	private static class Keys {
		private static final String SRC = "src";
		private static final String TITLE = "title";

		private Keys() {
		}
	}
}
