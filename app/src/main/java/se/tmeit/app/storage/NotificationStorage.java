package se.tmeit.app.storage;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import se.tmeit.app.model.Notification;

/**
 * Manages storage of notifications. Notifications that have been read are deleted.
 */
public final class NotificationStorage {
    private static final String FILENAME = "TmeitNotifications";
    private static final String TAG = NotificationStorage.class.getSimpleName();
    private static NotificationStorage mInstance;
    private final Context mContext;
    private final ArrayList<Notification> mNotifications = new ArrayList<>();

    private NotificationStorage(Context context) {
        mContext = context;
        readFromFile();
    }

    public static synchronized NotificationStorage getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new NotificationStorage(context.getApplicationContext());
        }

        return mInstance;
    }

    public void add(Notification notification) {
        int existingIdx = indexOf(notification.id());
        if (-1 != existingIdx) {
            Notification existing = mNotifications.get(existingIdx);
            if (!existing.equals(notification)) {
                mNotifications.remove(existingIdx);
                mNotifications.add(notification);
            }
        } else {
            mNotifications.add(notification);
        }
    }

    public void commit() {
        Log.i(TAG, "Persisting notifications to storage.");

        BufferedWriter writer = null;
        try {
            JSONArray array = new JSONArray();
            for (Notification notif : mNotifications) {
                array.put(notif.toJson());
            }

            FileOutputStream outputStream = mContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(array.toString());
        } catch (Exception ex) {
            Log.e(TAG, "Unexpected exception while writing notifications.", ex);
        } finally {
            if (null != writer) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public List<Notification> get() {
        return mNotifications;
    }

    public void remove(int id) {
        int idx = indexOf(id);
        if (-1 != idx) {
            mNotifications.remove(idx);
        }
    }

    private int indexOf(int id) {
        int idx = 0;
        for (Notification notif : mNotifications) {
            if (notif.id() == id) {
                return idx;
            } else {
                idx++;
            }
        }

        return -1;
    }

    private String readFileIntoString(String fileName) throws IOException {
        BufferedReader reader = null;
        try {
            FileInputStream inputStream = mContext.openFileInput(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while (null != (line = reader.readLine()))
                builder.append(line).append('\n');
            return builder.toString();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void readFromFile() {
        Log.i(TAG, "Loading persisted notifications from storage.");

        try {
            mNotifications.clear();
            String result = readFileIntoString(FILENAME);
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                mNotifications.add(Notification.fromJson(jsonArray.getJSONObject(i)));
            }

            Log.i(TAG, "Loaded " + mNotifications.size() + " notification(s).");
        } catch (FileNotFoundException ignored) {
            Log.i(TAG, "No existing file found, no existing notifications loaded.");
        } catch (Exception ex) {
            Log.e(TAG, "Unexpected exception while reading notifications.", ex);
        }
    }
}
