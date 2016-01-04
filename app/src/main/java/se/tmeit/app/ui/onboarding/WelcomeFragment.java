package se.tmeit.app.ui.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import se.tmeit.app.R;

/**
 * First screen displayed to sparkly new users.
 */
public final class WelcomeFragment extends Fragment {
	private WelcomeFragmentCallbacks mWelcomeFragmentCallbacks;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_welcome, container, false);

		Button logInWithKthButton = (Button) view.findViewById(R.id.onboarding_log_in_kth_button);
		logInWithKthButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mWelcomeFragmentCallbacks) {
					mWelcomeFragmentCallbacks.onLogInWithKthClicked();
				}
			}
		});

		Button logInWithoutKthButton = (Button) view.findViewById(R.id.onboarding_log_in_without_kth_button);
		logInWithoutKthButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mWelcomeFragmentCallbacks) {
					mWelcomeFragmentCallbacks.onLogInWithoutKthClicked();
				}
			}
		});

		return view;
	}

	public void setCallbacks(WelcomeFragmentCallbacks welcomeFragmentCallbacks) {
		mWelcomeFragmentCallbacks = welcomeFragmentCallbacks;
	}

	public static interface WelcomeFragmentCallbacks {
		public void onLogInWithKthClicked();

		public void onLogInWithoutKthClicked();
	}
}
