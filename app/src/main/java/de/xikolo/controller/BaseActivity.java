package de.xikolo.controller;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.path.android.jobqueue.JobManager;

import de.greenrobot.event.EventBus;
import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.data.database.DatabaseHelper;
import de.xikolo.model.events.NetworkStateEvent;
import de.xikolo.model.events.PermissionDeniedEvent;
import de.xikolo.model.events.PermissionGrantedEvent;

public abstract class BaseActivity extends AppCompatActivity {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    protected DatabaseHelper databaseHelper;

    protected ActionBar actionBar;

    protected Toolbar toolbar;

    protected VideoCastManager videoCastManager;

    private DrawerLayout drawerLayout;

    private FrameLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();
        videoCastManager = VideoCastManager.getInstance();
    }

    protected void setupActionBar() {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        if (tb != null) {
            setSupportActionBar(tb);
        }

        toolbar = tb;

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
//            actionBar.setLogo(R.drawable.ic_logo);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (Build.VERSION.SDK_INT >= 21) {
            if (drawerLayout != null) {
                drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.apptheme_main));
            } else {
                getWindow().setStatusBarColor(getResources().getColor(R.color.apptheme_main_dark_status));
            }
        }

        contentLayout = (FrameLayout) findViewById(R.id.contentLayout);
        if (contentLayout != null) {
            contentLayout.setBackgroundColor(getResources().getColor(R.color.apptheme_main));
        }
    }

    protected void setActionBarElevation(float elevation) {
        if (actionBar != null && Build.VERSION.SDK_INT >= 21) {
            actionBar.setElevation(elevation);
        }
    }

    public void onEventMainThread(NetworkStateEvent event) {
        if (toolbar != null) {
            if (event.isOnline()) {
                toolbar.setBackgroundColor(getResources().getColor(R.color.apptheme_main));
                toolbar.setSubtitle("");
                if (Build.VERSION.SDK_INT >= 21) {
                    if (drawerLayout != null) {
                        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.apptheme_main));
                    } else {
                        getWindow().setStatusBarColor(getResources().getColor(R.color.apptheme_main_dark_status));
                    }
                    if (contentLayout != null) {
                        contentLayout.setBackgroundColor(getResources().getColor(R.color.apptheme_main));
                    }
                }
            } else {
                toolbar.setBackgroundColor(getResources().getColor(R.color.offline_mode));
                toolbar.setSubtitle(getString(R.string.offline_mode));
                if (Build.VERSION.SDK_INT >= 21) {
                    if (drawerLayout != null) {
                        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.offline_mode));
                    } else {
                        getWindow().setStatusBarColor(getResources().getColor(R.color.offline_mode_dark));
                    }
                    if (contentLayout != null) {
                        contentLayout.setBackgroundColor(getResources().getColor(R.color.offline_mode));
                    }
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        globalApplication.startCookieSyncManager();
        videoCastManager.incrementUiCounter();
    }

    @Override
    protected void onPause() {
        super.onPause();

        globalApplication.syncCookieSyncManager();
        globalApplication.stopCookieSyncManager();
        videoCastManager.decrementUiCounter();
    }

    @Override
    protected void onStop() {
        super.onStop();

        globalApplication.flushHttpResponseCache();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            EventBus.getDefault().post(new PermissionGrantedEvent(requestCode));

        } else {

            EventBus.getDefault().post(new PermissionDeniedEvent(requestCode));

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_activity, menu);
        videoCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
