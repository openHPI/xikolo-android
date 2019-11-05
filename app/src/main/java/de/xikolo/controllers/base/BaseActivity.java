package de.xikolo.controllers.base;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.material.appbar.AppBarLayout;
import com.yatatsu.autobundle.AutoBundle;

import butterknife.ButterKnife;
import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.utils.NotificationUtil;
import de.xikolo.utils.extensions.PlayServicesUtil;

public abstract class BaseActivity extends AppCompatActivity implements CastStateListener {

    protected App app;

    protected ActionBar actionBar;

    protected Toolbar toolbar;

    protected AppBarLayout appBar;

    private CastContext castContext;

    private DrawerLayout drawerLayout;

    private CoordinatorLayout contentLayout;

    private MenuItem mediaRouteMenuItem;

    private boolean offlineModeToolbar;

    private IntroductoryOverlay overlay;

    private boolean translucentActionbar = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // restore
            AutoBundle.bind(this, savedInstanceState);
        } else {
            AutoBundle.bind(this);
        }

        app = App.getInstance();

        offlineModeToolbar = true;

        try {
            if (PlayServicesUtil.getHasPlayServices(this)) {
                castContext = CastContext.getSharedInstance(this);
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        if (overlay == null) {
            showOverlay();
        }

        handleIntent(getIntent());

        App.getInstance().getState().getConnectivity().observe(this, this::onConnectivityChange);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this, findViewById(android.R.id.content));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        try {
            if (PlayServicesUtil.getHasPlayServices(this)) {
                castContext = CastContext.getSharedInstance(this);
                getMenuInflater().inflate(R.menu.cast, menu);
                mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(
                        getApplicationContext(),
                        menu,
                        R.id.media_route_menu_item);
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        AutoBundle.bind(this);

        handleIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (castContext != null) {
            castContext.addCastStateListener(this);
            setupCastMiniController();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        app.syncCookieSyncManager();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (castContext != null) {
            castContext.removeCastStateListener(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        AutoBundle.pack(this, outState);
    }

    private void showOverlay() {
        if (overlay != null) {
            overlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().postDelayed(() -> {
                if (mediaRouteMenuItem != null && mediaRouteMenuItem.isVisible()) {
                    overlay = new IntroductoryOverlay.Builder(BaseActivity.this, mediaRouteMenuItem)
                            .setTitleText(R.string.intro_overlay_text)
                            .setSingleTime()
                            .setOnOverlayDismissedListener(() -> overlay = null)
                            .build();
                    overlay.show();
                }
            }, 1000);
        }
    }

    @Override
    public void onCastStateChanged(int newState) {
        if (newState != CastState.NO_DEVICES_AVAILABLE) {
            showOverlay();
        }
    }

    protected void setupActionBar() {
        setupActionBar(false);
    }

    protected void setupActionBar(boolean translucentActionbar) {
        this.translucentActionbar = translucentActionbar;
        Toolbar tb = findViewById(R.id.toolbar);
        if (tb != null) {
            setSupportActionBar(tb);
        }

        toolbar = tb;

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        contentLayout = findViewById(R.id.contentLayout);
        appBar = findViewById(R.id.appbar);
        setColorScheme(R.color.toolbar, R.color.statusbar);
    }

    private boolean setupCastMiniController() {
        if (PlayServicesUtil.getHasPlayServices(this) && findViewById(R.id.miniControllerContainer) != null) {
            View container = findViewById(R.id.miniControllerContainer);
            ViewGroup parent = (ViewGroup) container.getParent();
            int index = parent.indexOfChild(container);

            parent.removeView(container);
            container = getLayoutInflater().inflate(R.layout.container_mini_controller, parent, false);
            parent.addView(container, index);

            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unused")
    protected void setActionBarElevation(float elevation) {
        if (actionBar != null) {
            actionBar.setElevation(elevation);
        }
    }

    protected void setAppBarExpanded(boolean expanded) {
        if (appBar != null) {
            appBar.setExpanded(expanded, false);
        }
    }

    protected void enableOfflineModeToolbar(boolean enable) {
        this.offlineModeToolbar = enable;
    }

    protected void setColorScheme(int toolbarColor, int statusbarColor) {
        if (toolbar != null && !translucentActionbar) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, toolbarColor));
            if (drawerLayout != null) {
                drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, toolbarColor));
            } else {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, statusbarColor));
            }
            if (contentLayout != null) {
                contentLayout.setBackgroundColor(ContextCompat.getColor(this, toolbarColor));
            }
        }
    }

    public void onConnectivityChange(boolean isOnline) {
        if (toolbar != null && offlineModeToolbar) {
            if (isOnline) {
                toolbar.setSubtitle("");
                setColorScheme(R.color.toolbar, R.color.statusbar);
            } else {
                toolbar.setSubtitle(getString(R.string.offline_mode));
                setColorScheme(R.color.toolbar_offline, R.color.statusbar_offline);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            App.getInstance().getState().getPermission().of(requestCode).granted();
        } else {
            App.getInstance().getState().getPermission().of(requestCode).denied();
        }
    }

    protected void enableCastMediaRouterButton(boolean enable) {
        if (mediaRouteMenuItem != null) {
            mediaRouteMenuItem.setVisible(enable);
            invalidateOptionsMenu();
        }
    }

    private void handleIntent(Intent intent) {
        NotificationUtil.deleteDownloadNotificationsFromIntent(intent);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (PlayServicesUtil.getHasPlayServices(this) && castContext != null) {
            return castContext.onDispatchVolumeKeyEventBeforeJellyBean(event)
                    || super.dispatchKeyEvent(event);
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

}
