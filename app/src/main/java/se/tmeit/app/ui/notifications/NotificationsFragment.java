package se.tmeit.app.ui.notifications;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;

import se.tmeit.app.R;
import se.tmeit.app.notifications.GcmRegistration;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for configuring notification settings.
 */
public class NotificationsFragment extends MainActivity.MainActivityFragment {
    private static final String TAG = NotificationsFragment.class.getSimpleName();
    private final RegistrationResultHandler mResultHandler = new RegistrationResultHandler();
    private Switch mNotificationsSwitch;
    private Preferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = new Preferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        mNotificationsSwitch = (Switch) view.findViewById(R.id.notifications_enable);
        mNotificationsSwitch.setChecked(mPrefs.hasGcmRegistrationId());
        mNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Context context = getActivity();
                GcmRegistration gcmRegistration = GcmRegistration.getInstance(context);

                if (gcmRegistration.isBusy()) {
                    Toast toast = Toast.makeText(context, R.string.notifications_server_is_still_processing, Toast.LENGTH_SHORT);
                    toast.show();
                    mNotificationsSwitch.setChecked(!isChecked);
                    return;
                }

                if (isChecked) {
                    gcmRegistration.unregister(mResultHandler);
                } else {
                    gcmRegistration.register(mResultHandler);
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
        public void onGoogleServicesError(int resultCode, boolean canRecover) {
            Activity activity = getActivity();
            if (null == activity) {
                return;
            }

            mNotificationsSwitch.setChecked(false);

            if (canRecover) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast toast = Toast.makeText(activity, R.string.notifications_your_device_does_not_support, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
