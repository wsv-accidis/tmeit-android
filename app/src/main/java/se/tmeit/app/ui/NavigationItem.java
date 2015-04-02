package se.tmeit.app.ui;

import android.content.res.Resources;

import se.tmeit.app.R;

/**
 * Holds the set of navigable items used by the navigation drawer and main activity.
 */
public enum NavigationItem {
    MEMBERS_ITEM(0),
    NOTIFICATIONS_ITEM(1),
    ABOUT_ITEM(2);

    private final int mPosition;

    private NavigationItem(int position) {
        mPosition = position;
    }

    public static String[] asTitles(Resources resources) {
        // The index of each item in this array _MUST_ match its position in the enum
        return new String[]{
                resources.getString(R.string.members_nav_title),
                resources.getString(R.string.notifications_nav_title),
                resources.getString(R.string.about_nav_title)
        };
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
        return NOTIFICATIONS_ITEM;
    }

    public int getPosition() {
        return mPosition;
    }
}
