package se.tmeit.app.onboarding;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Performs QR code scanning.
 */
public final class ScanActivity extends Activity implements ZBarScannerView.ResultHandler {
    private final static String TAG = ScanActivity.class.getSimpleName();
    private ZBarScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);
        mScannerView.setFormats( Arrays.asList(BarcodeFormat.QRCODE));
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.v(TAG, rawResult.getContents());
        Log.v(TAG, rawResult.getBarcodeFormat().getName());
        finish();
    }
}