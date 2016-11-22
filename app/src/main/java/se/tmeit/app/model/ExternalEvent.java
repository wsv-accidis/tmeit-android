package se.tmeit.app.model;

import com.google.auto.value.AutoValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Model object for external events.
 */
@AutoValue
public abstract class ExternalEvent {
	private static ExternalEvent create(
		String body,
		String externalUrl,
		int id,
		String lastSignupDate,
		int numberOfAttendees,
		String startDate,
		String title,
		boolean isAttending,
		boolean isNearSignup,
		boolean isPast,
		boolean isPastSignup
	) {
		return new AutoValue_ExternalEvent(
			body,
			externalUrl,
			id,
			lastSignupDate,
			numberOfAttendees,
			startDate,
			title,
			isAttending,
			isNearSignup,
			isPast,
			isPastSignup
		);
	}

	public static ExternalEvent fromJson(JSONObject obj) throws JSONException {
		return create(
			obj.optString(Keys.BODY, ""),
			obj.optString(Keys.EXTERNAL_URL, ""),
			obj.getInt(Keys.ID),
			obj.getString(Keys.LAST_SIGNUP),
			obj.optInt(Keys.ATTENDEES),
			obj.getString(Keys.START_DATE),
			obj.getString(Keys.TITLE),
			obj.optBoolean(Keys.IS_ATTENDING),
			obj.optBoolean(Keys.IS_NEAR_SIGNUP),
			obj.getBoolean(Keys.IS_PAST),
			obj.getBoolean(Keys.IS_PAST_SIGNUP)
		);
	}

	public abstract String body();

	public abstract String externalUrl();

	public abstract int id();

	public abstract String lastSignupDate();

	public abstract int numberOfAttendees();

	public abstract String startDate();

	public abstract String title();

	public abstract boolean isAttending();

	public abstract boolean isNearSignup();

	public abstract boolean isPast();

	public abstract boolean isPastSignup();


	public static class Keys {
		private static final String ATTENDEES = "attendees";
		private static final String BODY = "body";
		private static final String EXTERNAL_URL = "external_url";
		public static final String ID = "id";
		public static final String IS_ATTENDING = "is_attending";
		private static final String IS_NEAR_SIGNUP = "is_near_signup";
		private static final String IS_PAST = "is_past";
		private static final String IS_PAST_SIGNUP = "is_past_signup";
		public static final String LAST_SIGNUP = "last_signup";
		public static final String START_DATE = "start_date";
		public static final String TITLE = "title";

		private Keys() {
		}
	}

	public static class RepositoryData {
		private final List<ExternalEventAttendee> mAttendees;
		private final ExternalEventAttendee mCurrentAttendee;
		private final ExternalEvent mEvent;

		public RepositoryData(ExternalEvent externalEvent, ExternalEventAttendee currentAttendee, List<ExternalEventAttendee> attendees) {
			mEvent = externalEvent;
			mCurrentAttendee = currentAttendee;
			mAttendees = attendees;
		}

		public List<ExternalEventAttendee> getAttendees() {
			return mAttendees;
		}

		public ExternalEventAttendee getCurrentAttendee() {
			return mCurrentAttendee;
		}

		public ExternalEvent getEvent() {
			return mEvent;
		}

		public boolean isUserAttending(int userId) {
			for (ExternalEventAttendee attendee : mAttendees) {
				if (userId == attendee.id()) {
					return true;
				}
			}

			return false;
		}
	}
}
