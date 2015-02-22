package se.tmeit.app.ui.onboarding;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import se.tmeit.app.R;

/**
 * Pretends to be the ScanningFragment, but actually reads data from a textbox. For development.
 * Use this to paste the code into the textbox: adb -s {emulator id} shell input text {one-time code}
 */
public final class EmulatedScanningFragment extends ScanningFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emu_scanning, container, false);

        final EditText scanText = (EditText) view.findViewById(R.id.onboarding_scanemu_text);
        Button continueButton = (Button) view.findViewById(R.id.onboarding_scanemu_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = scanText.getText().toString();

                // Shouldn't be needed - but Android's not smart enough to hide it
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(scanText.getWindowToken(), 0);

                if (null != mScanResultHandler) {
                    Result result = new Result();
                    result.setBarcodeFormat(BarcodeFormat.QRCODE);
                    result.setContents(text);
                    mScanResultHandler.handleResult(result);
                }
            }
        });

        view.requestFocus();
        return view;
    }
}
