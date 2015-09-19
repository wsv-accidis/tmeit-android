package se.tmeit.app.ui.internalEvents;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.InternalEvent;

/**
 * List adapter for internal events list.
 */
public class InternalEventsListAdapter extends BaseAdapter {
    private static final String FORMAT_DESCRIPTION = "%s - %d/%d %s";
    private final List<InternalEvent> mInternalEvents;
    private final LayoutInflater mInflater;
    private final Context mContext;

    public InternalEventsListAdapter(Context context, List<InternalEvent> internalEvents) {
        mInternalEvents = internalEvents;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
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
        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item_internal_event, parent, false);
        } else {
            view = convertView;
        }

        InternalEvent event = mInternalEvents.get(position);

        TextView dateView = (TextView) view.findViewById(R.id.event_date);
        dateView.setText(event.getStartDate());

        TextView timeView = (TextView) view.findViewById(R.id.event_time);
        timeView.setText(event.getStartTime());

        TextView titleView = (TextView) view.findViewById(R.id.event_title);
        titleView.setTextColor(ContextCompat.getColor(mContext, event.isPast() ? android.R.color.tertiary_text_light : android.R.color.primary_text_light));
        titleView.setText(event.getTitle());

        TextView descriptionView = (TextView) view.findViewById(R.id.event_description);
        descriptionView.setText(getEventDescription(event));

        return view;
    }

    private String getEventDescription(InternalEvent event) {
        return String.format(FORMAT_DESCRIPTION, event.getTeamTitle(), event.getWorkersCount(), event.getWorkersMax(), mContext.getString(R.string.event_workers));
    }
}