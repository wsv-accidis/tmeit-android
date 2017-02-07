package se.tmeit.app.ui.internalEvents;

import android.support.v4.app.Fragment;

import se.tmeit.app.R;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for reporting work done on internal event.
 */
public final class InternalEventWorkFragment extends Fragment implements MainActivity.HasTitle, MainActivity.HasNavigationItem {
	@Override
	public int getItemId() {
		return R.id.nav_event_internal;
	}

	@Override
	public int getTitle() {
		return R.string.event_report_event;
	}
}
