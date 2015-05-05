package se.tmeit.app.ui.internalEvents;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.InternalEvent;

/**
 * List adapter for internal events list
 */
public class InternalEventsListAdapter extends BaseAdapter {
    private static final char FORMAT_SPACE = ' ';
    private static final char FORMAT_OF = '/';
    private static final String FORMAT_SEPARATOR = " - ";
    private final List<InternalEvent> mInternalEvents;
    private final LayoutInflater mInflater;
    private final Resources mResources;

    public InternalEventsListAdapter(Context context, List<InternalEvent> internalEvents) {
        mInternalEvents = internalEvents;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources = context.getResources();
    }

    @Override
    public int getCount() {
        return mInternalEvents.size();
    }

    @Override
    public Object getItem(int position) {
        return mInternalEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.list_item_external_event, parent, false);
        } else {
            view = convertView;
        }

        InternalEvent event = mInternalEvents.get(position);

        TextView dateView = (TextView) view.findViewById(R.id.event_date);
        dateView.setText(event.getStartDate());

        TextView titleView = (TextView) view.findViewById(R.id.event_title);
        titleView.setText(event.getTitle());

        TextView descriptionView = (TextView) view.findViewById(R.id.event_description);
        descriptionView.setText(getEventDescription(event));

        return view;
    }

    private String getEventDescription(InternalEvent event) {
        StringBuilder builder = new StringBuilder();

        builder.append(event.getTeamTitle())
                .append(FORMAT_SEPARATOR)
                .append(event.getWorkersCount())
                .append(FORMAT_OF)
                .append(event.getmWorkersMax())
                .append(FORMAT_SPACE)
                .append(mResources.getString(R.string.event_internal_workers));

        return builder.toString();
    }
}