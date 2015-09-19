package se.tmeit.app.ui.internalEvents;

import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.InternalEvent;
import se.tmeit.app.services.Repository;
import se.tmeit.app.services.RepositoryResultHandler;
import se.tmeit.app.ui.ListFragmentBase;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for the list of internal events.
 */
public final class InternalEventsListFragment extends ListFragmentBase implements MainActivity.HasTitle {
    private static final String STATE_LIST_VIEW = "intEventsListState";
    private final RepositoryResultHandler mRepositoryResultHandler = new InternalEventsResultHandler();
    private List<InternalEvent> mEvents;

    @Override
    public int getTitle() {
        return R.string.event_internal_nav_title;
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
        InternalEventsListAdapter listAdapter = new InternalEventsListAdapter(getActivity(), mEvents);
        finishInitializeList(listAdapter);
    }

    private final class InternalEventsResultHandler implements RepositoryResultHandler<List<InternalEvent>> {
        @Override
        public void onError(int errorMessage) {
            mEvents = null;
            onRepositoryError(errorMessage);
        }

        @Override
        public void onSuccess(List<InternalEvent> result) {
            mEvents = result;
            onRepositorySuccess();
        }
    }
}
