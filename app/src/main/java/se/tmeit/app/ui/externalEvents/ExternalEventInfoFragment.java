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
public final class ExternalEventInfoFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
	private static final char FORMAT_SPACE = ' ';
	private final AttendingButtonClickListener mAttendingClickedListener = new AttendingButtonClickListener();
	private final AttendingDialogListener mAttendingDialogListener = new AttendingDialogListener();
	private final AttendingResultHandler mAttendingResultHandler = new AttendingResultHandler();
	private final Handler mHandler = new Handler();
	private final ExternalEventResultHandler mRepositoryResultHandler = new ExternalEventResultHandler();
	private Button mAttendingButton;
	private ExternalEventAttendee mCurrentAttendee;
	private View mDetailsLayout;
	private ExternalEvent mEvent;
	private boolean mIsAttending;
	private Preferences mPrefs;
	private ProgressBar mProgressBar;
	private Repository mRepository;

	public static ExternalEventInfoFragment createInstance(ExternalEvent event) {
		final Bundle bundle = new Bundle();
		bundle.putInt(ExternalEvent.Keys.ID, event.id());
		bundle.putString(ExternalEvent.Keys.TITLE, event.title());
		bundle.putString(ExternalEvent.Keys.START_DATE, event.startDate());
		bundle.putString(ExternalEvent.Keys.LAST_SIGNUP, event.lastSignupDate());

		final ExternalEventInfoFragment instance = new ExternalEventInfoFragment();
		instance.setArguments(bundle);
		return instance;
	}

	@Override
	public int getItemId() {
		return R.id.nav_event_external;
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
		final View view = inflater.inflate(R.layout.fragment_external_event_info, container, false);

		final Bundle args = getArguments();
		final TextView titleText = view.findViewById(R.id.event_title);
		titleText.setText(args.getString(ExternalEvent.Keys.TITLE));

		final TextView startDateText = view.findViewById(R.id.event_start_date);
		startDateText.setText(args.getString(ExternalEvent.Keys.START_DATE));

		final TextView lastSignupText = view.findViewById(R.id.event_last_signup);
		lastSignupText.setText(getString(R.string.event_last_signup_date) + FORMAT_SPACE + args.getString(ExternalEvent.Keys.LAST_SIGNUP));

		mAttendingButton = view.findViewById(R.id.event_button_attending);
		mProgressBar = view.findViewById(R.id.event_progress_bar);
		mDetailsLayout = view.findViewById(R.id.event_details_layout);

		beginLoad(false);

		return view;
	}

	private void beginLoad(boolean noCache) {
		setProgressBarVisible(true);

		final Bundle args = getArguments();
		int id = args.getInt(ExternalEvent.Keys.ID);

		final String username = mPrefs.getAuthenticatedUserName(), serviceAuth = mPrefs.getServiceAuthentication();
		mRepository = new Repository(username, serviceAuth);
		mRepository.getExternalEventDetails(id, noCache, mRepositoryResultHandler);
	}

	private void finishLoad(ExternalEvent.RepositoryData repositoryData) {
		final View view = getView();
		if (null == view) {
			return;
		}

		mEvent = repositoryData.getEvent();
		mCurrentAttendee = repositoryData.getCurrentAttendee();
		mIsAttending = repositoryData.isUserAttending(mPrefs.getAuthenticatedUserId());

		final TextView bodyText = view.findViewById(R.id.event_body);
		bodyText.setText(mEvent.body());

		final TextView externalUrlText = view.findViewById(R.id.event_external_url);
		if (!TextUtils.isEmpty(mEvent.externalUrl())) {
			externalUrlText.setText(getString(R.string.event_more_information_at_url) + ' ' + mEvent.externalUrl());
			externalUrlText.setVisibility(View.VISIBLE);
		} else {
			externalUrlText.setVisibility(View.GONE);
		}

		final List<ExternalEventAttendee> attendees = repositoryData.getAttendees();
		final LinearLayout attendeesLayout = view.findViewById(R.id.event_attendees);
		final View noAttendeesText = view.findViewById(R.id.event_no_attendees);

		if (null != attendees && !attendees.isEmpty()) {
			noAttendeesText.setVisibility(View.GONE);
			attendeesLayout.setVisibility(View.VISIBLE);
			initializeListOfAttendees(attendeesLayout, attendees);
		} else {
			attendeesLayout.setVisibility(View.GONE);
			noAttendeesText.setVisibility(View.VISIBLE);
		}

		mAttendingButton.setOnClickListener(mAttendingClickedListener);
		mAttendingButton.setText(mIsAttending ? R.string.event_attending : R.string.event_not_attending);
		mAttendingButton.setEnabled(!mEvent.isPastSignup());
		mAttendingButton.setVisibility(View.VISIBLE);

		mDetailsLayout.setVisibility(View.VISIBLE);
	}

	private void initializeListOfAttendees(LinearLayout layout, List<ExternalEventAttendee> attendees) {
		layout.removeAllViews();

		final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
		for (ExternalEventAttendee attendee : attendees) {
			final TextView view = (TextView) layoutInflater.inflate(R.layout.list_item_external_event_attendee, layout, false);
			final StringBuilder builder = new StringBuilder();

			builder.append(attendee.name());

			final boolean hasDrinkPrefs = !TextUtils.isEmpty(attendee.drinkPreferences()),
				hasFoodPrefs = !TextUtils.isEmpty(attendee.foodPreferences());

			if (hasDrinkPrefs || hasFoodPrefs) {
				builder.append(" (");
				if (hasDrinkPrefs) {
					builder.append(attendee.drinkPreferences());
					if (hasFoodPrefs) {
						builder.append(", ");
					}
				}
				if (hasFoodPrefs) {
					builder.append(attendee.foodPreferences());
				}
				builder.append(")");
			}

			view.setText(builder.toString());
			layout.addView(view);
		}
	}

	private void setProgressBarVisible(boolean visible) {
		if (visible) {
			mDetailsLayout.setVisibility(View.GONE);
			mAttendingButton.setVisibility(View.GONE);
		}

		mProgressBar.setIndeterminate(visible);
		mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private final class AttendingButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			final Bundle args = new Bundle();
			args.putBoolean(ExternalEvent.Keys.IS_ATTENDING, mIsAttending);
			args.putString(ExternalEventAttendee.Keys.NAME, mCurrentAttendee.name());
			args.putString(ExternalEventAttendee.Keys.DOB, mCurrentAttendee.dateOfBirth());
			args.putString(ExternalEventAttendee.Keys.DRINK_PREFS, mCurrentAttendee.drinkPreferences());
			args.putString(ExternalEventAttendee.Keys.FOOD_PREFS, mCurrentAttendee.foodPreferences());
			args.putString(ExternalEventAttendee.Keys.NOTES, mCurrentAttendee.notes());

			final ExternalEventAttendDialogFragment dialog = new ExternalEventAttendDialogFragment();
			dialog.setArguments(args);
			dialog.setListener(mAttendingDialogListener);
			dialog.show(getFragmentManager(), ExternalEventAttendDialogFragment.class.getSimpleName());
		}
	}

	private final class AttendingDialogListener implements ExternalEventAttendDialogFragment.ExternalEventAttendDialogListener {
		@Override
		public void deleteClicked() {
			setProgressBarVisible(true);
			mRepository.attendExternalEvent(mEvent.id(), null, mAttendingResultHandler);
			mPrefs.setShouldRefreshExternalEvents(true);
		}

		@Override
		public void saveClicked(ExternalEventAttendee attendee) {
			setProgressBarVisible(true);
			mRepository.attendExternalEvent(mEvent.id(), attendee, mAttendingResultHandler);
			mPrefs.setShouldRefreshExternalEvents(true);
		}
	}

	private final class AttendingResultHandler implements RepositoryResultHandler<Void> {
		@Override
		public void onError(final int errorMessage) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					final Activity activity = getActivity();
					if (null != activity && isVisible()) {
						setProgressBarVisible(false);

						final Toast toast = Toast.makeText(getActivity(), getString(errorMessage), Toast.LENGTH_LONG);
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
					final Activity activity = getActivity();
					if (null != activity && isVisible()) {
						setProgressBarVisible(false);

						final Toast toast = Toast.makeText(activity, getString(errorMessage), Toast.LENGTH_LONG);
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
