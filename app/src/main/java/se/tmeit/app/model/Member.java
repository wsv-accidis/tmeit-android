package se.tmeit.app.model;

import android.content.Context;

import com.google.auto.value.AutoValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import se.tmeit.app.R;

/**
 * Model object for members.
 */
@AutoValue
public abstract class Member {
	public static Builder builder() {
		return new AutoValue_Member.Builder();
	}

	public static Member fromJson(JSONObject obj) throws JSONException {
		Member.Builder builder = builder()
			.setDateMarskalk(obj.optString(Keys.DATE_MARSKALK, ""))
			.setDatePrao(obj.optString(Keys.DATE_PRAO, ""))
			.setDateVraq(obj.optString(Keys.DATE_VRAQ, ""))
			.setEmail(obj.getString(Keys.EMAIL))
			.setExperiencePoints(obj.optInt(Keys.EXPERIENCE_POINTS))
			.setFlags(
				obj.optBoolean(Keys.HAS_STAD),
				obj.optBoolean(Keys.HAS_FEST),
				obj.optBoolean(Keys.HAS_PERMIT),
				obj.optBoolean(Keys.HAS_LICENSE))
			.setGroupId(obj.optInt(Keys.GROUP_ID))
			.setId(obj.getInt(Keys.ID))
			.setPhone(obj.optString(Keys.PHONE, ""))
			.setRealName(obj.getString(Keys.REAL_NAME))
			.setTeamId(obj.optInt(Keys.TEAM_ID))
			.setTitleId(obj.optInt(Keys.TITLE_ID))
			.setUsername(obj.getString(Keys.USERNAME));

		ArrayList<String> faces = new ArrayList<>();
		JSONArray jsonFaces = obj.optJSONArray(Keys.FACES);
		if (null != jsonFaces) {
			for (int i = 0; i < jsonFaces.length(); i++) {
				faces.add(jsonFaces.getString(i));
			}
		}
		builder.setFaces(faces);

		ArrayList<MemberBadge> badges = new ArrayList<>();
		JSONArray jsonBadges = obj.optJSONArray(Keys.EXPERIENCE_BADGES);
		if (null != jsonBadges) {
			for (int i = 0; i < jsonBadges.length(); i++) {
				badges.add(MemberBadge.fromJson(jsonBadges.getJSONObject(i)));
			}
		}
		builder.setExperienceBadges(badges);

		return builder.build();
	}

	public abstract String dateMarskalk();

	public abstract String datePrao();

	public abstract String dateVraq();

	public abstract String email();

	public abstract List<MemberBadge> experienceBadges();

	public abstract int experiencePoints();

	public abstract List<String> faces();

	public abstract long flags();

	public abstract int groupId();

	public abstract int id();

	public abstract String phone();

	public abstract String realName();

	abstract String searchText();

	public abstract int teamId();

	public String teamText(Context context, RepositoryData repositoryData) {
		if (teamId() > 0) {
			return repositoryData.getTeams().get(teamId());
		} else {
			return context.getString(R.string.members_no_team_placeholder);
		}
	}

	public abstract int titleId();

	public String titleText(Context context, RepositoryData repositoryData) {
		if (titleId() > 0) {
			return repositoryData.getTitles().get(titleId());
		} else if (groupId() > 0) {
			return repositoryData.getGroups().get(groupId());
		} else {
			return context.getString(R.string.members_no_title_placeholder);
		}
	}

	public abstract String username();

	public boolean hasFlag(Flags flag) {
		return (0 != (flags() & flag.getValue()));
	}

	public boolean matches(CharSequence search) {
		return (null == search || searchText().contains(search));
	}

	@AutoValue.Builder
	abstract static class Builder {
		abstract Member autoBuild();

		private Member build() {
			setSearchText(createSearchText());
			return autoBuild();
		}

		abstract String email();

		abstract String phone();

		abstract String realName();

		abstract Builder setDateMarskalk(String value);

		abstract Builder setDatePrao(String value);

