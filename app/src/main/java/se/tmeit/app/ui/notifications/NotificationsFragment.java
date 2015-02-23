package se.tmeit.app.ui.notifications;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;

import se.tmeit.app.R;
import se.tmeit.app.notifications.GcmRegistration;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for configuring notification settings.
 */
public class NotificationsFragment extends MainActivity.MainActivityFragment {
    private static final String TAG = NotificationsFragment.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private final RegistrationResultHandler mResultHandler = new RegistrationResultHandler();
    private Switch mNotificationsSwitch;
    private boolean mSuppressSwitchListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        mNotificationsSwitch = (Switch) view.findViewById(R.id.notifications_enable);
        mNotificationsSwitch.setChecked(GcmRegistration.getInstance(getActivity()).isRegistered());
        mNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSuppressSwitchListener) {
                    return;
                }

                Context context = getActivity();
                GcmRegistration gcmRegistration = GcmRegistration.getInstance(context);

                if (gcmRegistration.isBusy()) {
                    Toast toast = Toast.makeText(context, R.string.notifications_server_is_still_processing, Toast.LENGTH_SHORT);
                    toast.show();
                    mNotificationsSwitch.setChecked(!isChecked);
                    return;
                }

                if (isChecked) {
                    gcmRegistration.register(mResultHandler);
                } else {
                    gcmRegistration.unregister(mResultHandler);
                }
            }
        });

        return view;
    }

    @Override
    protected int getTitle() {
        return R.string.notifications_title;
    }

    private final class RegistrationResultHandler implements GcmRegistration.RegistrationResultHandler {
        private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

        @Override
        public void onError(final int errorMessage) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Activity activity = getActivity();
                    if (null == activity || !isVisible()) {
                        return;
                    }

                    mSuppressSwitchListener = true;
                    mNotificationsSwitch.setChecked(false);
                    mSuppressSwitchListener = false;

                    Toast toast = Toast.makeText(activity, getString(errorMessage), Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

        @Override
        public void onGoogleServicesError(final int resultCode, final boolean canRecover) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Activity activity = getActivity();
                    if (null == activity || !isVisible()) {
                        return;
                    }

                    mSuppressSwitchListener = true;
                    mNotificationsSwitch.setChecked(false);
                    mSuppressSwitchListener = false;

                    if (canRecover) {
                        GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
                    } else {
                        Toast toast = Toast.makeText(activity, R.string.notifications_your_device_does_not_support, Toast.LENGTH_LONG);
                        toast.show();
                    }

                }
            });
        }

        @Override
        public void onSuccess() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Activity activity = getActivity();
                    if (null == activity || !isVisible()) {
                        return;
                    }

                    Toast toast = Toast.makeText(activity, R.string.notifications_success, Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }
}
