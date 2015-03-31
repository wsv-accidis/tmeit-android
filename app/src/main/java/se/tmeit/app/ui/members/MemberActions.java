package se.tmeit.app.ui.members;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Canned actions for taking specific actions on members that interact with the Android system.
 */
public final class MemberActions {
    private static final String TAG = MemberActions.class.getSimpleName();

    private MemberActions() {
    }

    public static void makeCallTo(String phoneNo, Fragment fragment) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNo, null));
            fragment.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Exception while trying to start a call.", e);
        }
    }

    public static void sendEmailTo(String email, Fragment fragment) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
            fragment.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Exception while trying to send e-mail.", e);
        }
    }

    public static void sendSmsTo(String phoneNo, Fragment fragment) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", phoneNo, null));
            fragment.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Exception while trying to send message.", e);
        }
    }
}