		abstract Builder setDateVraq(String value);

		abstract Builder setEmail(String value);

		abstract Builder setExperienceBadges(List<MemberBadge> value);

		abstract Builder setExperiencePoints(int value);

		abstract Builder setFaces(List<String> value);

		private Builder setFlags(boolean hasStad, boolean hasFest, boolean onPermit, boolean driversLicense) {
			long flags = 0;
			if (hasStad) {
				flags |= Flags.HAS_STAD.getValue();
			}
			if (hasFest) {
				flags |= Flags.HAS_FEST.getValue();
			}
			if (onPermit) {
				flags |= Flags.ON_PERMIT.getValue();
			}
			if (driversLicense) {
				flags |= Flags.DRIVERS_LICENSE.getValue();
			}

			return setFlags(flags);
		}

		abstract Builder setFlags(long value);

		abstract Builder setGroupId(int value);

		abstract Builder setId(int value);

		abstract Builder setPhone(String value);

		abstract Builder setRealName(String value);

		abstract Builder setSearchText(String value);

		abstract Builder setTeamId(int value);

		abstract Builder setTitleId(int value);

		abstract Builder setUsername(String value);

		abstract String username();

		private String createSearchText() {
			return realName().toLowerCase() + ' ' + username().toLowerCase() + ' ' + stripPhoneNumberChars(phone()) + ' ' + email().toLowerCase();
		}

		private static CharSequence stripPhoneNumberChars(String phoneNo) {
			return phoneNo.replace("-", "").replace(" ", "");
		}
	}

	public enum Flags {
		HAS_STAD(1),
		HAS_FEST(1 << 1),
		ON_PERMIT(1 << 2),
		DRIVERS_LICENSE(1 << 3);

		private final long mValue;

		Flags(long value) {
			mValue = value;
		}

		public long getValue() {
			return mValue;
		}
	}

	public static class Keys {
		public static final String DATE_MARSKALK = "date_marskalk";
		public static final String DATE_PRAO = "date_prao";
		public static final String DATE_VRAQ = "date_vraq";
		public static final String EMAIL = "email";
		public static final String EXPERIENCE_BADGES = "experience_badges";
		public static final String EXPERIENCE_POINTS = "experience_points";
		public static final String FACES = "faces";
		public static final String FLAGS = "flags"; // bundle only
		private static final String GROUP_ID = "group_id";
		private static final String HAS_FEST = "has_fest";
		private static final String HAS_LICENSE = "has_license";
		private static final String HAS_PERMIT = "has_permit";
		private static final String HAS_STAD = "has_stad";
		public static final String ID = "id";
		public static final String PHONE = "phone";
		public static final String REAL_NAME = "realname";
		private static final String TEAM_ID = "team_id";
		public static final String TEAM_TEXT = "team_text"; // bundle only
		private static final String TITLE_ID = "title_id";
		public static final String TITLE_TEXT = "title_text"; // bundle only
		public static final String USERNAME = "username";

		private Keys() {
		}
	}

	public static class RepositoryData {
		private final Map<Integer, String> mGroups;
		private final List<Member> mMembers;
		private final Map<Integer, String> mTeams;
		private final Map<Integer, String> mTitles;

		public RepositoryData(List<Member> members, Map<Integer, String> groups, Map<Integer, String> teams, Map<Integer, String> titles) {
			mMembers = members;
			mGroups = groups;
			mTeams = teams;
			mTitles = titles;
		}

		public static RepositoryData empty() {
			return new RepositoryData(Collections.<Member>emptyList(), Collections.<Integer, String>emptyMap(), Collections.<Integer, String>emptyMap(), Collections.<Integer, String>emptyMap());
		}

		public Map<Integer, String> getGroups() {
			return mGroups;
		}

		public List<Member> getMembers() {
			return mMembers;
		}

		public Map<Integer, String> getTeams() {
			return mTeams;
		}

		private Map<Integer, String> getTitles() {
			return mTitles;
		}
	}
}
