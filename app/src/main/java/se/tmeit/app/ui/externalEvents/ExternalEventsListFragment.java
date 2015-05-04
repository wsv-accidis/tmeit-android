package se.tmeit.app.ui.externalEvents;

import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.ExternalEvent;
import se.tmeit.app.services.Repository;
import se.tmeit.app.ui.ListFragmentBase;

/**
 * Fragment for the list of external events.
 */
public final class ExternalEventsListFragment extends ListFragmentBase {
    private static final String STATE_LIST_VIEW = "extEventsListState";
    private final RepositoryResultHandler mRepositoryResultHandler = new RepositoryResultHandler();
    private List<ExternalEvent> mEvents;

    @Override
    public int getTitle() {
        return R.string.event_external_nav_title;
    }

    @Override
    protected void getDataFromRepository(Repository repository) {
        repository.getExternalEvents(mRepositoryResultHandler);
    }

    @Override
    protected String getStateKey() {
        return STATE_LIST_VIEW;
    }

    @Override
    protected void initializeList() {
        ExternalEventsListAdapter listAdapter = new ExternalEventsListAdapter(getActivity(), mEvents);
        finishInitializeList(listAdapter);
    }

    private final class RepositoryResultHandler implements Repository.RepositoryResultHandler<List<ExternalEvent>> {
        @Override
        public void onError(int errorMessage) {
            mEvents = null;
            onRepositoryError(errorMessage);
        }

        @Override
        public void onSuccess(List<ExternalEvent> result) {
            mEvents = result;
            onRepositorySuccess();
        }
    }
}
