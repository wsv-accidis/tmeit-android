package se.tmeit.app.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import se.tmeit.app.MainActivity;
import se.tmeit.app.R;
import se.tmeit.app.storage.Preferences;


public final class OnboardingActivity extends FragmentActivity {
    private final static String TAG = OnboardingActivity.class.getSimpleName();
    private final ScanResultHandler mScanResultHandler = new ScanResultHandler();
    private FragmentManager mFragmentManager;
    private Preferences mPrefs;

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

    private class WelcomeFragmentCallbacks implements WelcomeFragment.WelcomeFragmentCallbacks {
        @Override
        public void onContinueClicked() {
            ScanningFragment scanFragment = new ScanningFragment();
            scanFragment.setResultHandler(mScanResultHandler);
            mFragmentManager.beginTransaction()
                    .replace(R.id.container, scanFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private class ScanResultHandler implements ZBarScannerView.ResultHandler {
        @Override
        public void handleResult(Result rawResult) {

            String contents = rawResult.getContents();
            Log.v(TAG, contents);

            // TODO Validate that rawResult.getBarcodeFormat() is QRCODE
            // TODO Validate code contents

            mPrefs.setServiceAuthentication(contents);

            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

