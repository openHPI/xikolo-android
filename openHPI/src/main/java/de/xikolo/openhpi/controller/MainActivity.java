package de.xikolo.openhpi.controller;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.controller.fragments.ContentFragment;
import de.xikolo.openhpi.controller.fragments.CourseFragment;
import de.xikolo.openhpi.controller.fragments.CoursesFragment;
import de.xikolo.openhpi.controller.fragments.DownloadsFragment;
import de.xikolo.openhpi.controller.fragments.SettingsFragment;
import de.xikolo.openhpi.controller.fragments.WebViewFragment;
import de.xikolo.openhpi.controller.navigation.NavigationFragment;
import de.xikolo.openhpi.util.Config;
import de.xikolo.openhpi.util.FontsOverride;


public class MainActivity extends FragmentActivity
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

        // set global typefaces
        // use xml attr. android:typeface="serif" for bold font style
        FontsOverride.setDefaultFont(this, "SANS_SERIF", Config.FONT_SANS);
        FontsOverride.setDefaultFont(this, "SERIF", Config.FONT_SANS_BOLD);

            mTitle = getString(R.string.app_name);

        setContentView(R.layout.activity_main);

        mNavigationFragment = (NavigationFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mActionBar = getActionBar();
    }

    @Override
    public void attachLowerFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        mNavigationFragment.setDrawerIndicatorEnabled(false);
    }

    @Override
    public void onTopFragmentAttached(int id) {
        setTitle(id);
        mNavigationFragment.markItem(id);
        mNavigationFragment.setDrawerIndicatorEnabled(true);
    }

    @Override
    public void onLowerFragmentAttached(int id, String title) {
        if (getActionBar() != null) {
            mTitle = title;
            getActionBar().setTitle(title);
        }
        mNavigationFragment.markItem(id);
        mNavigationFragment.setDrawerIndicatorEnabled(false);
    }

    @Override
    public void onLowerFragmentDetached() {
        mNavigationFragment.setDrawerIndicatorEnabled(true);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            case 0:
                mFragment = CoursesFragment.newInstance();
                break;
            case 1:
                mFragment = WebViewFragment.newInstance(Config.URI_HPI + Config.PATH_NEWS, false, null);
                break;
            case 2:
                mFragment = DownloadsFragment.newInstance();
                break;
            case 3:
                mFragment = SettingsFragment.newInstance();
                break;
            case 4:
                mFragment = CourseFragment.newInstance();
                break;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, mFragment);
        transaction.addToBackStack(mTitle.toString());
        transaction.commit();
    }

    @Override
    public void setTitle(int titleId) {
        switch (titleId) {
            case 0:
                mTitle = getString(R.string.title_section_courses);
                break;
            case 1:
                mTitle = getString(R.string.title_section_news);
                break;
            case 2:
                mTitle = getString(R.string.title_section_downloads);
                break;
            case 3:
                mTitle = getString(R.string.title_section_settings);
                break;
            case 4:
                mTitle = getString(R.string.title_section_course);
                break;
        }
        if (getActionBar() != null) {
            getActionBar().setTitle(mTitle);

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
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
    protected void onStop() {
        super.onStop();
        GlobalApplication app = (GlobalApplication) getApplicationContext();
        app.flushCache();
    }

}
