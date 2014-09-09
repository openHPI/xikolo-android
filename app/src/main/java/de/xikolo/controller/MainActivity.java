package de.xikolo.controller;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.controller.main.ContentFragment;
import de.xikolo.controller.main.CourseListFragment;
import de.xikolo.controller.main.DownloadsFragment;
import de.xikolo.controller.main.ProfileFragment;
import de.xikolo.controller.main.SettingsFragment;
import de.xikolo.controller.main.WebViewFragment;
import de.xikolo.controller.navigation.NavigationFragment;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.model.CourseModel;
import de.xikolo.util.Config;

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

    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationFragment = (NavigationFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mActionBar = getActionBar();

        if (Config.DEBUG) {
            Log.i(TAG, "Build Type: " + BuildConfig.buildType);
            Log.i(TAG, "Build Flavor: " + BuildConfig.buildFlavor);
        }
    }

    @Override
    public void attachFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onTopLevelFragmentAttached(int id, String title) {
        setTitle(title);
        mNavigationFragment.markItem(id);
        mNavigationFragment.setDrawerIndicatorEnabled(true);
    }

    @Override
    public void onLowLevelFragmentAttached(int id, String title) {
        setTitle(title);
        mNavigationFragment.markItem(id);
        mNavigationFragment.setDrawerIndicatorEnabled(false);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        String tag = null;
        ContentFragment newFragment = null;
        switch (position) {
            case NavigationAdapter.NAV_ID_PROFILE:
                newFragment = ProfileFragment.newInstance();
                tag = "profile";
                break;
            case NavigationAdapter.NAV_ID_ALL_COURSES:
                newFragment = CourseListFragment.newInstance(CourseModel.FILTER_ALL);
                tag = "all_courses";
                break;
            case NavigationAdapter.NAV_ID_MY_COURSES:
                newFragment = CourseListFragment.newInstance(CourseModel.FILTER_MY);
                tag = "my_courses";
                break;
            case NavigationAdapter.NAV_ID_NEWS:
                newFragment = WebViewFragment.newInstance(Config.URI + Config.NEWS, true, null);
                tag = "news";
                break;
            case NavigationAdapter.NAV_ID_DOWNLOADS:
                newFragment = DownloadsFragment.newInstance();
                tag = "downloads";
                break;
            case NavigationAdapter.NAV_ID_SETTINGS:
                newFragment = SettingsFragment.newInstance();
                tag = "settings";
                break;
        }
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
    }

    private void setTitle(String title) {
        mTitle = title;
        if (mActionBar != null) {
            mActionBar.setTitle(mTitle);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mNavigationFragment.isDrawerOpen()) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1)
                getSupportFragmentManager().popBackStack();
            else
                finish();
        } else {
            mNavigationFragment.closeDrawer();
        }
    }

    public NavigationFragment getNavigationDrawer() {
        return this.mNavigationFragment;
    }

    public void restoreActionBar() {
        if (mActionBar != null) {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            mActionBar.setDisplayShowTitleEnabled(true);
            mActionBar.setTitle(mTitle);
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
    public void toggleDrawer(int pos) {
        this.mNavigationFragment.selectItem(pos);
    }

}
