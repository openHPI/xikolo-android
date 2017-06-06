package de.xikolo.controllers.main;

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
import de.xikolo.R;
import de.xikolo.controllers.login.LoginActivity;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.controllers.downloads.DownloadsActivity;
import de.xikolo.controllers.shared.CourseListFragmentAutoBundle;
import de.xikolo.controllers.second_screen.SecondScreenActivity;
import de.xikolo.controllers.settings.SettingsActivity;
import de.xikolo.events.LoginEvent;
import de.xikolo.events.LogoutEvent;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.utils.Config;
import de.xikolo.utils.DeepLinkingUtil;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.PlayServicesUtil;

public class MainActivity extends BaseActivity
        implements NavigationFragment.NavigationDrawerCallbacks, MainFragment.MainActivityCallback {

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
        MainFragment newFragment = null;
        if (position == NavigationAdapter.NAV_PROFILE.getPosition()) {
            if (UserManager.isAuthorized()) {
                newFragment = new ProfileFragment();
                tag = "profile";

                LanalyticsUtil.trackVisitedProfile(UserManager.getUserId());
            } else {
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }
        }
        if (position == NavigationAdapter.NAV_ALL_COURSES.getPosition()) {
            newFragment = CourseListFragmentAutoBundle.builder(Course.Filter.ALL).build();
            tag = "all_courses";
        }
        if (position == NavigationAdapter.NAV_MY_COURSES.getPosition()) {
            newFragment = CourseListFragmentAutoBundle.builder(Course.Filter.ALL).build();
            tag = "my_courses";
        }
        if (position == NavigationAdapter.NAV_NEWS.getPosition()) {
//            newFragment = MainWebViewFragmentAutoBundle.builder(NavigationAdapter.NAV_NEWS.getPosition(), Config.URI + Config.NEWS, getString(R.string.title_section_news))
//                    .externalLinksEnabled(false)
//                    .inAppLinksEnabled(false)
//                    .build();
            tag = "news";
        }
        if (position == NavigationAdapter.NAV_SECOND_SCREEN.getPosition()) {
            intent = new Intent(MainActivity.this, SecondScreenActivity.class);
        }
        if (position == NavigationAdapter.NAV_DOWNLOADS.getPosition()) {
            intent = new Intent(MainActivity.this, DownloadsActivity.class);

            LanalyticsUtil.trackVisitedDownloads(UserManager.getUserId());
        }
        if (position == NavigationAdapter.NAV_SETTINGS.getPosition()) {
            intent = new Intent(MainActivity.this, SettingsActivity.class);

            LanalyticsUtil.trackVisitedPreferences(UserManager.getUserId());
        }
        if (tag != null) {
            MainFragment oldFragment = (MainFragment) fragmentManager.findFragmentByTag(tag);
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
            if (UserManager.isAuthorized()
                    && navigationFragment.getItem() == NavigationAdapter.NAV_MY_COURSES.getPosition()) {
                finish();
            } else if (!UserManager.isAuthorized()
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
        return true;
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
