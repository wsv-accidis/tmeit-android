package se.tmeit.app.notifications;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Broadcast receiver for notifications through GCM.
 */
public final class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
        intent.setComponent(comp);
        startWakefulService(context, intent);
        setResultCode(Activity.RESULT_OK);
    }
}
