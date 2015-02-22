package se.tmeit.app.ui.onboarding;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import se.tmeit.app.utils.AndroidUtils;

/**
 * Scans the QR code for onboarding using the device camera.
 */
public final class CameraScanningFragment extends ScanningFragment {
    private final static String TAG = CameraScanningFragment.class.getSimpleName();
    private ZBarScannerView mScannerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mScannerView = new ZBarScannerView(getActivity());
        mScannerView.setFormats(Arrays.asList(BarcodeFormat.QRCODE));
        if (null != mScanResultHandler) {
            mScannerView.setResultHandler(mScanResultHandler);
        }
        return mScannerView;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "CameraScanningFragment pausing.");

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        mScannerView.stopCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "CameraScanningFragment resuming.");

        int requestedOrientation = AndroidUtils.hasApiLevel(Build.VERSION_CODES.JELLY_BEAN_MR2)
                ? ActivityInfo.SCREEN_ORIENTATION_LOCKED
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        getActivity().setRequestedOrientation(requestedOrientation);
        mScannerView.startCamera();
    }
}