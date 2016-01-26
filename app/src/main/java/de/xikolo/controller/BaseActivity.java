package de.xikolo.controller;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.widgets.IntroductoryOverlay;
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

    private MenuItem mediaRouteMenuItem;

    private VideoCastConsumerImpl castConsumer;

    private boolean offlineModeToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();

        offlineModeToolbar = true;

        BaseCastManager.checkGooglePlayServices(this);

        videoCastManager = VideoCastManager.getInstance();

        castConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onCastAvailabilityChanged(boolean castPresent) {
                if (castPresent) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mediaRouteMenuItem.isVisible()) {
                                showOverlay();
                            }
                        }
                    }, 1000);
                }
            }
        };
        videoCastManager.addVideoCastConsumer(castConsumer);
    }

    private void showOverlay() {
        IntroductoryOverlay overlay = new IntroductoryOverlay.Builder(this)
                .setMenuItem(mediaRouteMenuItem)
                .setTitleText(R.string.intro_overlay_text)
                .setSingleTime()
                .build();
        overlay.show();
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
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        contentLayout = (FrameLayout) findViewById(R.id.contentLayout);
        setColorScheme(R.color.apptheme_main, R.color.apptheme_main_dark);
    }

    protected void setActionBarElevation(float elevation) {
        if (actionBar != null && Build.VERSION.SDK_INT >= 21) {
            actionBar.setElevation(elevation);
        }
    }

    protected void enableOfflineModeToolbar(boolean enable) {
        this.offlineModeToolbar = enable;
    }

    public void onEventMainThread(NetworkStateEvent event) {
        if (toolbar != null && offlineModeToolbar) {
            if (event.isOnline()) {
                toolbar.setSubtitle("");
                setColorScheme(R.color.apptheme_main, R.color.apptheme_main_dark);
            } else {
                toolbar.setSubtitle(getString(R.string.offline_mode));
                setColorScheme(R.color.offline_mode, R.color.offline_mode_dark);
            }
        }
    }

    protected void setColorScheme(int color, int darkColor) {
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, color));
            if (Build.VERSION.SDK_INT >= 21) {
                if (drawerLayout != null) {
                    drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, color));
                } else {
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, darkColor));
                }
                if (contentLayout != null) {
                    contentLayout.setBackgroundColor(ContextCompat.getColor(this, color));
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

    protected void enableCastMediaRouterButton(boolean enable) {
        if (mediaRouteMenuItem != null) {
            mediaRouteMenuItem.setVisible(enable);
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cast, menu);
        mediaRouteMenuItem = videoCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
