package se.tmeit.app.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import se.tmeit.app.R;

/**
 * Pretends to be the ScanningFragment, but actually reads data from a textbox. For development.
 * Use this to paste the code into the textbox: adb -s {emulator id} shell input text {one-time code}
 */
public class EmulatedScanningFragment extends Fragment {
    private ZBarScannerView.ResultHandler mScanResultHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emu_scanning, container, false);

        final EditText scanText = (EditText) view.findViewById(R.id.scanning_emu_text);
        Button continueButton = (Button) view.findViewById(R.id.scanning_emu_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = scanText.getText().toString();

                if (null != mScanResultHandler) {
                    Result result = new Result();
                    result.setBarcodeFormat(BarcodeFormat.QRCODE);
                    result.setContents(text);
                    mScanResultHandler.handleResult(result);
                }
            }
        });

        scanText.requestFocus();
        return view;
    }

    public void setResultHandler(ZBarScannerView.ResultHandler resultHandler) {
        mScanResultHandler = resultHandler;
    }
}
