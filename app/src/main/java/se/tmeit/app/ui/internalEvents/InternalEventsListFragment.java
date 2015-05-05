package se.tmeit.app.ui.internalEvents;

import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.InternalEvent;
import se.tmeit.app.services.Repository;
import se.tmeit.app.ui.ListFragmentBase;

/**
 * Fragment for the list of Internal events.
 */
public class InternalEventsListFragment extends ListFragmentBase {
    private static final String STATE_LIST_VIEW = "intEventsListState";
    private final RepositoryResultHandler mRepositoryResultHandler = new RepositoryResultHandler();
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

    private final class RepositoryResultHandler implements Repository.RepositoryResultHandler<List<InternalEvent>> {

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
