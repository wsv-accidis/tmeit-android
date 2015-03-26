package se.tmeit.app.ui.notifications;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
public class NotificationsFragment extends Fragment implements MainActivity.HasTitle {
    private static final String TAG = NotificationsFragment.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private final RegistrationResultHandler mResultHandler = new RegistrationResultHandler();
    private GcmRegistration mGcmRegistration;
    private Switch mNotificationsSwitch;
    private boolean mSuppressSwitchListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        mGcmRegistration = GcmRegistration.getInstance(getActivity());
        mNotificationsSwitch = (Switch) view.findViewById(R.id.notifications_enable);
        mNotificationsSwitch.setChecked(mGcmRegistration.isRegistered());
        mNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSuppressSwitchListener) {
                    return;
                }

                Context context = getActivity();
                if (mGcmRegistration.isBusy()) {
                    Toast toast = Toast.makeText(context, R.string.notifications_server_is_still_processing, Toast.LENGTH_SHORT);
                    toast.show();
                    mNotificationsSwitch.setChecked(!isChecked);
                    return;
                }

                if (isChecked) {
                    mGcmRegistration.register(mResultHandler);
                } else {
                    mGcmRegistration.unregister(mResultHandler);
                }
            }
        });

        return view;
    }

    public void refreshNotificationsState() {
        setNotificationsSwitch(mGcmRegistration.isRegistered());
    }

    @Override
    public int getTitle() {
        return R.string.notifications_title;
    }

    private void setNotificationsSwitch(boolean checked) {
        mSuppressSwitchListener = true;
        mNotificationsSwitch.setChecked(checked);
        mSuppressSwitchListener = false;
    }

    private final class RegistrationResultHandler implements GcmRegistration.RegistrationResultHandler {
        @Override
        public void onError(final int errorMessage) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Activity activity = getActivity();
                    if (null == activity || !isVisible()) {
                        return;
                    }

                    setNotificationsSwitch(false);

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

                    setNotificationsSwitch(true);

                    if (canRecover) {
                        GooglePlayServicesUtil.getErrorDialog(resultCode, activity, GcmRegistration.PLAY_SERVICES_RESOLUTION_REQUEST).show();
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
