package de.xikolo.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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

public class MainActivity extends BaseActivity
        implements NavigationFragment.NavigationDrawerCallbacks, ContentFragment.OnFragmentInteractionListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationFragment mNavigationFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private ContentFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();

        mNavigationFragment = (NavigationFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                toolbar);

        if (Config.DEBUG) {
            Log.i(TAG, "Build Type: " + BuildConfig.buildType);
            Log.i(TAG, "Build Flavor: " + BuildConfig.buildFlavor);
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
                            mNavigationFragment.selectItem(NavigationAdapter.NAV_ID_ALL_COURSES);
                            break;
                        case NEWS:
                            mNavigationFragment.selectItem(NavigationAdapter.NAV_ID_NEWS);
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
        mNavigationFragment.markItem(id);
        mNavigationFragment.setDrawerIndicatorEnabled(true);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        String tag = null;
        Intent intent = null;
        ContentFragment newFragment = null;
        switch (position) {
            case NavigationAdapter.NAV_ID_PROFILE:
                if (UserModel.isLoggedIn(this)) {
                    newFragment = ProfileFragment.newInstance();
                    tag = "profile";
                } else {
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                }
                break;
            case NavigationAdapter.NAV_ID_ALL_COURSES:
                newFragment = CourseListFragment.newInstance(CourseListFragment.FILTER_ALL);
                tag = "all_courses";
                break;
            case NavigationAdapter.NAV_ID_MY_COURSES:
                newFragment = CourseListFragment.newInstance(CourseListFragment.FILTER_MY);
                tag = "my_courses";
                break;
            case NavigationAdapter.NAV_ID_NEWS:
                newFragment = ContentWebViewFragment.newInstance(NavigationAdapter.NAV_ID_NEWS, Config.URI + Config.NEWS, getString(R.string.title_section_news), false, false);
                tag = "news";
                break;
            case NavigationAdapter.NAV_ID_DOWNLOADS:
                intent = new Intent(MainActivity.this, DownloadsActivity.class);
                break;
            case NavigationAdapter.NAV_ID_SETTINGS:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                break;
        }
        if (tag != null) {
            ContentFragment oldFragment = (ContentFragment) fragmentManager.findFragmentByTag(tag);
            if (oldFragment == null) {
                mFragment = newFragment;
            } else {
                mFragment = oldFragment;
            }
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, mFragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
        } else if (intent != null) {
            startActivity(intent);
        }
    }

    private void setTitle(String title) {
        mTitle = title;
        if (actionBar != null) {
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mNavigationFragment.isDrawerOpen()) {
            if (UserModel.isLoggedIn(this)
                    && mNavigationFragment.getItem() == NavigationAdapter.NAV_ID_MY_COURSES) {
                finish();
            } else if (!UserModel.isLoggedIn(this)
                    && mNavigationFragment.getItem() == NavigationAdapter.NAV_ID_ALL_COURSES) {
                finish();
            } else if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        } else {
            mNavigationFragment.closeDrawer();
        }
    }

    public NavigationFragment getNavigationDrawer() {
        return this.mNavigationFragment;
    }

    public void restoreActionBar() {
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean isDrawerOpen() {
        return this.mNavigationFragment.isDrawerOpen();
    }

    @Override
    public void updateDrawer() {
        this.mNavigationFragment.updateDrawer();
    }

    @Override
    public void selectDrawerSection(int pos) {
        this.mNavigationFragment.selectItem(pos);
    }

    public void onEventMainThread(LoginEvent event) {
        updateDrawer();
    }

    public void onEventMainThread(LogoutEvent event) {
        updateDrawer();
    }


}
