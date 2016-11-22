package se.tmeit.app.model;

import com.google.auto.value.AutoValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Model object for internal events.
 */
@AutoValue
public abstract class InternalEvent {
	private static InternalEvent create(
		int id,
		String location,
		String startDate,
		String startTime,
		int teamId,
		String teamTitle,
		String title,
		int workersCount,
		int workersMax,
		boolean isPast,
		boolean isReported
	) {
		return new AutoValue_InternalEvent(
			id,
			location,
			startDate,
			startTime,
			teamId,
			teamTitle,
			title,
			workersCount,
			workersMax,
			isPast,
			isReported);
	}

	public static InternalEvent fromJson(JSONObject obj) throws JSONException {
		return create(
			obj.getInt(Keys.ID),
			obj.getString(Keys.LOCATION),
			obj.optString(Keys.START_DATE, ""),
			obj.optString(Keys.START_TIME, ""),
			obj.getInt(Keys.TEAM_ID),
			obj.optString(Keys.TEAM_TITLE, ""),
			obj.getString(Keys.TITLE),
			obj.optInt(Keys.WORKERS_COUNT),
			obj.optInt(Keys.WORKERS_MAX),
			obj.getBoolean(Keys.IS_PAST),
			obj.optBoolean(Keys.IS_REPORTED));
	}

	public abstract int id();

	public abstract String location();

	public abstract String startDate();

	public abstract String startTime();

	public abstract int teamId();

	public abstract String teamTitle();

	public abstract String title();

	public abstract int workersCount();

	public abstract int workersMax();

	public abstract boolean isPast();

	public abstract boolean isReported();

	public static class Keys {
		public static final String ID = "id";
		private static final String IS_PAST = "is_past";
		private static final String IS_REPORTED = "is_reported";
		private static final String LOCATION = "location";
		public static final String START_DATE = "start_date";
		public static final String START_TIME = "start_time";
		private static final String TEAM_ID = "team_id";
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
