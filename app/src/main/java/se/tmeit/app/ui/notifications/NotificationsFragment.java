package se.tmeit.app.ui.notifications;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.tmeit.app.R;
import se.tmeit.app.ui.MainActivity;

/**
 * Fragment for configuring notification settings.
 */
public class NotificationsFragment extends MainActivity.MainActivityFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    protected int getTitle() {
        return R.string.notifications_title;
    }
}
