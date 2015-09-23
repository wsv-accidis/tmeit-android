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
public final class InternalEventInfoFragment extends Fragment implements MainActivity.HasTitle {
    private final Handler mHandler = new Handler();
    private final InternalEventResultHandler mRepositoryResultHandler = new InternalEventResultHandler();
    private final WorkButtonClickListener mWorkClickedListener = new WorkButtonClickListener();
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
        bundle.putInt(InternalEvent.Keys.ID, event.getId());
        bundle.putString(InternalEvent.Keys.TITLE, event.getTitle());
        bundle.putString(InternalEvent.Keys.START_DATE, event.getStartDate());
        bundle.putInt(InternalEvent.Keys.WORKERS_COUNT, event.getWorkersCount());
        bundle.putInt(InternalEvent.Keys.WORKERS_MAX, event.getWorkersMax());

        InternalEventInfoFragment instance = new InternalEventInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_internal_event_info, container, false);

        Bundle args = getArguments();
        TextView titleText = (TextView) view.findViewById(R.id.event_title);
        titleText.setText(args.getString(InternalEvent.Keys.TITLE));

        TextView startDateText = (TextView) view.findViewById(R.id.event_start_date);
        startDateText.setText(args.getString(InternalEvent.Keys.START_DATE));

        mNumberOfWorkersText = (TextView) view.findViewById(R.id.event_number_of_workers);
        int workersCount = args.getInt(InternalEvent.Keys.WORKERS_COUNT), workersMax = args.getInt(InternalEvent.Keys.WORKERS_MAX);
        setNumberOfWorkersText(workersCount, workersMax);

        mYesLayout = view.findViewById(R.id.event_workers_yes_layout);
        mMaybeLayout = view.findViewById(R.id.event_workers_maybe_layout);
        mNoLayout = view.findViewById(R.id.event_workers_no_layout);
        mProgressBar = (ProgressBar) view.findViewById(R.id.event_progress_bar);
        mWorkButton = (Button) view.findViewById(R.id.event_button_work);

        beginLoad(false);

        return view;
    }

    private void beginLoad(boolean noCache) {
        setProgressBarVisible(true);

        Bundle args = getArguments();
        int id = args.getInt(ExternalEvent.Keys.ID);

        String username = mPrefs.getAuthenticatedUserName(), serviceAuth = mPrefs.getServiceAuthentication();
        mRepository = new Repository(username, serviceAuth);
        mRepository.getInternalEventDetails(id, noCache, mRepositoryResultHandler);
    }

    private void finishLoad(InternalEvent.RepositoryData repositoryData) {
        View view = getView();
        if (null == view) {
            return;
        }

        view.findViewById(R.id.event_divider).setVisibility(View.VISIBLE);

        LinearLayout yesList = (LinearLayout) view.findViewById(R.id.event_workers_yes),
                maybeList = (LinearLayout) view.findViewById(R.id.event_workers_maybe),
                noList = (LinearLayout) view.findViewById(R.id.event_workers_no);

        List<InternalEventWorker> yesWorkers = InternalEventWorker.filterByWorking(repositoryData.getWorkers(), InternalEventWorker.Working.YES),
                maybeWorkers = InternalEventWorker.filterByWorking(repositoryData.getWorkers(), InternalEventWorker.Working.MAYBE),
                noWorkers = InternalEventWorker.filterByWorking(repositoryData.getWorkers(), InternalEventWorker.Working.NO);

        initializeListOfWorkers(mYesLayout, yesList, yesWorkers);
        initializeListOfWorkers(mMaybeLayout, maybeList, maybeWorkers);
        initializeListOfWorkers(mNoLayout, noList, noWorkers);

        mWorkButton.setOnClickListener(mWorkClickedListener);
        mWorkButton.setEnabled(!repositoryData.getEvent().isPast());
        mWorkButton.setVisibility(View.VISIBLE);
    }

    private void initializeListOfWorkers(View listLayout, LinearLayout listView, List<InternalEventWorker> workers) {
        if (null == workers || workers.isEmpty()) {
            listLayout.setVisibility(View.GONE);
            return;
        }

        View mainView = getView();
        if (null == mainView) {
            return;
        }

        int marginWidth = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        int listWidth = mainView.getWidth() - 2 * marginWidth;
        double widthHour = listWidth / (double) (InternalEventWorker.RANGE_MAX_HOUR - InternalEventWorker.RANGE_MIN_HOUR);

        listView.removeAllViews();

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        for (InternalEventWorker worker : workers) {
            View view = layoutInflater.inflate(R.layout.list_item_internal_event_worker, null);

            TextView nameText = (TextView) view.findViewById(R.id.event_worker_name);
            nameText.setText(worker.getName());

            TextView commentText = (TextView) view.findViewById(R.id.event_worker_comment);
            commentText.setText(TextUtils.isEmpty(worker.getComment()) ? "-" : worker.getComment());

            TextView rangeTextView = (TextView) view.findViewById(R.id.event_worker_range_text);
            View rangeView = view.findViewById(R.id.event_worker_range);
            View rangeBgView = view.findViewById(R.id.event_worker_range_bg);
            View rangeEmptyView = view.findViewById(R.id.event_worker_range_empty);

            if (worker.hasRange()) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rangeView.getLayoutParams();
                params.leftMargin = (int) Math.ceil((worker.getRangeStart() - InternalEventWorker.RANGE_MIN_HOUR) * widthHour);
                params.width = (int) Math.ceil((worker.getRangeEnd() - worker.getRangeStart()) * widthHour);
                rangeView.setLayoutParams(params);

                rangeView.setVisibility(View.VISIBLE);
                rangeBgView.setVisibility(View.VISIBLE);
                rangeEmptyView.setVisibility(View.GONE);

                int rangeStart = worker.getRangeStart() % 24, rangeEnd = worker.getRangeEnd() % 24;
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
        String workersStr = getString(workersMax == 1 ? R.string.event_worker : R.string.event_workers);
        mNumberOfWorkersText.setText(String.valueOf(workersCount) + ' ' + getString(R.string.event_workers_of) + ' ' + String.valueOf(workersMax) + ' ' + workersStr);
    }

    private void setProgressBarVisible(boolean visible) {
        if (visible) {
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
            Bundle args = new Bundle();

            InternalEventWorkDialogFragment dialog = new InternalEventWorkDialogFragment();
            dialog.setArguments(args);
            //dialog.setListener(mAttendingDialogListener);
            dialog.show(getFragmentManager(), ExternalEventAttendDialogFragment.class.getSimpleName());
        }
    }
}
