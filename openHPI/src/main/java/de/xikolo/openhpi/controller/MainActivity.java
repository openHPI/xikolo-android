package de.xikolo.openhpi.controller;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.controller.fragments.CourseFragment;
import de.xikolo.openhpi.controller.fragments.CoursesFragment;
import de.xikolo.openhpi.controller.fragments.DownloadsFragment;
import de.xikolo.openhpi.controller.fragments.SettingsFragment;
import de.xikolo.openhpi.controller.fragments.WebViewFragment;
import de.xikolo.openhpi.controller.navigation.NavigationFragment;


public class MainActivity extends FragmentActivity
        implements NavigationFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationFragment mNavigationFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Fragment mFragment;

    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationFragment = (NavigationFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getString(R.string.title_section_courses);

        // Set up the drawer.
        mNavigationFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mActionBar = getActionBar();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the webview content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            case 0:
                mFragment = CoursesFragment.newInstance();
                mTitle = getString(R.string.title_section_courses);
                break;
            case 1:
                mFragment = WebViewFragment.newInstance(getString(R.string.url_news));
                mTitle = getString(R.string.title_section_news);
                break;
            case 2:
                mFragment = DownloadsFragment.newInstance();
                mTitle = getString(R.string.title_section_downloads);
                break;
            case 3:
                mFragment = SettingsFragment.newInstance();
                mTitle = getString(R.string.title_section_settings);
                break;
            case 4:
                mFragment = CourseFragment.newInstance();
                mTitle = getString(R.string.title_section_course);
                break;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, mFragment)
                .commit();
        getActionBar().setTitle(mTitle);
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
//            getMenuInflater().inflate(R.menu.webview, menu);
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

}
