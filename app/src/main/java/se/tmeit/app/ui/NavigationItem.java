package se.tmeit.app.ui;

import android.content.res.Resources;
import android.support.v4.app.Fragment;

import se.tmeit.app.R;
import se.tmeit.app.ui.externalEvents.ExternalEventsListFragment;
import se.tmeit.app.ui.internalEvents.InternalEventsListFragment;
import se.tmeit.app.ui.members.MembersListFragment;
import se.tmeit.app.ui.notifications.NotificationsFragment;
import se.tmeit.app.ui.uploadPhoto.UploadPhotoFragment;

/**
 * Holds the set of navigable items used by the navigation drawer and main activity.
 */
public enum NavigationItem {
    MEMBERS_ITEM(0),
    INTERNAL_EVENTS_ITEM(1),
    EXTERNAL_EVENTS_ITEM(2),
    UPLOAD_PHOTO_ITEM(3),
    AGE_CHECK_ITEM(4),
    NOTIFICATIONS_ITEM(5),
    ABOUT_ITEM(6);

    private final int mPosition;

    NavigationItem(int position) {
        mPosition = position;
    }

    public static String[] asTitles(Resources resources) {
        // The index of each item in this array _MUST_ match its position in the enum
        return new String[]{
                resources.getString(R.string.members_nav_title),
                resources.getString(R.string.event_internal_nav_title),
                resources.getString(R.string.event_external_nav_title),
                resources.getString(R.string.upload_photo_nav_title),
                resources.getString(R.string.age_check_nav_title),
                resources.getString(R.string.notifications_nav_title),
                resources.getString(R.string.about_nav_title)
        };
    }

    public static Fragment createFragment(NavigationItem item) {
        switch (item) {
            case ABOUT_ITEM:
                return new AboutFragment();
            case AGE_CHECK_ITEM:
                return new AgeCheckFragment();
            case EXTERNAL_EVENTS_ITEM:
                return new ExternalEventsListFragment();
            case INTERNAL_EVENTS_ITEM:
                return new InternalEventsListFragment();
            case MEMBERS_ITEM:
                return new MembersListFragment();
            case NOTIFICATIONS_ITEM:
                return new NotificationsFragment();
            case UPLOAD_PHOTO_ITEM:
                return new UploadPhotoFragment();
        }

        return null;
    }

    public static NavigationItem fromPosition(int position) {
        for (NavigationItem item : NavigationItem.values()) {
            if (position == item.getPosition()) {
                return item;
            }
        }

        return null;
    }

    public static NavigationItem getDefault() {
        return MEMBERS_ITEM;
    }

    public int getPosition() {
        return mPosition;
    }
}
