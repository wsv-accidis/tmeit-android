package se.tmeit.app.ui.notifications;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import se.tmeit.app.R;
import se.tmeit.app.notifications.GcmRegistration;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for configuring notification settings.
 */
public class NotificationsFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
	private GcmRegistration mGcmRegistration;
	private Switch mNotificationsSwitch;
	private boolean mSuppressSwitchListener;

	@Override
	public int getItemId() {
		return R.id.nav_notifications;
	}

	@Override
	public int getTitle() {
		return R.string.notifications_nav_title;
	}

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
					if (checkForGooglePlayServices()) {
						mGcmRegistration.register();
					}
				} else if (mGcmRegistration.isRegistered()) {
					mGcmRegistration.unregister();
				}
			}
		});

		return view;
	}

	private boolean checkForGooglePlayServices() {
		GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();

		int resultCode = googleApi.isGooglePlayServicesAvailable(getContext());
		if (ConnectionResult.SUCCESS != resultCode) {
			if (googleApi.isUserResolvableError(resultCode)) {
				googleApi.getErrorDialog(getActivity(), resultCode, GcmRegistration.PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Toast toast = Toast.makeText(getActivity(), R.string.notifications_your_device_does_not_support, Toast.LENGTH_LONG);
				toast.show();
			}
			return false;
		}

		return true;
	}

	public void refreshNotificationsState() {
		if (isVisible()) {
			Toast toast = Toast.makeText(getActivity(), R.string.notifications_success, Toast.LENGTH_LONG);
			toast.show();
		}

		setNotificationsSwitch(mGcmRegistration.isRegistered());
	}

	private void setNotificationsSwitch(boolean checked) {
		mSuppressSwitchListener = true;
		mNotificationsSwitch.setChecked(checked);
		mSuppressSwitchListener = false;
	}
}
