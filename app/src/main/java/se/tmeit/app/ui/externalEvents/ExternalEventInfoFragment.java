package se.tmeit.app.ui.externalEvents;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import se.tmeit.app.R;
import se.tmeit.app.model.ExternalEvent;
import se.tmeit.app.services.Repository;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for an external event.
 */
public final class ExternalEventInfoFragment extends Fragment implements MainActivity.HasTitle {
    private final static String TAG = ExternalEventInfoFragment.class.getSimpleName();
    private static final char FORMAT_SPACE = ' ';
    private Button mAttendingButton;
    private ProgressBar mProgressBar;
    private Preferences mPrefs;

    public static ExternalEventInfoFragment createInstance(Context context, ExternalEvent event) {
        Bundle bundle = new Bundle();
        bundle.putInt(ExternalEvent.Keys.ID, event.getId());
        bundle.putString(ExternalEvent.Keys.TITLE, event.getTitle());
        bundle.putString(ExternalEvent.Keys.START_DATE, event.getStartDate());
        bundle.putString(ExternalEvent.Keys.LAST_SIGNUP, event.getLastSignupDate());

        ExternalEventInfoFragment instance = new ExternalEventInfoFragment();
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPrefs = new Preferences(activity);
    }

    @Override
    public int getTitle() {
        return R.string.event_external_info_nav_title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_external_event_info, container, false);

        Bundle args = getArguments();
        TextView titleText = (TextView) view.findViewById(R.id.event_title);
        titleText.setText(args.getString(ExternalEvent.Keys.TITLE));

        TextView startDateText = (TextView) view.findViewById(R.id.event_start_date);
        startDateText.setText(args.getString(ExternalEvent.Keys.START_DATE));

        TextView lastSignupText = (TextView) view.findViewById(R.id.event_last_signup);
        lastSignupText.setText(getString(R.string.event_last_signup_date) + FORMAT_SPACE + args.getString(ExternalEvent.Keys.LAST_SIGNUP));

        mAttendingButton = (Button) view.findViewById(R.id.event_button_attending);
        mProgressBar = (ProgressBar) view.findViewById(R.id.event_progress_bar);

        beginLoad();

        return view;
    }

    private void beginLoad() {
        mAttendingButton.setEnabled(false);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);

        String username = mPrefs.getAuthenticatedUser(), serviceAuth = mPrefs.getServiceAuthentication();
        Repository repository = new Repository(username, serviceAuth);

    }
}
