package se.tmeit.app.ui.internalEvents;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import se.tmeit.app.R;
import se.tmeit.app.model.InternalEvent;

/**
 * List adapter for internal events list.
 */
public class InternalEventsListAdapter extends BaseAdapter {
	private static final String FORMAT_DESCRIPTION = "%s - %d/%d %s";
	private final Context mContext;
	private final LayoutInflater mInflater;
	private final List<InternalEvent> mInternalEvents;

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

		final InternalEvent event = mInternalEvents.get(position);

		final TextView dateView = view.findViewById(R.id.event_date);
		dateView.setText(event.startDate());

		final TextView timeView = view.findViewById(R.id.event_time);
		timeView.setText(event.startTime());

		final TextView titleView = view.findViewById(R.id.event_title);
		titleView.setTextColor(ContextCompat.getColor(mContext, event.isPast() ? android.R.color.tertiary_text_light : android.R.color.primary_text_light));
		titleView.setText(event.title());

		final TextView descriptionView = view.findViewById(R.id.event_description);
		descriptionView.setText(getEventDescription(event));

		return view;
	}

	private String getEventDescription(InternalEvent event) {
		return String.format(Locale.getDefault(), FORMAT_DESCRIPTION, event.teamTitle(), event.workersCount(), event.workersMax(), mContext.getString(R.string.event_workers));
	}
}