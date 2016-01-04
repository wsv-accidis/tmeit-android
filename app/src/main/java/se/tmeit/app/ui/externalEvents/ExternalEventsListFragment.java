package se.tmeit.app.ui.externalEvents;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.ExternalEvent;
import se.tmeit.app.services.Repository;
import se.tmeit.app.services.RepositoryResultHandler;
import se.tmeit.app.ui.ListFragmentBase;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for the list of external events.
 */
public final class ExternalEventsListFragment extends ListFragmentBase implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
	private static final String STATE_LIST_VIEW = "extEventsListState";
	private static final String TAG = ExternalEventsListFragment.class.getSimpleName();
	private final ExternalEventsResultHandler mRepositoryResultHandler = new ExternalEventsResultHandler();
	private List<ExternalEvent> mEvents;
	private ExternalEventsListAdapter mListAdapter;

	@Override
	public int getItemId() {
		return R.id.nav_event_external;
	}

	@Override
	public int getTitle() {
		return R.string.event_external_nav_title;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (position >= 0 && position < mListAdapter.getCount()) {
			ExternalEvent event = (ExternalEvent) mListAdapter.getItem(position);
			Fragment eventInfoFragment = ExternalEventInfoFragment.createInstance(event);
			Activity activity = getActivity();
			if (activity instanceof MainActivity) {
				saveInstanceState();
				MainActivity mainActivity = (MainActivity) activity;
				mainActivity.openFragment(eventInfoFragment);
			} else {
				Log.e(TAG, "Activity holding fragment is not MainActivity!");
			}
		}
	}

	@Override
	protected void getDataFromRepository(Repository repository) {
		repository.getExternalEvents(mRepositoryResultHandler, getPreferences().shouldRefreshExternalEvents());
		getPreferences().setShouldRefreshExternalEvents(false);
	}

	@Override
	protected String getStateKey() {
		return STATE_LIST_VIEW;
	}

	@Override
	protected void initializeList() {
		mListAdapter = new ExternalEventsListAdapter(getActivity(), mEvents);
		finishInitializeList(mListAdapter);
	}

	private final class ExternalEventsResultHandler implements RepositoryResultHandler<List<ExternalEvent>> {
		@Override
		public void onError(int errorMessage) {
			mEvents = Collections.emptyList();
			onRepositoryError(errorMessage);
		}

		@Override
		public void onSuccess(List<ExternalEvent> result) {
			mEvents = result;
			onRepositorySuccess();
		}
	}
}
