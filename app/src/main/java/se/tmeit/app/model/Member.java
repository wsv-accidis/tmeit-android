package se.tmeit.app.model;

import android.content.Context;

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
public final class Member {
	private String mDateMarskalk;
	private String mDatePrao;
	private String mDateVraq;
	private String mEmail;
	private List<MemberBadge> mExperienceBadges;
	private int mExperiencePoints;
	private List<String> mFaces;
	private long mFlags;
	private int mGroupId;
	private int mId;
	private String mPhone;
	private String mRealName;
	private String mSearchText;
	private int mTeamId;
	private int mTitleId;
	private String mUsername;

	public static Member fromJson(JSONObject obj) throws JSONException {
		Member member = new Member();
		member.setDateMarskalk(obj.optString(Keys.DATE_MARSKALK));
		member.setDatePrao(obj.optString(Keys.DATE_PRAO));
		member.setDateVraq(obj.optString(Keys.DATE_VRAQ));
		member.setEmail(obj.getString(Keys.EMAIL));
		member.setExperiencePoints(obj.optInt(Keys.EXPERIENCE_POINTS));
		member.setFlags(obj.optBoolean(Keys.HAS_STAD), obj.optBoolean(Keys.HAS_FEST), obj.optBoolean(Keys.HAS_PERMIT), obj.optBoolean(Keys.HAS_LICENSE));
		member.setGroupId(obj.optInt(Keys.GROUP_ID));
		member.setId(obj.getInt(Keys.ID));
		member.setPhone(obj.getString(Keys.PHONE));
		member.setRealName(obj.getString(Keys.REAL_NAME));
		member.setTeamId(obj.optInt(Keys.TEAM_ID));
		member.setTitleId(obj.optInt(Keys.TITLE_ID));
		member.setUsername(obj.getString(Keys.USERNAME));

		ArrayList<String> faces = new ArrayList<>();
		JSONArray jsonFaces = obj.optJSONArray(Keys.FACES);
		if (null != jsonFaces) {
			for (int i = 0; i < jsonFaces.length(); i++) {
				faces.add(jsonFaces.getString(i));
			}
		}
		member.setFaces(faces);

		ArrayList<MemberBadge> badges = new ArrayList<>();
		JSONArray jsonBadges = obj.optJSONArray(Keys.EXPERIENCE_BADGES);
		if (null != jsonBadges) {
			for (int i = 0; i < jsonBadges.length(); i++) {
				badges.add(MemberBadge.fromJson(jsonBadges.getJSONObject(i)));
			}
		}
		member.setExperienceBadges(badges);

		member.initSearchText();
		return member;
	}

	public String getDateMarskalk() {
		return mDateMarskalk;
	}

	private void setDateMarskalk(String date) {
		mDateMarskalk = date;
	}

	public String getDatePrao() {
		return mDatePrao;
	}

	private void setDatePrao(String date) {
		mDatePrao = date;
	}

	public String getDateVraq() {
		return mDateVraq;
	}

	private void setDateVraq(String date) {
		mDateVraq = date;
	}

	public String getEmail() {
		return mEmail;
	}

	private void setEmail(String email) {
		mEmail = email;
	}

	public List<MemberBadge> getExperienceBadges() {
		return mExperienceBadges;
	}

	private void setExperienceBadges(List<MemberBadge> badges) {
		mExperienceBadges = badges;
	}

	public int getExperiencePoints() {
		return mExperiencePoints;
	}

	private void setExperiencePoints(int value) {
		mExperiencePoints = value;
	}

	public List<String> getFaces() {
		return mFaces;
	}

	private void setFaces(List<String> faces) {
		mFaces = faces;
	}

	public long getFlags() {
		return mFlags;
	}

	public int getGroupId() {
		return mGroupId;
	}

	private void setGroupId(int groupId) {
		mGroupId = groupId;
	}

	public int getId() {
		return mId;
	}

	private void setId(int id) {
		mId = id;
	}

	public String getPhone() {
		return mPhone;
	}

	private void setPhone(String phone) {
		mPhone = phone;
	}

	public String getRealName() {
		return mRealName;
	}

	private void setRealName(String realName) {
		mRealName = realName;
	}

	public int getTeamId() {
		return mTeamId;
	}

	private void setTeamId(int teamId) {
		mTeamId = teamId;
	}

	public String getTeamText(Context context, RepositoryData repositoryData) {
		if (getTeamId() > 0) {
			return repositoryData.getTeams().get(getTeamId());
		} else {
			return context.getString(R.string.members_no_team_placeholder);
		}
	}

	public int getTitleId() {
		return mTitleId;
	}

	private void setTitleId(int titleId) {
		mTitleId = titleId;
	}

	public String getTitleText(Context context, RepositoryData repositoryData) {
		if (getTitleId() > 0) {
			return repositoryData.getTitles().get(getTitleId());
		} else if (getGroupId() > 0) {
			return repositoryData.getGroups().get(getGroupId());
		} else {
			return context.getString(R.string.members_no_title_placeholder);
		}
	}

	public String getUsername() {
		return mUsername;
	}

	private void setUsername(String username) {
		mUsername = username;
	}

	public boolean hasFlag(Flags flag) {
		return (0 != (mFlags & flag.getValue()));
	}

	public boolean matches(CharSequence search) {
		return mSearchText.contains(search);
	}

	@Override
	public String toString() {
		return getRealName();
	}

	private void initSearchText() {
		StringBuilder builder = new StringBuilder();
		builder.append(mRealName.toLowerCase());
		builder.append(' ');
		builder.append(mUsername.toLowerCase());
		builder.append(' ');
		builder.append(stripPhoneNumberChars(mPhone));
		builder.append(' ');
		builder.append(mEmail.toLowerCase());
		mSearchText = builder.toString();
	}

	private void setFlags(boolean hasStad, boolean hasFest, boolean onPermit, boolean driversLicense) {
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

		mFlags = flags;
	}

	private static CharSequence stripPhoneNumberChars(String phoneNo) {
		return phoneNo.replace("-", "").replace(" ", "");
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
		public static final String GROUP_ID = "group_id";
		public static final String HAS_FEST = "has_fest";
		public static final String HAS_LICENSE = "has_license";
		public static final String HAS_PERMIT = "has_permit";
		public static final String HAS_STAD = "has_stad";
		public static final String ID = "id";
		public static final String PHONE = "phone";
		public static final String REAL_NAME = "realname";
		public static final String TEAM_ID = "team_id";
		public static final String TEAM_TEXT = "team_text"; // bundle only
		public static final String TITLE_ID = "title_id";
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

		public Map<Integer, String> getTitles() {
			return mTitles;
		}
	}
}
