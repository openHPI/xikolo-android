package de.xikolo.controller;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.MediaRouteButton;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;

import de.xikolo.R;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.controller.helper.VideoController;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.model.ItemModel;
import de.xikolo.model.Result;
import de.xikolo.util.CastUtil;
import de.xikolo.util.LanalyticsUtil;

public class VideoActivity extends BaseActivity {

    public static final String TAG = VideoActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";
    public static final String ARG_MODULE = "arg_module";
    public static final String ARG_ITEM = "arg_item";

    private VideoController videoController;

    private Course course;
    private Module module;
    private Item<VideoItemDetail> item;

    private ItemModel itemModel;

    private View videoMetadataView;

    private MediaRouteButton mediaRouteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        setupActionBar();
        enableOfflineModeToolbar(false);
        setColorScheme(R.color.black, R.color.black);
        actionBar.setTitle("");
        actionBar.setSubtitle("");

        View videoContainer = findViewById(R.id.videoContainer);

        itemModel = new ItemModel(jobManager);

        videoMetadataView = findViewById(R.id.videoMetadata);
        TextView videoTitleText = (TextView) findViewById(R.id.textTitle);

        videoController = new VideoController(this, videoContainer);
        videoController.setControllerListener(new VideoController.ControllerListener() {
            @Override
            public void onControllerShow() {
                showSystemBars();
            }

            @Override
            public void onControllerHide() {
                hideSystemBars();
            }
        });

        Bundle b = getIntent().getExtras();
        if (b == null || !b.containsKey(ARG_COURSE) || !b.containsKey(ARG_MODULE) || !b.containsKey(ARG_ITEM)) {
            throw new WrongParameterException();
        } else {
            course = getIntent().getExtras().getParcelable(ARG_COURSE);
            module = getIntent().getExtras().getParcelable(ARG_MODULE);
            item = getIntent().getExtras().getParcelable(ARG_ITEM);

            itemModel.getLocalVideoProgress(new Result<VideoItemDetail>() {
                @Override
                protected void onSuccess(VideoItemDetail result, DataSource dataSource) {
                    item.detail = result;
                }
            }, item.detail);

            if (videoTitleText != null) {
                videoTitleText.setText(item.detail.title);
            }
        }

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (Build.VERSION.SDK_INT >= 17) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        videoController.show();
                    }
                } else {
                    if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                        videoController.show();
                    }
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        mediaRouteButton = (MediaRouteButton) findViewById(R.id.video_media_route_button);
        if (mediaRouteButton != null) {
            VideoCastManager.getInstance().addMediaRouterButton(mediaRouteButton);
            Configuration config = getResources().getConfiguration();
            mediaRouteButton.setVisibility(VideoCastManager.getInstance().isAnyRouteAvailable()
                    && config.orientation == Configuration.ORIENTATION_LANDSCAPE
                    ? View.VISIBLE : View.GONE);
        }

        VideoCastConsumerImpl castConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
                if (videoController != null) {
                    LanalyticsUtil.trackVideoPlay(item.id, course.id, module.id, item.detail.progress, 1.0f,
                            Configuration.ORIENTATION_LANDSCAPE, "hd", LanalyticsUtil.CONTEXT_CAST);

                    videoController.pause();
                    VideoCastManager.getInstance()
                            .startVideoCastControllerActivity(VideoActivity.this, CastUtil.buildCastMetadata(item), item.detail.progress, true);
                    finish();
                }
            }

            @Override
            public void onCastAvailabilityChanged(boolean castPresent) {
                if (mediaRouteButton != null) {
                    Configuration config = getResources().getConfiguration();
                    mediaRouteButton.setVisibility(castPresent
                            && config.orientation == Configuration.ORIENTATION_LANDSCAPE
                            ? View.VISIBLE : View.GONE);
                }
            }
        };
        videoCastManager.addVideoCastConsumer(castConsumer);

        hideSystemBars();

        updateVideoView(getResources().getConfiguration().orientation);

        videoController.setupVideo(course, module, item);

        LanalyticsUtil.trackVideoPlay(item.id,
                course.id, module.id,
                item.detail.progress,
                videoController.getCurrentPlaybackSpeed().getSpeed(),
                getResources().getConfiguration().orientation,
                videoController.getQualityString(),
                videoController.getSourceString());
    }

    private void updateVideoView(int orientation) {
        View layout = findViewById(R.id.container);
        if (Build.VERSION.SDK_INT >= 17 && layout != null) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layout.setFitsSystemWindows(true);

                actionBar.hide();

                if (mediaRouteButton != null) {
                    mediaRouteButton.setVisibility(VideoCastManager.getInstance().isAnyRouteAvailable()
                            ? View.VISIBLE : View.GONE);
                }

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getRealSize(size); // API 17

                View videoContainer = videoController.getVideoContainer();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoContainer.getLayoutParams();
                params.height = size.y;
                params.setMargins(0, 0, 0, 0);
                videoContainer.setLayoutParams(params);
                videoContainer.requestLayout();

                int statusBarHeight = 0;
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                }

                int videoOffset = (size.y - size.x / 16 * 9) / 2;

                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int systemBarHeight = size.y - displaymetrics.heightPixels;

                videoController.getControllerView().setPadding(0,
                        videoOffset > statusBarHeight ? videoOffset : statusBarHeight,
                        size.x - displaymetrics.widthPixels,
                        videoOffset > systemBarHeight ? videoOffset : systemBarHeight);

                videoMetadataView.setVisibility(View.GONE);
            } else { // Portrait
                layout.setFitsSystemWindows(false);

                actionBar.show();

                if (mediaRouteButton != null) {
                    mediaRouteButton.setVisibility(View.GONE);
                }

                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

                int actionBarHeight = 0;
                TypedValue tv = new TypedValue();
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, displaymetrics);
                }

                View videoContainer = videoController.getVideoContainer();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoContainer.getLayoutParams();
                params.height = (int) Math.ceil(displaymetrics.widthPixels / 16. * 9.);
                params.setMargins(0, actionBarHeight, 0, 0);
                videoContainer.setLayoutParams(params);
                videoContainer.requestLayout();

                videoController.getControllerView().setPadding(0, 0, 0, 0);

                videoMetadataView.setVisibility(View.VISIBLE);
            }
        } else if (layout != null) {
            layout.setFitsSystemWindows(false);
        }
    }

    private void hideSystemBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions;
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (Build.VERSION.SDK_INT >= 17) {
                uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE// API 16
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // API 16
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // API 16
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // API 14
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE // API 14
                        | View.SYSTEM_UI_FLAG_FULLSCREEN; // API 16
            } else {
                uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // API 14
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE; // API 14
            }
        } else {
            uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        }
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void showSystemBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions;
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (Build.VERSION.SDK_INT >= 17) {
                uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE // API 16
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // API 16
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN; // API 16
            } else {
                uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            }
        } else {
            uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        }
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar module clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (videoController != null) {
            videoController.pause();
            VideoItemDetail itemDetail = videoController.getVideoItemDetail();
            if (itemDetail != null) {
                itemModel.updateLocalVideoProgress(new Result<Void>() {
                }, itemDetail);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        videoController.show();

        updateVideoView(newConfig.orientation);

        LanalyticsUtil.trackVideoChangeOrientation(item.id, course.id, module.id,
                videoController.getCurrentPosition(),
                videoController.getCurrentPlaybackSpeed().getSpeed(),
                newConfig.orientation,
                videoController.getQualityString(),
                videoController.getSourceString());
    }

}