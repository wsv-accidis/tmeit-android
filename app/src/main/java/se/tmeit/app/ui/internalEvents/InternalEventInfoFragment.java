package se.tmeit.app.ui.internalEvents;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.tmeit.app.R;
import se.tmeit.app.model.ExternalEvent;
import se.tmeit.app.model.InternalEvent;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for an internal event.
 */
public final class InternalEventInfoFragment extends Fragment implements MainActivity.HasTitle {
    public static InternalEventInfoFragment createInstance(InternalEvent event) {
        Bundle bundle = new Bundle();
        bundle.putInt(ExternalEvent.Keys.ID, event.getId());
        //bundle.putString(ExternalEvent.Keys.TITLE, event.getTitle());
        //bundle.putString(ExternalEvent.Keys.START_DATE, event.getStartDate());
        //bundle.putString(ExternalEvent.Keys.LAST_SIGNUP, event.getLastSignupDate());

        InternalEventInfoFragment instance = new InternalEventInfoFragment();
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public int getTitle() {
        return R.string.event_external_info_nav_title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_internal_event_info, container, false);

        Bundle args = getArguments();


        return view;
    }
}
