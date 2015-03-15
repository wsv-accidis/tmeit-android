package se.tmeit.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;

import se.tmeit.app.R;
import se.tmeit.app.notifications.GcmRegistration;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.notifications.NotificationsFragment;
import se.tmeit.app.ui.onboarding.OnboardingActivity;

public final class MainActivity extends ActionBarActivity {
    private static final String STATE_LAST_OPENED_FRAGMENT = "openMainActivityFragment";
    private static final String TAG = MainActivity.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private NavigationItem mOpenFragmentItem;
    private Preferences mPrefs;
    private CharSequence mTitle;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen if the drawer is not showing. Otherwise, let the drawer decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles clicks from the action bar, which we don't currently use.
        // The menu used is populated from menu/main.xml or menu/global.xml

        /*
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // DO STUFF
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    public void setMainTitle(int resId) {
        if (0 == resId) {
            resId = R.string.app_name;
        }
        mTitle = getString(resId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = new Preferences(this);
        mTitle = getTitle();

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setNavigationDrawerCallbacks(new NavigationDrawerCallbacks());
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        if (null != savedInstanceState) {
            mOpenFragmentItem = NavigationItem.fromPosition(savedInstanceState.getInt(STATE_LAST_OPENED_FRAGMENT));
            if (null == mOpenFragmentItem) {
                mOpenFragmentItem = NavigationItem.getDefault();
            }
        } else {
            mOpenFragmentItem = NavigationItem.getDefault();
        }

        openFragment(mOpenFragmentItem);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mPrefs.hasServiceAuthentication()) {
            Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
            startActivity(intent);
            finish();
        } else {
            validateAndRegisterServicesIfNeeded();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_LAST_OPENED_FRAGMENT, mOpenFragmentItem.getPosition());
    }

    private static Fragment getFragmentByDrawerItem(NavigationItem item) {
        switch (item) {
            case ABOUT_ITEM:
                return new AboutFragment();
            case NOTIFICATIONS_ITEM:
                return new NotificationsFragment();
        }

        Log.e(TAG, "Trying to navigate to unrecognized fragment " + item + ".");
        return null;
    }

    private void openFragment(NavigationItem item) {
        Fragment nextFragment = getFragmentByDrawerItem(item);
        if (null != nextFragment) {
            mOpenFragmentItem = item;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, nextFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private void validateAndRegisterServicesIfNeeded() {
        // TODO Validate auth to make sure it hasn't expired
        GcmRegistration.getInstance(this).registerIfRegistrationExpired(new RegistrationResultHandler());
    }

    public static abstract class MainActivityFragment extends Fragment {
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            if (activity instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) activity;
                mainActivity.setMainTitle(getTitle());
            }
        }

        protected abstract int getTitle();
    }

    private final class NavigationDrawerCallbacks implements NavigationDrawerFragment.NavigationDrawerCallbacks {
        @Override
        public void onNavigationDrawerItemSelected(NavigationItem item) {
            openFragment(item);
        }
    }

    private final class RegistrationResultHandler implements GcmRegistration.RegistrationResultHandler {
        @Override
        public void onError(final int errorMessage) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    refreshNotificationsFragment();
                    Toast toast = Toast.makeText(MainActivity.this, getString(errorMessage), Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

        @Override
        public void onGoogleServicesError(final int resultCode, final boolean canRecover) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    refreshNotificationsFragment();
                    if (canRecover) {
                        GooglePlayServicesUtil.getErrorDialog(resultCode, MainActivity.this, GcmRegistration.PLAY_SERVICES_RESOLUTION_REQUEST).show();
                    } else {
                        Toast toast = Toast.makeText(MainActivity.this, R.string.notifications_your_device_does_not_support, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            });
        }

        @Override
        public void onSuccess() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    refreshNotificationsFragment();
                }
            });
        }

        private void refreshNotificationsFragment() {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (fragment instanceof NotificationsFragment) {
                NotificationsFragment notificationsFragment = (NotificationsFragment) fragment;
                notificationsFragment.refreshNotificationsState();
            }
        }
    }
}
