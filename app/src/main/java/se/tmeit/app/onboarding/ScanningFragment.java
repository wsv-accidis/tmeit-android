package se.tmeit.app.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Fragment where the QR code is scanned for onboarding.
 */
public final class ScanningFragment extends Fragment {
    private final static String TAG = ScanningFragment.class.getSimpleName();
    private ZBarScannerView mScannerView;
    private ZBarScannerView.ResultHandler mScanResultHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mScannerView = new ZBarScannerView(getActivity());
        mScannerView.setFormats(Arrays.asList(BarcodeFormat.QRCODE));
        return mScannerView;
    }

    public void setResultHandler(ZBarScannerView.ResultHandler resultHandler) {
        mScanResultHandler = resultHandler;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mScanResultHandler) {
            mScannerView.setResultHandler(mScanResultHandler);
        } else {
            Log.e(TAG, "No result handler has been assigned. This is probably going to be very unproductive.");
        }

        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }
}