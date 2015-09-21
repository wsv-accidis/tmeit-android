package se.tmeit.app.ui.externalEvents;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.ExternalEvent;
import se.tmeit.app.model.ExternalEventAttendee;
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
    private final AttendingButtonClickListener mAttendingClickedListener = new AttendingButtonClickListener();
    private final Handler mHandler = new Handler();
    private final AttendingResultHandler mAttendingResultHandler = new AttendingResultHandler();
    private final AttendingDialogListener mAttendingDialogListener = new AttendingDialogListener();
    private final ExternalEventResultHandler mRepositoryResultHandler = new ExternalEventResultHandler();
    private Button mAttendingButton;
    private ExternalEventAttendee mCurrentAttendee;
    private View mDetailsLayout;
    private ExternalEvent mEvent;
    private Preferences mPrefs;
    private ProgressBar mProgressBar;
    private Repository mRepository;

    public static ExternalEventInfoFragment createInstance(ExternalEvent event) {
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
    public void onAttach(Context context) {
        super.onAttach(context);
        mPrefs = new Preferences(context);
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

        beginLoad(false);

        return view;
    }

    private void beginLoad(boolean noCache) {
        setProgressBarVisible(true);

        Bundle args = getArguments();
        int id = args.getInt(ExternalEvent.Keys.ID);

        String username = mPrefs.getAuthenticatedUserName(), serviceAuth = mPrefs.getServiceAuthentication();
        mRepository = new Repository(username, serviceAuth);
        mRepository.getExternalEventDetails(id, noCache, mRepositoryResultHandler);
    }

    private void finishLoad(ExternalEvent.RepositoryData repositoryData) {
        View view = getView();
        if (null == view) {
            return;
        }

        mEvent = repositoryData.getEvent();
        mCurrentAttendee = repositoryData.getCurrentAttendee();
        mEvent.setIsAttending(repositoryData.isUserAttending(mPrefs.getAuthenticatedUserId()));

        TextView bodyText = (TextView) view.findViewById(R.id.event_body);
        bodyText.setText(mEvent.getBody());

        TextView externalUrlText = (TextView) view.findViewById(R.id.event_external_url);
        if (!TextUtils.isEmpty(mEvent.getExternalUrl())) {
            externalUrlText.setText(getString(R.string.event_more_information_at_url) + ' ' + mEvent.getExternalUrl());
            externalUrlText.setVisibility(View.VISIBLE);
        } else {
            externalUrlText.setVisibility(View.GONE);
        }

        List<ExternalEventAttendee> attendees = repositoryData.getAttendees();
        LinearLayout attendeeListView = (LinearLayout) view.findViewById(R.id.event_attendees);
        View noAttendeesText = view.findViewById(R.id.event_no_attendees);
        if (null != attendees && !attendees.isEmpty()) {
            noAttendeesText.setVisibility(View.GONE);
            initializeListOfAttendees(attendeeListView, attendees);
        } else {
            attendeeListView.setVisibility(View.GONE);
            noAttendeesText.setVisibility(View.VISIBLE);
        }

        mAttendingButton.setOnClickListener(mAttendingClickedListener);
        mAttendingButton.setText(mEvent.isAttending() ? R.string.event_attending : R.string.event_not_attending);
        mAttendingButton.setEnabled(!mEvent.isPastSignup());
        mAttendingButton.setVisibility(View.VISIBLE);

        mDetailsLayout.setVisibility(View.VISIBLE);
    }

    private void initializeListOfAttendees(LinearLayout attendeeListView, List<ExternalEventAttendee> attendees) {
        attendeeListView.removeAllViews();

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        for (ExternalEventAttendee attendee : attendees) {
            TextView view = (TextView) layoutInflater.inflate(R.layout.list_item_external_event_attendee, null);

            StringBuilder builder = new StringBuilder();
            builder.append(attendee.getName());

            boolean hasDrinkPrefs = !TextUtils.isEmpty(attendee.getDrinkPreferences()),
                    hasFoodPrefs = !TextUtils.isEmpty(attendee.getFoodPreferences());

            if (hasDrinkPrefs || hasFoodPrefs) {
                builder.append(" (");
                if (hasDrinkPrefs) {
                    builder.append(attendee.getDrinkPreferences());
                    if (hasFoodPrefs) {
                        builder.append(", ");
                    }
                }
                if (hasFoodPrefs) {
                    builder.append(attendee.getFoodPreferences());
                }
                builder.append(")");
            }

            view.setText(builder.toString());
            attendeeListView.addView(view);
        }
    }

    private void setProgressBarVisible(boolean visible) {
        if(visible) {
            mDetailsLayout.setVisibility(View.GONE);
            mAttendingButton.setVisibility(View.GONE);
        }

        mProgressBar.setIndeterminate(visible);
        mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private final class AttendingButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Bundle args = new Bundle();
            args.putBoolean(ExternalEvent.Keys.IS_ATTENDING, mEvent.isAttending());
            args.putString(ExternalEventAttendee.Keys.NAME, mCurrentAttendee.getName());
            args.putString(ExternalEventAttendee.Keys.DOB, mCurrentAttendee.getDateOfBirth());
            args.putString(ExternalEventAttendee.Keys.DRINK_PREFS, mCurrentAttendee.getDrinkPreferences());
            args.putString(ExternalEventAttendee.Keys.FOOD_PREFS, mCurrentAttendee.getFoodPreferences());
            args.putString(ExternalEventAttendee.Keys.NOTES, mCurrentAttendee.getNotes());

            ExternalEventAttendDialogFragment dialog = new ExternalEventAttendDialogFragment();
            dialog.setArguments(args);
            dialog.setListener(mAttendingDialogListener);
            dialog.show(getFragmentManager(), ExternalEventAttendDialogFragment.class.getSimpleName());
        }
    }

    private final class AttendingDialogListener implements ExternalEventAttendDialogFragment.ExternalEventAttendDialogListener {
        @Override
        public void deleteClicked() {
            setProgressBarVisible(true);
            mRepository.attendExternalEvent(mEvent.getId(), null, mAttendingResultHandler);
        }

        @Override
        public void saveClicked(ExternalEventAttendee attendee) {
            setProgressBarVisible(true);
            mRepository.attendExternalEvent(mEvent.getId(), attendee, mAttendingResultHandler);
        }
    }

    private final class AttendingResultHandler implements RepositoryResultHandler<Void> {
        @Override
        public void onError(final int errorMessage) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Activity activity = getActivity();
                    if (null != activity && isVisible()) {
                        Toast toast = Toast.makeText(getActivity(), getString(errorMessage), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            });
        }

        @Override
        public void onSuccess(Void aVoid) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != getActivity() && isVisible()) {
                        beginLoad(true);
                    }
                }
            });
        }
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
