package se.tmeit.app.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;

import se.tmeit.app.R;
import se.tmeit.app.notifications.GcmRegistration;
import se.tmeit.app.services.AuthenticationResultHandler;
import se.tmeit.app.services.ServiceAuthenticator;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.notifications.NotificationsFragment;
import se.tmeit.app.ui.onboarding.OnboardingActivity;
import se.tmeit.app.utils.AndroidUtils;

public final class MainActivity extends AppCompatActivity {
    private static final String STATE_LAST_OPENED_FRAGMENT = "openMainActivityFragment";
    private static final String STATE_LAST_OPENED_FRAGMENT_POS = "openMainActivityFragmentPos";
    private static final String TAG = MainActivity.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private boolean mHasShownNetworkAlert;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private NavigationItem mOpenFragmentItem;
    private HasMenu mOptionsMenu;
    private Preferences mPrefs;
    private CharSequence mTitle;

    public static void showNoNetworkAlert(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.network_error_not_available)
                .setTitle(R.string.network_error_not_available_title)
                .setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen if the drawer is not showing. Otherwise, let the drawer decide what to show in the action bar.
            int menuId = (null == mOptionsMenu ? R.menu.main : mOptionsMenu.getMenu());
            getMenuInflater().inflate(menuId, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (null != mOptionsMenu && mOptionsMenu.onMenuItemSelected(item)) || super.onOptionsItemSelected(item);
    }

    public void openFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
        updateViewFromFragment(fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = new Preferences(this);
        mTitle = getTitle();

        final FragmentManager fragmentManager = getSupportFragmentManager();
        mNavigationDrawerFragment = (NavigationDrawerFragment) fragmentManager.findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setNavigationDrawerCallbacks(new NavigationDrawerCallbacks());
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        if (null != savedInstanceState) {
            mOpenFragmentItem = NavigationItem.fromPosition(savedInstanceState.getInt(STATE_LAST_OPENED_FRAGMENT_POS));
            if (null == mOpenFragmentItem) {
                mOpenFragmentItem = NavigationItem.getDefault();
                openNavigationItem(mOpenFragmentItem);
            } else {
                Fragment lastFragment = fragmentManager.getFragment(savedInstanceState, STATE_LAST_OPENED_FRAGMENT);
                openFragment(lastFragment, false);
            }
        } else if (null == mOpenFragmentItem) {
            mOpenFragmentItem = NavigationItem.getDefault();
            openNavigationItem(mOpenFragmentItem);
        }

        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = fragmentManager.findFragmentById(R.id.container);
                if (null != fragment) {
                    updateViewFromFragment(fragment);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mPrefs.hasServiceAuthentication()) {
            startOnboardingActivity();
        } else {
            if (!mHasShownNetworkAlert && !AndroidUtils.isNetworkConnected(this)) {
                mHasShownNetworkAlert = true;
                showNoNetworkAlert(this);
            }

            validateAndRegisterServicesIfNeeded();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.container);
        if (null != fragment) {
            fragmentManager.putFragment(outState, STATE_LAST_OPENED_FRAGMENT, fragment);
        }

        outState.putInt(STATE_LAST_OPENED_FRAGMENT_POS, mOpenFragmentItem.getPosition());
    }

    private void openNavigationItem(NavigationItem item) {
        Fragment nextFragment = NavigationItem.createFragment(item);
        if (null != nextFragment) {
            mOpenFragmentItem = item;
            openFragment(nextFragment, false);
        } else {
            Log.e(TAG, "Trying to navigate to unrecognized fragment " + item + ".");
        }
    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    private void setMainTitle(int resId) {
        if (0 == resId) {
            resId = R.string.app_name;
        }
        mTitle = getString(resId);
    }

    private void startOnboardingActivity() {
        Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateViewFromFragment(Fragment fragment) {
        if (fragment instanceof HasTitle) {
            HasTitle fragmentWithTitle = (HasTitle) fragment;
            setMainTitle(fragmentWithTitle.getTitle());
        } else {
            setMainTitle(0);
        }

        if (fragment instanceof HasMenu) {
            mOptionsMenu = (HasMenu) fragment;
            fragment.setHasOptionsMenu(true);
        } else {
            mOptionsMenu = null;
            fragment.setHasOptionsMenu(false);
        }

        restoreActionBar();
    }

    private void validateAndRegisterServicesIfNeeded() {
        String username = mPrefs.getAuthenticatedUser(), serviceAuth = mPrefs.getServiceAuthentication();
        ServiceAuthenticator authenticator = new ServiceAuthenticator();
        authenticator.authenticateFromCredentials(username, serviceAuth, new MainAuthenticationResultHandler());
    }

    public interface HasMenu {
        int getMenu();

        boolean onMenuItemSelected(MenuItem item);
    }

    public interface HasTitle {
        int getTitle();
    }

    private final class MainAuthenticationResultHandler implements AuthenticationResultHandler {
        @Override
        public void onAuthenticationError(int errorMessage) {
            showErrorMessage(errorMessage);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startOnboardingActivity();
                }
            });
        }

        @Override
        public void onNetworkError(final int errorMessage) {
            showErrorMessage(errorMessage);
        }

        @Override
        public void onProtocolError(int errorMessage) {
            showErrorMessage(errorMessage);
        }

        @Override
        public void onSuccess(String serviceAuth, String authenticatedUser) {
            GcmRegistration.getInstance(MainActivity.this)
                    .registerIfRegistrationExpired(new RegistrationResultHandler());
        }

        private void showErrorMessage(final int errorMessage) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }

    private final class NavigationDrawerCallbacks implements NavigationDrawerFragment.NavigationDrawerCallbacks {
        @Override
        public void onNavigationDrawerItemSelected(NavigationItem item) {
            openNavigationItem(item);
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
                        GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, resultCode, GcmRegistration.PLAY_SERVICES_RESOLUTION_REQUEST).show();
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
