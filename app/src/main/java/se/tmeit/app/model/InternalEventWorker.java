package se.tmeit.app.model;

import com.google.auto.value.AutoValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Model objects for workers at an internal event.
 */
@AutoValue
public abstract class InternalEventWorker {
	public final static int RANGE_MAX_HOUR = 29;
	public final static int RANGE_MIN_HOUR = 8;
	public final static int MAX_COMMENT_LENGTH = 200;

	public static Builder builder() {
		return new AutoValue_InternalEventWorker.Builder()
			.setId(0)
			.setName("")
			.setGroupTitle("")
			.setTeamTitle("")
			.setRangeStart(0)
			.setRangeEnd(0);
	}

	public static List<InternalEventWorker> filterByWorking(List<InternalEventWorker> list, Working filter) {
		ArrayList<InternalEventWorker> result = new ArrayList<>();
		for (InternalEventWorker worker : list) {
			if (worker.working() == filter) {
				result.add(worker);
			}
		}

		return result;
	}

	public static InternalEventWorker fromJson(JSONObject json) throws JSONException {
		InternalEventWorker.Builder builder = builder()
			.setComment(json.optString(Keys.COMMENT.substring(0, MAX_COMMENT_LENGTH), ""))
			.setGroupTitle(json.optString(Keys.GROUP_TITLE, ""))
			.setId(json.optInt(Keys.ID))
			.setName(json.optString(Keys.NAME, ""))
			.setTeamTitle(json.optString(Keys.TEAM_TITLE, ""))
			.setWorking(Working.fromInt(json.optInt(Keys.WORKING)));

		if (json.optBoolean(Keys.HAS_RANGE)) {
			builder.setRangeStart(json.getInt(Keys.RANGE_START)).setRangeEnd(json.getInt(Keys.RANGE_END));
		}

		return builder.build();
	}

	public static List<InternalEventWorker> fromJsonArray(JSONArray json) throws JSONException {
		ArrayList<InternalEventWorker> result = new ArrayList<>(json.length());
		for (int i = 0; i < json.length(); i++) {
			result.add(fromJson(json.getJSONObject(i)));
		}

		return result;
	}

	public abstract String comment();

	public abstract String groupTitle();

	public abstract int id();

	public abstract String name();

	public abstract int rangeEnd();

	public abstract int rangeStart();

	public abstract String teamTitle();

	public abstract Working working();

	public boolean hasRange() {
		return 0 != rangeStart() && 0 != rangeEnd();
	}

	@AutoValue.Builder
	public abstract static class Builder {
		public abstract Builder setComment(String value);

		abstract Builder setGroupTitle(String value);

		abstract Builder setId(int value);

		abstract Builder setName(String value);

		public abstract Builder setRangeEnd(int value);

		public abstract Builder setRangeStart(int value);

		abstract Builder setTeamTitle(String value);

		public abstract Builder setWorking(Working value);

		public abstract InternalEventWorker build();
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

		public static int toInt(Working w) {
			switch (w) {
				case YES:
					return 2;
				case MAYBE:
					return 1;
				default:
					return 0;
			}
		}
	}

	public static class Keys {
		public static final String COMMENT = "comment";
		private static final String GROUP_TITLE = "group_title";
		private static final String HAS_RANGE = "has_range";
		public static final String ID = "id";
		public static final String IS_SAVED = "is_saved"; // bundle only
		private static final String NAME = "realname";
		public static final String RANGE_END = "range_end";
		public static final String RANGE_START = "range_start";
		private static final String TEAM_TITLE = "team_title";
		public static final String WORKING = "working";

		private Keys() {
		}
	}
}
