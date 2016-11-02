package se.tmeit.app.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model object for a member's badge.
 */
public final class MemberBadge implements Parcelable {
    public static Parcelable.Creator CREATOR = new Parcelable.Creator<MemberBadge>() {
        @Override
        public MemberBadge createFromParcel(Parcel source) {
            return new MemberBadge(source);
        }

        @Override
        public MemberBadge[] newArray(int size) {
            return new MemberBadge[size];
        }
    };
    private final String mSrc;
    private final String mTitle;

	private MemberBadge(String title, String src) {
        mTitle = title;
        mSrc = src;
    }

    private MemberBadge(Parcel parcel) {
        mTitle = parcel.readString();
        mSrc = parcel.readString();
    }

    public static MemberBadge fromJson(JSONObject obj) throws JSONException {
        String title = obj.getString(Keys.TITLE);
        String src = obj.getString(Keys.SRC);
        return new MemberBadge(title, src);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getSrc() {
        return mSrc;
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mSrc);
    }

    public static class Keys {
		private static final String SRC = "src";
		private static final String TITLE = "title";

        private Keys() {
        }
    }
}
