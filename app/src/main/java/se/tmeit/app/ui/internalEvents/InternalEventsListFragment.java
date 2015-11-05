package se.tmeit.app.ui.internalEvents;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.ExternalEvent;
import se.tmeit.app.model.InternalEvent;
import se.tmeit.app.services.Repository;
import se.tmeit.app.services.RepositoryResultHandler;
import se.tmeit.app.ui.ListFragmentBase;
import se.tmeit.app.ui.MainActivity;
import se.tmeit.app.ui.NavigationItem;
import se.tmeit.app.ui.externalEvents.ExternalEventInfoFragment;

/**
 * Fragment for the list of internal events.
 */
public final class InternalEventsListFragment extends ListFragmentBase implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
    private static final String STATE_LIST_VIEW = "intEventsListState";
    private static final String TAG = InternalEventsListFragment.class.getSimpleName();
    private final InternalEventsResultHandler mRepositoryResultHandler = new InternalEventsResultHandler();
    private List<InternalEvent> mEvents;
    private InternalEventsListAdapter mListAdapter;

    @Override
    public NavigationItem getItem() {
        return NavigationItem.INTERNAL_EVENTS_ITEM;
    }

    @Override
    public int getTitle() {
        return R.string.event_internal_nav_title;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position >= 0 && position < mListAdapter.getCount()) {
            InternalEvent event = (InternalEvent) mListAdapter.getItem(position);
            Fragment eventInfoFragment = InternalEventInfoFragment.createInstance(event);
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
        repository.getInternalEvents(mRepositoryResultHandler);
    }

    @Override
    protected String getStateKey() {
        return STATE_LIST_VIEW;
    }

    @Override
    protected void initializeList() {
        mListAdapter = new InternalEventsListAdapter(getActivity(), mEvents);
        finishInitializeList(mListAdapter);
    }

    private final class InternalEventsResultHandler implements RepositoryResultHandler<List<InternalEvent>> {
        @Override
        public void onError(int errorMessage) {
            mEvents = Collections.emptyList();
            onRepositoryError(errorMessage);
        }

        @Override
        public void onSuccess(List<InternalEvent> result) {
            mEvents = result;
            onRepositorySuccess();
        }
    }
}
