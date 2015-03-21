package se.tmeit.app.model;

import org.json.JSONObject;

/**
 * Model object for members.
 */
public final class Member {
    private static final String ID = "id";
    private static final String REAL_NAME = "realname";
    private final int mId;
    private final String mRealName;

    public Member(int id, String realName) {
        mId = id;
        mRealName = realName;
    }

    public static Member fromJson(JSONObject obj) {
        int id = obj.optInt(ID);
        String realName = obj.optString(REAL_NAME);
        return new Member(id, realName);
    }

    public int getId() {
        return mId;
    }

    public String getRealName() {
        return mRealName;
    }

    @Override
    public String toString() {
        return getRealName();
    }
}
