package se.tmeit.app.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import se.tmeit.app.R;
import se.tmeit.app.services.AuthenticationResultHandler;
import se.tmeit.app.services.ServiceAuthenticator;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.MainActivity;
import se.tmeit.app.utils.AndroidUtils;


public final class OnboardingActivity extends FragmentActivity {
	private final static String TAG = OnboardingActivity.class.getSimpleName();
	private final Handler mHandler = new Handler();
	private final ScanResultHandler mResultHandler = new ScanResultHandler();
	private OnboardingAuthenticationResultHandler mAuthResultHandler;
	private FragmentManager mFragmentManager;
	private boolean mHasShownNetworkAlert;
	private Preferences mPrefs;

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		if (null != mAuthResultHandler) {
			mAuthResultHandler.abandon();
			mAuthResultHandler = null;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_onboarding);

		mPrefs = new Preferences(this);
		mFragmentManager = getSupportFragmentManager();

		WelcomeFragment welcomeFragment = new WelcomeFragment();
		welcomeFragment.setCallbacks(new WelcomeFragmentCallbacks());
		mFragmentManager.beginTransaction()
			.replace(R.id.container, welcomeFragment)
			.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!mHasShownNetworkAlert && !AndroidUtils.isNetworkConnected(this)) {
			mHasShownNetworkAlert = true;
			MainActivity.showNoNetworkAlert(this);
		}
	}

	private final class OnboardingAuthenticationResultHandler implements AuthenticationResultHandler {
		private boolean mAbandoned;
		private WaitingFragment mWaitingFragment;

		public OnboardingAuthenticationResultHandler(WaitingFragment waitingFragment) {
			mWaitingFragment = waitingFragment;
		}

		private void abandon() {
			Log.d(TAG, "Abandoned the authentication result handler!");
			mAbandoned = true;
		}

		@Override
		public void onAuthenticationError(int errorMessage) {
			showErrorMessage(errorMessage);
		}

		@Override
		public void onNetworkError(int errorMessage) {
			showErrorMessage(errorMessage);
		}

		@Override
		public void onProtocolError(int errorMessage) {
			showErrorMessage(errorMessage);
		}

		@Override
		public void onSuccess(String serviceAuth, String userName, int userId) {
			if (mAbandoned) {
				return;
			}

			Log.i(TAG, "Completed onboarding flow. Authenticated user name = " + userName + ", id = " + userId);
			mPrefs.setServiceAuthentication(serviceAuth);
			mPrefs.setAuthenticatedUser(userName, userId);

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}
			});
		}

		private void showErrorMessage(final int errorMessage) {
			if (mAbandoned) {
				return;
			}

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mWaitingFragment.isVisible()) {
						mWaitingFragment.showErrorMessage(errorMessage);
					}
				}
			});
		}
	}

	private final class ScanResultHandler implements WebOnboardingFragment.OnboardingResultHandler {
		@Override
		public void handleResult(String authCode) {
			WaitingFragment waitingFragment = new WaitingFragment();
			mFragmentManager.beginTransaction()
				.replace(R.id.container, waitingFragment)
				.addToBackStack(null)
				.commit();

			ServiceAuthenticator authenticator = new ServiceAuthenticator();
			mAuthResultHandler = new OnboardingAuthenticationResultHandler(waitingFragment);
			authenticator.authenticateFromCode(authCode, mAuthResultHandler);
		}
	}

	private final class WelcomeFragmentCallbacks implements WelcomeFragment.WelcomeFragmentCallbacks {
		@Override
		public void onLogInWithKthClicked() {
			continueOnboarding(true);
		}

		@Override
		public void onLogInWithoutKthClicked() {
			continueOnboarding(false);
		}

		private void continueOnboarding(boolean usingSaml) {
			WebOnboardingFragment fragment = WebOnboardingFragment.newInstance(usingSaml);
			fragment.setResultHandler(mResultHandler);
			mFragmentManager.beginTransaction()
				.replace(R.id.container, fragment)
				.addToBackStack(null)
				.commit();
		}
	}
}