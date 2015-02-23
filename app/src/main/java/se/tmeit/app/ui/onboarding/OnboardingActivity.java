package se.tmeit.app.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import se.tmeit.app.R;
import se.tmeit.app.services.ServiceAuthenticator;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.MainActivity;
import se.tmeit.app.utils.AndroidUtils;


public final class OnboardingActivity extends FragmentActivity {
    private final static String TAG = OnboardingActivity.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private final ScanResultHandler mScanResultHandler = new ScanResultHandler();
    private AuthenticationResultHandler mAuthResultHandler;
    private FragmentManager mFragmentManager;
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

    private final class AuthenticationResultHandler implements ServiceAuthenticator.AuthenticationResultHandler {
        private boolean mAbandoned;
        private WaitingFragment mWaitingFragment;

        public AuthenticationResultHandler(WaitingFragment waitingFragment) {
            mWaitingFragment = waitingFragment;
        }

        public void abandon() {
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
        public void onSuccess(String serviceAuth, String authenticatedUser) {
            if (mAbandoned) {
                return;
            }

            Log.i(TAG, "Completed onboarding flow. Authenticated user = " + authenticatedUser);
            mPrefs.setServiceAuthentication(serviceAuth);
            mPrefs.setAuthenticatedUser(authenticatedUser);

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

    private final class ScanResultHandler implements ZBarScannerView.ResultHandler {
        @Override
        public void handleResult(Result rawResult) {
            String qrCode = rawResult.getContents();
            Log.d(TAG, "Read from QR code: " + qrCode);

            WaitingFragment waitingFragment = new WaitingFragment();
            mFragmentManager.beginTransaction()
                    .replace(R.id.container, waitingFragment)
                    .addToBackStack(null)
                    .commit();

            ServiceAuthenticator authenticator = new ServiceAuthenticator();
            mAuthResultHandler = new AuthenticationResultHandler(waitingFragment);
            authenticator.authenticateFromQr(qrCode, mAuthResultHandler);
        }
    }

    private final class WelcomeFragmentCallbacks implements WelcomeFragment.WelcomeFragmentCallbacks {
        @Override
        public void onContinueClicked() {
            ScanningFragment fragment;

            if (AndroidUtils.isInEmulator()) {
                fragment = new EmulatedScanningFragment();
            } else {
                fragment = new CameraScanningFragment();
            }

            fragment.setResultHandler(mScanResultHandler);
            mFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}

