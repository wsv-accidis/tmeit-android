package se.tmeit.app.ui.onboarding;

import android.support.v4.app.Fragment;

import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Base for the two scanning fragments to support hooking a result handler.
 */
public abstract class ScanningFragment extends Fragment {
    protected ZBarScannerView.ResultHandler mScanResultHandler;

    public void setResultHandler(ZBarScannerView.ResultHandler resultHandler) {
        mScanResultHandler = resultHandler;
    }
}
