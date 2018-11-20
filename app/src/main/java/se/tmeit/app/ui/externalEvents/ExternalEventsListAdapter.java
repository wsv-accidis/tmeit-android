package se.tmeit.app.ui.externalEvents;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.ExternalEvent;

/**
 * List adapter for external events list.
 */
public final class ExternalEventsListAdapter extends BaseAdapter {
	private static final String FORMAT_SEPARATOR = " - ";
	private static final char FORMAT_SPACE = ' ';
	private final Context mContext;
	private final List<ExternalEvent> mExternalEvents;
	private final LayoutInflater mInflater;

	public ExternalEventsListAdapter(Context context, List<ExternalEvent> externalEvents) {
		mExternalEvents = externalEvents;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
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

		final ExternalEvent event = mExternalEvents.get(position);

		final TextView dateView = view.findViewById(R.id.event_date);
		dateView.setText(event.startDate());

		final TextView titleView = view.findViewById(R.id.event_title);
		titleView.setTextColor(ContextCompat.getColor(mContext, event.isPast() ? android.R.color.tertiary_text_light : android.R.color.primary_text_light));
		titleView.setText(event.title());
		titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, getEventAttendingIcon(event), 0);

		final TextView descriptionView = view.findViewById(R.id.event_description);
		descriptionView.setText(getEventDescription(event));
		descriptionView.setCompoundDrawablesWithIntrinsicBounds(0, 0, getEventSignupIcon(event), 0);

		return view;
	}

	private static int getEventAttendingIcon(ExternalEvent event) {
		return event.isAttending() ? R.drawable.ic_check_circle : 0;
	}

	private String getEventDescription(ExternalEvent event) {
		final StringBuilder builder = new StringBuilder();

		if (event.isPastSignup()) {
			builder.append(mContext.getString(R.string.event_last_signup_passed));
		} else {
			builder.append(mContext.getString(R.string.event_last_signup_date))
				.append(FORMAT_SPACE)
				.append(event.lastSignupDate());
		}

		builder.append(FORMAT_SEPARATOR).append(event.numberOfAttendees()).append(FORMAT_SPACE);

		if (1 == event.numberOfAttendees()) {
			builder.append(mContext.getString(R.string.event_attendee));
		} else {
			builder.append(mContext.getString(R.string.event_attendees));
		}

		return builder.toString();
	}

	private static int getEventSignupIcon(ExternalEvent event) {
		if (event.isPast()) {
			return 0;
		} else if (event.isPastSignup()) {
			return R.drawable.ic_error_red_small;
		} else if (event.isNearSignup()) {
			return R.drawable.ic_warning_amber_small;
		} else {
			return 0;
		}
	}
}
