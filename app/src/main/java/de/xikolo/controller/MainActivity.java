package de.xikolo.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;

import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.controller.main.ContentFragment;
import de.xikolo.controller.main.ContentWebViewFragment;
import de.xikolo.controller.main.CourseListFragment;
import de.xikolo.controller.main.ProfileFragment;
import de.xikolo.controller.navigation.NavigationFragment;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.model.UserModel;
import de.xikolo.model.events.LoginEvent;
import de.xikolo.model.events.LogoutEvent;
import de.xikolo.util.Config;
import de.xikolo.util.DeepLinkingUtil;
import de.xikolo.util.LanalyticsUtil;

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
            if (UserModel.isLoggedIn(this)) {
                newFragment = ProfileFragment.newInstance();
                tag = "profile";

                LanalyticsUtil.trackVisitedProfile(UserModel.getSavedUser(this).id);
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

            LanalyticsUtil.trackVisitedDownloads(UserModel.getSavedUser(this).id);
        }
        if (position == NavigationAdapter.NAV_SETTINGS.getPosition()) {
            intent = new Intent(MainActivity.this, SettingsActivity.class);

            LanalyticsUtil.trackVisitedPreferences(UserModel.getSavedUser(this).id);
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
            if (UserModel.isLoggedIn(this)
                    && navigationFragment.getItem() == NavigationAdapter.NAV_MY_COURSES.getPosition()) {
                finish();
            } else if (!UserModel.isLoggedIn(this)
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
    public void onEventMainThread(LoginEvent event) {
        updateDrawer();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LogoutEvent event) {
        updateDrawer();
    }

}
