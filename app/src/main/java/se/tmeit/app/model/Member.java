package se.tmeit.app.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model object for members.
 */
public final class Member {
    private static final String EMAIL = "email";
    private static final String FACES = "faces";
    private static final String GROUP_ID = "group_id";
    private static final String ID = "id";
    private static final String PHONE = "phone";
    private static final String REAL_NAME = "realname";
    private static final String TEAM_ID = "team_id";
    private static final String TITLE_ID = "title_id";
    private static final String USERNAME = "username";
    private String mEmail;
    private List<String> mFaces;
    private int mGroupId;
    private int mId;
    private String mPhone;
    private String mRealName;
    private int mTeamId;
    private int mTitleId;
    private String mUsername;

    public static Member fromJson(JSONObject obj) throws JSONException {
        Member member = new Member();
        member.setEmail(obj.getString(EMAIL));
        member.setGroupId(obj.optInt(GROUP_ID));
        member.setId(obj.getInt(ID));
        member.setPhone(obj.getString(PHONE));
        member.setRealName(obj.getString(REAL_NAME));
        member.setTeamId(obj.optInt(TEAM_ID));
        member.setTitleId(obj.optInt(TITLE_ID));
        member.setUsername(obj.getString(USERNAME));

        ArrayList<String> faces = new ArrayList<>();
        JSONArray jsonFaces = obj.optJSONArray(FACES);
        if (null != jsonFaces) {
            for (int i = 0; i < jsonFaces.length(); i++) {
                faces.add(jsonFaces.getString(i));
            }
        }
        member.setFaces(faces);

        return member;
    }

    public String getEmail() {
        return mEmail;
    }

    private void setEmail(String email) {
        mEmail = email;
    }

    public List<String> getFaces() {
        return mFaces;
    }

    private void setFaces(List<String> faces) {
        mFaces = faces;
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

    public int getTitleId() {
        return mTitleId;
    }

    private void setTitleId(int titleId) {
        mTitleId = titleId;
    }

    public String getUsername() {
        return mUsername;
    }

    private void setUsername(String username) {
        mUsername = username;
    }

    @Override
    public String toString() {
        return getRealName();
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
