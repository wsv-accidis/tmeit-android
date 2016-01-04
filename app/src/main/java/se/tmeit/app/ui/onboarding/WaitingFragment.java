package se.tmeit.app.ui.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import se.tmeit.app.R;

/**
 * Displayed while waiting for a request to complete.
 */
public final class WaitingFragment extends Fragment {
	private View mErrorLayout;
	private TextView mErrorText;
	private View mWaitLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_waiting, container, false);

		mErrorLayout = view.findViewById(R.id.onboarding_error_layout);
		mErrorText = (TextView) view.findViewById(R.id.onboarding_error_text);
		mWaitLayout = view.findViewById(R.id.onboarding_waiting_layout);

		Button tryAgainButton = (Button) view.findViewById(R.id.onboarding_try_again_button);
		tryAgainButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});

		return view;
	}

	public void showErrorMessage(int resId) {
		mErrorText.setText(resId);
		mWaitLayout.setVisibility(View.GONE);
		mErrorLayout.setVisibility(View.VISIBLE);
	}
}
