package de.xikolo.controllers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.xikolo.BuildConfig;
import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controllers.main.ContentFragment;
import de.xikolo.controllers.main.ContentWebViewFragment;
import de.xikolo.controllers.main.CourseListFragment;
import de.xikolo.controllers.main.ProfileFragment;
import de.xikolo.controllers.navigation.NavigationFragment;
import de.xikolo.controllers.navigation.adapter.NavigationAdapter;
import de.xikolo.events.LoginEvent;
import de.xikolo.events.LogoutEvent;
import de.xikolo.managers.UserManager;
import de.xikolo.storages.preferences.ApplicationPreferences;
import de.xikolo.storages.preferences.StorageType;
import de.xikolo.utils.BuildFlavor;
import de.xikolo.utils.Config;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.DeepLinkingUtil;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.PlayServicesUtil;

public class MainActivity extends BaseActivity
        implements NavigationFragment.NavigationDrawerCallbacks, ContentFragment.OnFragmentInteractionListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationFragment navigationFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence texTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();

        ApplicationPreferences appPreferences = (ApplicationPreferences) GlobalApplication.getStorage(StorageType.APP);
        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO && DateUtil.nowIsBetween("2017-05-21T00:00:00", "2017-05-31T23:59:59") && !appPreferences.onboardingShown()) {
            startActivity(new Intent(this, OnboardingActivity.class));
        }

        navigationFragment = (NavigationFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        navigationFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                toolbar);

        if (Config.DEBUG) {
            Log.i(TAG, "Build Type: " + BuildConfig.X_TYPE);
            Log.i(TAG, "Build Flavor: " + BuildConfig.X_FLAVOR);
        }

        // check Play Services, display dialog is update needed
        PlayServicesUtil.checkPlayServicesWithDialog(this);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            Log.d(TAG, action + " " + intent.getData());

            if (action != null && action.equals(Intent.ACTION_VIEW)) {
                Uri uri = intent.getData();

                DeepLinkingUtil.Type type = DeepLinkingUtil.getType(uri);
                if (type != null) {
                    switch (type) {
                        case ALL_COURSES:
                            navigationFragment.selectItem(NavigationAdapter.NAV_ALL_COURSES.getPosition());
                            break;
                        case NEWS:
                            navigationFragment.selectItem(NavigationAdapter.NAV_NEWS.getPosition());
                            break;
                        case MY_COURSES:
                            navigationFragment.selectItem(NavigationAdapter.NAV_MY_COURSES.getPosition());
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void onFragmentAttached(int id, String title) {
        setTitle(title);
        navigationFragment.markItem(id);
        navigationFragment.setDrawerIndicatorEnabled(true);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        String tag = null;
        Intent intent = null;
        ContentFragment newFragment = null;
        if (position == NavigationAdapter.NAV_PROFILE.getPosition()) {
            if (UserManager.isLoggedIn()) {
                newFragment = ProfileFragment.newInstance();
                tag = "profile";

                LanalyticsUtil.trackVisitedProfile(UserManager.getSavedUser().id);
            } else {
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }
        }
        if (position == NavigationAdapter.NAV_ALL_COURSES.getPosition()) {
            newFragment = CourseListFragment.newInstance(CourseListFragment.FILTER_ALL);
            tag = "all_courses";
        }
        if (position == NavigationAdapter.NAV_MY_COURSES.getPosition()) {
            newFragment = CourseListFragment.newInstance(CourseListFragment.FILTER_MY);
            tag = "my_courses";
        }
        if (position == NavigationAdapter.NAV_NEWS.getPosition()) {
            newFragment = ContentWebViewFragment.newInstance(NavigationAdapter.NAV_NEWS.getPosition(), Config.URI + Config.NEWS, getString(R.string.title_section_news), false, false);
            tag = "news";
        }
        if (position == NavigationAdapter.NAV_SECOND_SCREEN.getPosition()) {
            intent = new Intent(MainActivity.this, SecondScreenActivity.class);
        }
        if (position == NavigationAdapter.NAV_DOWNLOADS.getPosition()) {
            intent = new Intent(MainActivity.this, DownloadsActivity.class);

            LanalyticsUtil.trackVisitedDownloads(UserManager.getSavedUser().id);
        }
        if (position == NavigationAdapter.NAV_SETTINGS.getPosition()) {
            intent = new Intent(MainActivity.this, SettingsActivity.class);

            LanalyticsUtil.trackVisitedPreferences(UserManager.getSavedUser().id);
        }
        if (tag != null) {
            ContentFragment oldFragment = (ContentFragment) fragmentManager.findFragmentByTag(tag);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (oldFragment == null) {
                transaction.replace(R.id.container, newFragment, tag);
            } else {
                transaction.replace(R.id.container, oldFragment, tag);
            }
            transaction.addToBackStack(tag);
            transaction.commit();
            setAppBarExpanded(true);
        } else if (intent != null) {
            startActivity(intent);
        }
    }

    private void setTitle(String title) {
        texTitle = title;
        if (actionBar != null) {
            actionBar.setTitle(texTitle);
        }
    }

    @Override
    public void onBackPressed() {
        if (!navigationFragment.isDrawerOpen()) {
            if (UserManager.isLoggedIn()
                    && navigationFragment.getItem() == NavigationAdapter.NAV_MY_COURSES.getPosition()) {
                finish();
            } else if (!UserManager.isLoggedIn()
                    && navigationFragment.getItem() == NavigationAdapter.NAV_ALL_COURSES.getPosition()) {
                finish();
            } else if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStack();
                setAppBarExpanded(true);
            } else {
                finish();
            }
        } else {
            navigationFragment.closeDrawer();
        }
    }

    @SuppressWarnings("unused")
    public NavigationFragment getNavigationDrawer() {
        return this.navigationFragment;
    }

    public void restoreActionBar() {
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(texTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            super.onCreateOptionsMenu(menu);
            restoreActionBar();
            return true;
        }
        return true;//
    }

    @Override
    public boolean isDrawerOpen() {
        return this.navigationFragment.isDrawerOpen();
    }

    @Override
    public void updateDrawer() {
        this.navigationFragment.updateDrawer();
    }

    @Override
    public void selectDrawerSection(int pos) {
        this.navigationFragment.selectItem(pos);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        updateDrawer();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogoutEvent(LogoutEvent event) {
        updateDrawer();
    }

}
