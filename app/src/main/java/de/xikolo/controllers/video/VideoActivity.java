package de.xikolo.controllers.video;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.MediaRouteButton;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastState;
import com.yatatsu.autobundle.AutoBundleField;

import java.util.List;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.base.BasePresenterActivity;
import de.xikolo.controllers.helper.VideoHelper;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.SubtitleTrack;
import de.xikolo.models.Video;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.video.VideoPresenter;
import de.xikolo.presenters.video.VideoPresenterFactory;
import de.xikolo.presenters.video.VideoView;
import de.xikolo.utils.AndroidDimenUtil;
import de.xikolo.utils.CastUtil;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.PlayServicesUtil;
import de.xikolo.utils.ToastUtil;

public class VideoActivity extends BasePresenterActivity<VideoPresenter, VideoView> implements VideoView {

    public static final String TAG = VideoActivity.class.getSimpleName();

    @AutoBundleField String courseId;
    @AutoBundleField String sectionId;
    @AutoBundleField String itemId;
    @AutoBundleField String videoId;

    @BindView(R.id.videoMetadata) View videoMetadataView;
    @BindView(R.id.textTitle) TextView videoTitleText;
    @BindView(R.id.videoContainer) View videoContainer;
    @BindView(R.id.video_media_route_button) MediaRouteButton mediaRouteButton;

    private VideoHelper videoHelper;
    private Video video;

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

        videoHelper = new VideoHelper(this, videoContainer);
        videoHelper.setControllerListener(new VideoHelper.ControllerListener() {
            @Override
            public void onControllerShow() {
                showSystemBars();
            }

            @Override
            public void onControllerHide() {
                hideSystemBars();
            }
        });

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener((visibility) -> {
            if (Build.VERSION.SDK_INT >= 17) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    videoHelper.show();
                }
            } else {
                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    videoHelper.show();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        if (mediaRouteButton != null) {
            if (PlayServicesUtil.checkPlayServices(this)) {
                CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
            }

            Configuration config = getResources().getConfiguration();
            mediaRouteButton.setVisibility(CastUtil.isAvailable()
                && config.orientation == Configuration.ORIENTATION_LANDSCAPE
                ? View.VISIBLE : View.GONE);
        }

        hideSystemBars();

        updateVideoView(getResources().getConfiguration().orientation);
    }

    @Override
    public void setupVideo(Course course, Section section, Item item, Video video) {
        this.video = video;

        if (videoTitleText != null) {
            videoTitleText.setText(item.title);
        }

        videoHelper.setupVideo(course, section, item, video);

        LanalyticsUtil.trackVideoPlay(itemId,
            courseId, sectionId,
            video.progress,
            videoHelper.getCurrentPlaybackSpeed().getSpeed(),
            getResources().getConfiguration().orientation,
            videoHelper.getCurrentQualityString(),
            videoHelper.getSourceString());
    }

    @Override
    public void onCastStateChanged(int newState) {
        super.onCastStateChanged(newState);

        if (newState != CastState.NO_DEVICES_AVAILABLE) {
            if (mediaRouteButton != null) {
                Configuration config = getResources().getConfiguration();
                mediaRouteButton.setVisibility(config.orientation == Configuration.ORIENTATION_LANDSCAPE
                    ? View.VISIBLE : View.GONE);
            }
        }

        if (newState == CastState.CONNECTED && videoHelper != null) {
            LanalyticsUtil.trackVideoPlay(itemId, courseId, sectionId, videoHelper.getCurrentPosition(), 1.0f,
                Configuration.ORIENTATION_LANDSCAPE, "hd", "cast");

            videoHelper.pause();
            CastUtil.loadMedia(this, video, true);

            finish();
        }
    }

    private void updateVideoView(int orientation) {
        View layout = findViewById(R.id.container);
        if (Build.VERSION.SDK_INT >= 17 && layout != null) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layout.setFitsSystemWindows(true);

                actionBar.hide();

                if (mediaRouteButton != null) {
                    mediaRouteButton.setVisibility(CastUtil.isAvailable()
                        ? View.VISIBLE : View.GONE);
                }

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getRealSize(size); // API 17

                View videoContainer = videoHelper.getVideoContainer();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoContainer.getLayoutParams();
                params.height = size.y;
                params.setMargins(0, 0, 0, 0);
                videoContainer.setLayoutParams(params);
                videoContainer.requestLayout();

                int statusBarHeight = AndroidDimenUtil.getStatusBarHeight();

                int videoOffset = (size.y - size.x / 16 * 9) / 2;

                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int systemBarHeight = size.y - displaymetrics.heightPixels;

                int paddingLeft;
                int paddingRight;
                if (Build.VERSION.SDK_INT >= 25) {
                    int rotation = getWindowManager().getDefaultDisplay().getRotation();
                    paddingLeft = rotation == Surface.ROTATION_270 ? size.x - displaymetrics.widthPixels : 0;
                    paddingRight = rotation == Surface.ROTATION_90 ? size.x - displaymetrics.widthPixels : 0;
                } else {
                    paddingLeft = 0;
                    paddingRight = size.x - displaymetrics.widthPixels;
                }

                videoHelper.getControllerView().setPadding(
                    paddingLeft,
                    videoOffset > statusBarHeight ? videoOffset : statusBarHeight,
                    paddingRight,
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

                View videoContainer = videoHelper.getVideoContainer();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoContainer.getLayoutParams();
                params.height = (int) Math.ceil(displaymetrics.widthPixels / 16. * 9.);
                params.setMargins(0, actionBarHeight, 0, 0);
                videoContainer.setLayoutParams(params);
                videoContainer.requestLayout();

                videoHelper.getControllerView().setPadding(0, 0, 0, 0);

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
        if (videoHelper.handleBackPress()) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (videoHelper != null) {
            videoHelper.pause();
            presenter.onPause(videoHelper.getCurrentPosition());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        videoHelper.show();

        updateVideoView(newConfig.orientation);

        LanalyticsUtil.trackVideoChangeOrientation(itemId, courseId, sectionId,
            videoHelper.getCurrentPosition(),
            videoHelper.getCurrentPlaybackSpeed().getSpeed(),
            newConfig.orientation,
            videoHelper.getCurrentQualityString(),
            videoHelper.getSourceString());
    }

    @NonNull
    @Override
    protected PresenterFactory<VideoPresenter> getPresenterFactory() {
        return new VideoPresenterFactory(courseId, sectionId, itemId, videoId);
    }
}
