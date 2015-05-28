package se.tmeit.app.ui.externalEvents;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import se.tmeit.app.R;
import se.tmeit.app.model.ExternalEvent;
import se.tmeit.app.services.Repository;
import se.tmeit.app.services.RepositoryResultHandler;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for an external event.
 */
public final class ExternalEventInfoFragment extends Fragment implements MainActivity.HasTitle {
    private static final char FORMAT_SPACE = ' ';
    private final static String TAG = ExternalEventInfoFragment.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private final ExternalEventResultHandler mRepositoryResultHandler = new ExternalEventResultHandler();
    private Button mAttendingButton;
    private Preferences mPrefs;
    private ProgressBar mProgressBar;
    private View mDetailsLayout;

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
    public int getTitle() {
        return R.string.event_external_info_nav_title;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPrefs = new Preferences(activity);
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
        mDetailsLayout = view.findViewById(R.id.event_details_layout);

        beginLoad();

        return view;
    }

    private void beginLoad() {
        mAttendingButton.setEnabled(false);
        mDetailsLayout.setVisibility(View.GONE);
        setProgressBarVisible(true);

        Bundle args = getArguments();
        int id = args.getInt(ExternalEvent.Keys.ID);

        String username = mPrefs.getAuthenticatedUser(), serviceAuth = mPrefs.getServiceAuthentication();
        Repository repository = new Repository(username, serviceAuth);
        repository.getExternalEventDetails(id, mRepositoryResultHandler);
    }

    private void finishLoad(ExternalEvent.RepositoryData repositoryData) {
        View view = getView();
        if (null == view) {
            return;
        }

        ExternalEvent event = repositoryData.getExternalEvent();

        TextView bodyText = (TextView) view.findViewById(R.id.event_body);
        bodyText.setText(event.getBody());

        TextView externalUrlText = (TextView) view.findViewById(R.id.event_external_url);
        if(!TextUtils.isEmpty(event.getExternalUrl())) {
            externalUrlText.setText(getString(R.string.event_more_information_at_url) + ' ' + event.getExternalUrl());
            externalUrlText.setVisibility(View.VISIBLE);
        } else {
            externalUrlText.setVisibility(View.GONE);
        }

        mDetailsLayout.setVisibility(View.VISIBLE);
    }

    private void setProgressBarVisible(boolean visible) {
        mProgressBar.setIndeterminate(visible);
        mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private final class ExternalEventResultHandler implements RepositoryResultHandler<ExternalEvent.RepositoryData> {
        @Override
        public void onError(final int errorMessage) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Activity activity = getActivity();
                    if (null != activity && isVisible()) {
                        setProgressBarVisible(false);
                        Toast toast = Toast.makeText(activity, getString(errorMessage), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            });
        }

        @Override
        public void onSuccess(final ExternalEvent.RepositoryData repositoryData) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != getActivity() && isVisible()) {
                        setProgressBarVisible(false);
                        finishLoad(repositoryData);
                    }
                }
            });
        }
    }
}
