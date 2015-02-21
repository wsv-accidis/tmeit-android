package se.tmeit.app.onboarding;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import se.tmeit.app.R;

/**
 * First fragment displayed to sparkly new users.
 */
public final class WelcomeFragment extends Fragment {
    private static final String TMEIT_URL = "http://tmeit.se";
    private WelcomeFragmentCallbacks mWelcomeFragmentCallbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        Button quitButton = (Button) view.findViewById(R.id.welcome_quit_button);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(TMEIT_URL));
                startActivity(intent);
                getActivity().finish();
            }
        });

        Button continueButton = (Button) view.findViewById(R.id.welcome_continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mWelcomeFragmentCallbacks) {
                    mWelcomeFragmentCallbacks.onContinueClicked();
                }
            }
        });

        return view;
    }

    public void setCallbacks(WelcomeFragmentCallbacks welcomeFragmentCallbacks) {
        mWelcomeFragmentCallbacks = welcomeFragmentCallbacks;
    }

    public static interface WelcomeFragmentCallbacks {
        public void onContinueClicked();
    }
}
