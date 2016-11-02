package se.tmeit.app.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

import se.tmeit.app.utils.DateTimeUtils;

/**
 * Model object for notifications.
 */
public final class Notification {
    private static final String BODY = "body";
    private static final String CREATED = "created";
    private static final String ID = "id";
    private static final String URL = "url";
    private String mBody;
    private Calendar mCreated;
    private int mId;
    private String mUrl;

    public static Notification fromJson(JSONObject obj) {
        Notification notif = new Notification();
        notif.setId(obj.optInt(ID));
        notif.setBody(obj.optString(BODY));
        notif.setUrl(obj.optString(URL));
        notif.setCreated(DateTimeUtils.parseIso8601(obj.optString(CREATED)));
        return notif;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (null == o || !(o instanceof Notification)) {
            return false;
        }

        Notification other = (Notification) o;
        return other.mId == this.mId && TextUtils.equals(other.mBody, this.mBody) && TextUtils.equals(other.mUrl, this.mUrl)
                && ((null == mCreated && null == other.mCreated) || (mCreated != null && mCreated.equals(other.mCreated)));
    }

    public String getBody() {
        return mBody;
    }

	private void setBody(String body) {
        this.mBody = body;
    }

    public Calendar getCreated() {
        return mCreated;
    }

	private void setCreated(Calendar created) {
        this.mCreated = created;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

	private void setUrl(String url) {
        this.mUrl = url;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(ID, mId);
        json.put(BODY, mBody);
        json.put(URL, mUrl);
        json.put(CREATED, DateTimeUtils.formatIso8601(mCreated));
        return json;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "[%d] %s (%s)", mId, mBody, DateTimeUtils.formatIso8601(mCreated));
    }
}
