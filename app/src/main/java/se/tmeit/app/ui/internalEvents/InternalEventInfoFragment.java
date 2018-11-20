package se.tmeit.app.ui.internalEvents;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.ExternalEvent;
import se.tmeit.app.model.InternalEvent;
import se.tmeit.app.model.InternalEventWorker;
import se.tmeit.app.services.Repository;
import se.tmeit.app.services.RepositoryResultHandler;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.MainActivity;
import se.tmeit.app.ui.externalEvents.ExternalEventAttendDialogFragment;

/**
 * Fragment for an internal event.
 */
public final class InternalEventInfoFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
	public static final String WORKER_NAME_FORMAT = "%s (%s/%s)";
	private final Handler mHandler = new Handler();
	private final InternalEventResultHandler mRepositoryResultHandler = new InternalEventResultHandler();
	private final WorkButtonClickListener mWorkClickedListener = new WorkButtonClickListener();
	private final WorkDialogListener mWorkDialogListener = new WorkDialogListener();
	private final WorkingResultHandler mWorkingResultHandler = new WorkingResultHandler();
	private InternalEventWorker mCurrentWorker;
	private View mDivider;
	private InternalEvent mEvent;
	private View mMaybeLayout;
	private View mNoLayout;
	private TextView mNumberOfWorkersText;
	private Preferences mPrefs;
	private ProgressBar mProgressBar;
	private Repository mRepository;
	private Button mWorkButton;
	private View mYesLayout;

	public static InternalEventInfoFragment createInstance(InternalEvent event) {
		Bundle bundle = new Bundle();
		bundle.putInt(InternalEvent.Keys.ID, event.id());
		bundle.putString(InternalEvent.Keys.TITLE, event.title());
		bundle.putString(InternalEvent.Keys.START_DATE, event.startDate());
		bundle.putString(InternalEvent.Keys.START_TIME, event.startTime());
		bundle.putString(InternalEvent.Keys.TEAM_TITLE, event.teamTitle());
		bundle.putInt(InternalEvent.Keys.WORKERS_COUNT, event.workersCount());
		bundle.putInt(InternalEvent.Keys.WORKERS_MAX, event.workersMax());

		final InternalEventInfoFragment instance = new InternalEventInfoFragment();
		instance.setArguments(bundle);

		return instance;
	}

	@Override
	public int getItemId() {
		return R.id.nav_event_internal;
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
		View view = inflater.inflate(R.layout.fragment_internal_event_info, container, false);

		final Bundle args = getArguments();
		final TextView titleText = view.findViewById(R.id.event_title);
		titleText.setText(args.getString(InternalEvent.Keys.TITLE));

		final TextView startDateText = view.findViewById(R.id.event_start_date);
		startDateText.setText(args.getString(InternalEvent.Keys.START_DATE) + ' ' + args.getString(InternalEvent.Keys.START_TIME));

		mNumberOfWorkersText = view.findViewById(R.id.event_number_of_workers);
		final int workersCount = args.getInt(InternalEvent.Keys.WORKERS_COUNT), workersMax = args.getInt(InternalEvent.Keys.WORKERS_MAX);
		setNumberOfWorkersText(workersCount, workersMax);

		String teamTitle = args.getString(InternalEvent.Keys.TEAM_TITLE);
		if (TextUtils.isEmpty(teamTitle)) {
			teamTitle = getString(R.string.members_no_team_placeholder);
		}

		final TextView teamText = view.findViewById(R.id.event_team);
		teamText.setText(teamTitle);

		mDivider = view.findViewById(R.id.event_divider);
		mYesLayout = view.findViewById(R.id.event_workers_yes_layout);
		mMaybeLayout = view.findViewById(R.id.event_workers_maybe_layout);
		mNoLayout = view.findViewById(R.id.event_workers_no_layout);
		mProgressBar = view.findViewById(R.id.event_progress_bar);
		mWorkButton = view.findViewById(R.id.event_button_work);

		beginLoad(false);

		return view;
	}

	private void beginLoad(boolean noCache) {
		setProgressBarVisible(true);

		final Bundle args = getArguments();
		final int id = args.getInt(ExternalEvent.Keys.ID);
		final String username = mPrefs.getAuthenticatedUserName(), serviceAuth = mPrefs.getServiceAuthentication();

		mRepository = new Repository(username, serviceAuth);
		mRepository.getInternalEventDetails(id, noCache, mRepositoryResultHandler);
	}

	private void finishLoad(InternalEvent.RepositoryData repositoryData) {
		final View view = getView();
		if (null == view) {
			return;
		}

		final LinearLayout yesList = view.findViewById(R.id.event_workers_yes),
			maybeList = view.findViewById(R.id.event_workers_maybe),
			noList = view.findViewById(R.id.event_workers_no);

		final List<InternalEventWorker> yesWorkers = InternalEventWorker.filterByWorking(repositoryData.getWorkers(), InternalEventWorker.Working.YES),
			maybeWorkers = InternalEventWorker.filterByWorking(repositoryData.getWorkers(), InternalEventWorker.Working.MAYBE),
			noWorkers = InternalEventWorker.filterByWorking(repositoryData.getWorkers(), InternalEventWorker.Working.NO);

		initializeListOfWorkers(mYesLayout, yesList, yesWorkers);
		initializeListOfWorkers(mMaybeLayout, maybeList, maybeWorkers);
		initializeListOfWorkers(mNoLayout, noList, noWorkers);

		mEvent = repositoryData.getEvent();
		mCurrentWorker = getCurrentWorker(repositoryData.getWorkers());
		setNumberOfWorkersText(yesWorkers.size(), mEvent.workersMax());

		mWorkButton.setOnClickListener(mWorkClickedListener);
		mWorkButton.setEnabled(!repositoryData.getEvent().isPast());
		mWorkButton.setVisibility(View.VISIBLE);
		mDivider.setVisibility(View.VISIBLE);
	}

	private InternalEventWorker getCurrentWorker(List<InternalEventWorker> workers) {
		final int userId = mPrefs.getAuthenticatedUserId();
		for (InternalEventWorker worker : workers) {
			if (worker.id() == userId) {
				return worker;
			}
		}

		return null;
	}

	private void initializeListOfWorkers(View listLayout, LinearLayout listView, List<InternalEventWorker> workers) {
		if (null == workers || workers.isEmpty()) {
			listLayout.setVisibility(View.GONE);
			return;
		}

		final View mainView = getView();
		if (null == mainView) {
			return;
		}

		final int marginWidth = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
		final int listWidth = mainView.getWidth() - 2 * marginWidth;
		final double widthHour = listWidth / (double) (InternalEventWorker.RANGE_MAX_HOUR - InternalEventWorker.RANGE_MIN_HOUR);

		listView.removeAllViews();

		final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
		for (InternalEventWorker worker : workers) {
			final View view = layoutInflater.inflate(R.layout.list_item_internal_event_worker, null);

			final TextView nameText = view.findViewById(R.id.event_worker_name);
			final String teamTitle = TextUtils.isEmpty(worker.teamTitle()) ? getString(R.string.event_worker_no_team_placeholder) : worker.teamTitle();
			nameText.setText(String.format(WORKER_NAME_FORMAT, worker.name(), worker.groupTitle(), teamTitle));

			final TextView commentText = view.findViewById(R.id.event_worker_comment);
			commentText.setText(TextUtils.isEmpty(worker.comment()) ? "-" : worker.comment());

			final TextView rangeTextView = view.findViewById(R.id.event_worker_range_text);
			final View rangeView = view.findViewById(R.id.event_worker_range);
			final View rangeBgView = view.findViewById(R.id.event_worker_range_bg);
			final View rangeEmptyView = view.findViewById(R.id.event_worker_range_empty);

			if (worker.hasRange()) {
				final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rangeView.getLayoutParams();

				params.leftMargin = (int) Math.ceil((worker.rangeStart() - InternalEventWorker.RANGE_MIN_HOUR) * widthHour);
				params.width = (int) Math.ceil((worker.rangeEnd() - worker.rangeStart()) * widthHour);
				rangeView.setLayoutParams(params);

				rangeView.setVisibility(View.VISIBLE);
				rangeBgView.setVisibility(View.VISIBLE);
				rangeEmptyView.setVisibility(View.GONE);

				final int rangeStart = worker.rangeStart() % 24, rangeEnd = worker.rangeEnd() % 24;
				rangeTextView.setText(String.format("%02d-%02d", rangeStart, rangeEnd));
			} else {
				rangeView.setVisibility(View.GONE);
				rangeBgView.setVisibility(View.GONE);
				rangeEmptyView.setVisibility(View.VISIBLE);
			}

			listView.addView(view);
		}

		listLayout.setVisibility(View.VISIBLE);
	}

	private void setNumberOfWorkersText(int workersCount, int workersMax) {
		final String workersStr = getString(workersMax == 1 ? R.string.event_worker : R.string.event_workers);
		mNumberOfWorkersText.setText(String.valueOf(workersCount) + ' ' + getString(R.string.event_workers_of) + ' ' + String.valueOf(workersMax) + ' ' + workersStr);
	}

	private void setProgressBarVisible(boolean visible) {
		if (visible) {
			mDivider.setVisibility(View.GONE);
			mYesLayout.setVisibility(View.GONE);
			mMaybeLayout.setVisibility(View.GONE);
			mNoLayout.setVisibility(View.GONE);
			mWorkButton.setVisibility(View.GONE);
		}

		mProgressBar.setIndeterminate(visible);
		mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private final class InternalEventResultHandler implements RepositoryResultHandler<InternalEvent.RepositoryData> {
		@Override
		public void onError(final int errorMessage) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					final Activity activity = getActivity();
					if (null != activity && isVisible()) {
						setProgressBarVisible(false);
						Toast toast = Toast.makeText(activity, getString(errorMessage), Toast.LENGTH_LONG);
						toast.show();
					}
				}
			});
		}

		@Override
		public void onSuccess(final InternalEvent.RepositoryData repositoryData) {
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

	private final class WorkButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			final Bundle args = new Bundle();
			if (null != mCurrentWorker) {
				args.putBoolean(InternalEventWorker.Keys.IS_SAVED, true);
				args.putInt(InternalEventWorker.Keys.WORKING, InternalEventWorker.Working.toInt(mCurrentWorker.working()));
				args.putString(InternalEventWorker.Keys.COMMENT, mCurrentWorker.comment());

				if (mCurrentWorker.hasRange()) {
					args.putInt(InternalEventWorker.Keys.RANGE_START, mCurrentWorker.rangeStart());
					args.putInt(InternalEventWorker.Keys.RANGE_END, mCurrentWorker.rangeEnd());
				}
			}

			final InternalEventWorkDialogFragment dialog = new InternalEventWorkDialogFragment();
			dialog.setArguments(args);
			dialog.setListener(mWorkDialogListener);
			dialog.show(getFragmentManager(), ExternalEventAttendDialogFragment.class.getSimpleName());
		}
	}

	private final class WorkDialogListener implements InternalEventWorkDialogFragment.InternalEventWorkDialogListener {
		@Override
		public void saveClicked(InternalEventWorker worker) {
			setProgressBarVisible(true);
			mRepository.workInternalEvent(mEvent.id(), worker, mWorkingResultHandler);
			mPrefs.setShouldRefreshInternalEvents(true);
		}
	}

	private final class WorkingResultHandler implements RepositoryResultHandler<Void> {
		@Override
		public void onError(final int errorMessage) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					Activity activity = getActivity();
					if (null != activity && isVisible()) {
						setProgressBarVisible(false);
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
}
