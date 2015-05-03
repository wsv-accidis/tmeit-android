package se.tmeit.app.ui.externalEvents;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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

    private final class ExternalEventsListAdapter extends BaseAdapter {
        private final List<ExternalEvent> mExternalEvents;
        private final LayoutInflater mInflater;
        private final Resources mResources;

        public ExternalEventsListAdapter(Context context, List<ExternalEvent> externalEvents) {
            mExternalEvents = externalEvents;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mResources = context.getResources();
        }

        @Override
        public int getCount() {
            return mExternalEvents.size();
        }

        @Override
        public Object getItem(int position) {
            return mExternalEvents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item_external_event, parent, false);
            } else {
                view = convertView;
            }

            ExternalEvent event = mExternalEvents.get(position);

            TextView dateView = (TextView) view.findViewById(R.id.event_date);
            dateView.setText(event.getStartDate());

            TextView titleView = (TextView) view.findViewById(R.id.event_title);
            titleView.setText(event.getTitle());
            // TODO Set setCompoundDrawables (right) if signed up

            TextView descriptionView = (TextView) view.findViewById(R.id.event_description);
            descriptionView.setText(getEventDescription(event));
            // TODO Set setCompoundDrawables (left) depending on flags

            return view;
        }

        private String getEventDescription(ExternalEvent event) {
            StringBuilder builder = new StringBuilder();

            if (event.isPastSignup()) {
                builder.append(mResources.getString(R.string.event_last_signup_passed));
            } else {
                builder.append(mResources.getString(R.string.event_last_signup_date))
                        .append(' ')
                        .append(event.getLastSignupDate());
            }

            builder.append(" - ").append(event.getNumberOfAttendees());

            if (1 == event.getNumberOfAttendees()) {
                builder.append(mResources.getString(R.string.event_attendee));
            } else {
                builder.append(mResources.getString(R.string.event_attendees));
            }

            return builder.toString();
        }
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
