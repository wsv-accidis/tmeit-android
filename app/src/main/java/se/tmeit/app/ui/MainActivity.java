package se.tmeit.app.ui;

import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import se.tmeit.app.R;
import se.tmeit.app.notifications.GcmRegistration;
import se.tmeit.app.services.AuthenticationResultHandler;
import se.tmeit.app.services.ServiceAuthenticator;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.externalEvents.ExternalEventsListFragment;
import se.tmeit.app.ui.internalEvents.InternalEventsListFragment;
import se.tmeit.app.ui.members.MembersListFragment;
import se.tmeit.app.ui.notifications.NotificationsFragment;
import se.tmeit.app.ui.onboarding.OnboardingActivity;
import se.tmeit.app.ui.uploadPhoto.UploadPhotoFragment;
import se.tmeit.app.utils.AndroidUtils;

public final class MainActivity extends AppCompatActivity {
	private static final String STATE_LAST_OPENED_FRAGMENT = "openMainActivityFragment";
	private static final String TAG = MainActivity.class.getSimpleName();
	private final BroadcastReceiver mGcmRegistrationBroadcastReceiver = new GcmRegistrationBroadcastReceiver();
	private final Handler mHandler = new Handler();
	private boolean mHasShownNetworkAlert;
	private DrawerLayout mNavigationDrawer;
	private NavigationView mNavigationView;
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
	public void onBackPressed() {
		if (mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
			mNavigationDrawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (null != mOptionsMenu) {
			getMenuInflater().inflate(mOptionsMenu.getMenu(), menu);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return (null != mOptionsMenu && mOptionsMenu.onMenuItemSelected(item)) || super.onOptionsItemSelected(item);
	}

	public void openFragment(Fragment fragment) {
		openFragment(fragment, true);
	}

	public void openFragment(Fragment fragment, boolean addToBackStack) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment oldFragment = fragmentManager.findFragmentById(R.id.container);

		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.container, fragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

		if (addToBackStack && !isSameFragment(oldFragment, fragment)) {
			transaction.addToBackStack(null);
		}

		transaction.commit();
		updateViewFromFragment(fragment);
	}

	public void popFragmentFromBackStack() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.popBackStack();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPrefs = new Preferences(this);
		mTitle = getTitle();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mNavigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mNavigationDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		mNavigationDrawer.addDrawerListener(toggle);
		mNavigationDrawer.addDrawerListener(new NavigationDrawerListener());
		toggle.syncState();

		mNavigationView = (NavigationView) findViewById(R.id.nav_view);
		mNavigationView.setNavigationItemSelectedListener(new NavigationListener());

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.addOnBackStackChangedListener(new BackStackChangedListener());

		if (null != savedInstanceState) {
			Fragment lastFragment = fragmentManager.getFragment(savedInstanceState, STATE_LAST_OPENED_FRAGMENT);
			openFragment(lastFragment, false);
		} else {
			openFragment(getFragmentByNavigationItem(R.id.nav_members), false);
		}
		Intent intent = getIntent();
		if(intent != null && intent.getAction().equalsIgnoreCase(Intent.ACTION_SEND))
		{
			setupFromIntent();
		}
	}

	private void setupFromIntent() {
		Intent intent = getIntent();
		if(intent == null) {
			return;
		}
		Bundle extras = intent.getExtras();
		intent.getData();
		ClipData data = intent.getClipData();
		if (data != null)
		{
			Uri sourceURI =data.getItemAt(0).getUri();
			extras.putString(UploadPhotoFragment.PHOTO, sourceURI.toString());
		}

		UploadPhotoFragment uploadPhotoFragment = new UploadPhotoFragment();
		uploadPhotoFragment.setArguments(extras);
		openFragment(uploadPhotoFragment, false);

	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mGcmRegistrationBroadcastReceiver);
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

			// TODO Perhaps we don't need to call this if we already did a few seconds ago (e.g. screen rotation)
			validateAndRegisterServicesIfNeeded();
		}

		LocalBroadcastManager.getInstance(this)
			.registerReceiver(mGcmRegistrationBroadcastReceiver, new IntentFilter(GcmRegistration.REGISTRATION_COMPLETE_BROADCAST));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentById(R.id.container);
		if (null != fragment) {
			fragmentManager.putFragment(outState, STATE_LAST_OPENED_FRAGMENT, fragment);
		}
	}

	private Fragment getFragmentByNavigationItem(int navId) {
		switch (navId) {
			case R.id.nav_about:
				return new AboutFragment();
			case R.id.nav_age_check:
				return new AgeCheckFragment();
			case R.id.nav_event_external:
				return new ExternalEventsListFragment();
			case R.id.nav_event_internal:
				return new InternalEventsListFragment();
			case R.id.nav_members:
				return new MembersListFragment();
			case R.id.nav_notifications:
				return new NotificationsFragment();
			case R.id.nav_upload_photo:
				return new UploadPhotoFragment();
		}

		return null;
	}

	private boolean isSameFragment(Fragment oldFragment, Fragment newFragment) {
		return null != oldFragment && oldFragment.getClass().getName().equals(newFragment.getClass().getName());
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
		Intent intent = new Intent(this, OnboardingActivity.class);
		startActivity(intent);
		finish();
	}

	private void updateViewFromFragment(Fragment fragment) {
		if (fragment instanceof HasNavigationItem) {
			HasNavigationItem fragmentWithNavItem = (HasNavigationItem) fragment;
			mNavigationView.setCheckedItem(fragmentWithNavItem.getItemId());
		}

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
		String username = mPrefs.getAuthenticatedUserName(), serviceAuth = mPrefs.getServiceAuthentication();
		ServiceAuthenticator authenticator = new ServiceAuthenticator();
		authenticator.authenticateFromCredentials(username, serviceAuth, new MainAuthenticationResultHandler());
	}

	public interface HasMenu {
		int getMenu();

		boolean onMenuItemSelected(MenuItem item);
	}

	public interface HasNavigationItem {
		int getItemId();
	}

	public interface HasTitle {
		int getTitle();
	}

	private final class BackStackChangedListener implements FragmentManager.OnBackStackChangedListener {
		@Override
		public void onBackStackChanged() {
			Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
			if (null != fragment) {
				updateViewFromFragment(fragment);
			}
		}
	}

	private final class GcmRegistrationBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
			if (fragment instanceof NotificationsFragment) {
				NotificationsFragment notificationsFragment = (NotificationsFragment) fragment;
				notificationsFragment.refreshNotificationsState();
			}
		}
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
		public void onSuccess(String serviceAuth, String userName, int userId) {
			mPrefs.setServiceAuthentication(serviceAuth);
			mPrefs.setAuthenticatedUser(userName, userId);
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

	private final class NavigationDrawerListener implements DrawerLayout.DrawerListener {
		@Override
		public void onDrawerClosed(View drawerView) {
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			AndroidUtils.hideSoftKeyboard(MainActivity.this, getCurrentFocus());
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {
		}

		@Override
		public void onDrawerStateChanged(int newState) {
		}
	}

	private final class NavigationListener implements NavigationView.OnNavigationItemSelectedListener {
		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			Fragment fragment = getFragmentByNavigationItem(item.getItemId());
			if (null != fragment) {
				openFragment(fragment);
			} else {
				Log.e(TAG, "Trying to navigate to unrecognized fragment.");
			}

			DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
			drawer.closeDrawer(GravityCompat.START);
			return true;
		}
	}
}
