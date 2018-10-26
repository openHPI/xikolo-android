package de.xikolo.controllers.video;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.graphics.Typeface;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastState;
import com.yatatsu.autobundle.AutoBundleField;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.base.BasePresenterActivity;
import de.xikolo.controllers.helper.VideoHelper;
import de.xikolo.managers.PermissionManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.models.VideoSubtitles;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.video.VideoPresenter;
import de.xikolo.presenters.video.VideoPresenterFactory;
import de.xikolo.presenters.video.VideoView;
import de.xikolo.utils.AndroidDimenUtil;
import de.xikolo.utils.CastUtil;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.MarkdownUtil;
import de.xikolo.utils.PlayServicesUtil;

public class VideoActivity extends BasePresenterActivity<VideoPresenter, VideoView> implements VideoView {

    public static final String TAG = VideoActivity.class.getSimpleName();

    public static final String ACTION_SWITCH_PLAYBACK_STATE = "switch_playback_state";

    @AutoBundleField String courseId;
    @AutoBundleField String sectionId;
    @AutoBundleField String itemId;
    @AutoBundleField String videoId;

    @BindView(R.id.videoMetadata) View videoMetadataView;
    @BindView(R.id.textTitle) TextView videoTitleText;
    @BindView(R.id.textDescription) TextView videoDescriptionText;
    @BindView(R.id.textSubtitles) TextView videoSubtitlesText;
    @BindView(R.id.videoContainer) View videoContainer;
    @BindView(R.id.video_media_route_button) MediaRouteButton mediaRouteButton;
    @BindView(R.id.settingsContainer) LinearLayout settingsContainer;
    @BindView(R.id.overlay) View overlay;

    private VideoHelper videoHelper;
    private Video video;

    private BroadcastReceiver broadcastReceiver;

    @TargetApi(26)
    private static void openPipSettings(Activity context) {
        try {
            Intent intent = new Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS");
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        } catch (RuntimeException e) {
            PermissionManager.startAppInfo(context);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        setupActionBar();
        enableOfflineModeToolbar(false);
        setColorScheme(R.color.transparent, R.color.black);
        actionBar.setTitle("");
        actionBar.setSubtitle("");

        videoHelper = new VideoHelper(this, videoContainer, settingsContainer);
        videoHelper.setControllerListener(new VideoHelper.ControllerListener() {
            @Override
            public void onControllerShow() {
                if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    actionBar.show();
                }
                showSystemBars();
            }

            @Override
            public void onControllerHide() {
                actionBar.hide();
                hideSystemBars();
            }

            @Override
            public void onSettingsSlide(float offset) {
                float alpha = (offset) * 0.7f;
                if (!Float.isNaN(alpha)) {
                    overlay.setAlpha(alpha);
                }
            }

            @Override
            public void onSettingsOpen() {
                overlay.setClickable(true);
                overlay.setOnClickListener(v -> videoHelper.hideSettings());
                videoHelper.show(Integer.MAX_VALUE);
            }

            @Override
            public void onSettingsClosed() {
                overlay.setOnClickListener(null);
                overlay.setClickable(false);
                videoHelper.show();
            }
        });

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener((visibility) -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                videoHelper.show();
            }
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));

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

        if (videoDescriptionText != null && video.summary != null
            && !video.summary.trim().isEmpty()
            && !video.summary.trim().contentEquals("Enter content")
            ) {
            videoDescriptionText.setTypeface(videoDescriptionText.getTypeface(), Typeface.NORMAL);
            MarkdownUtil.formatAndSet(video.summary, videoDescriptionText);
        }

        if (videoSubtitlesText != null && video.subtitles != null && video.subtitles.size() > 0) {
            StringBuilder text = new StringBuilder(getString(R.string.video_settings_subtitles) + ": ");
            for (VideoSubtitles subtitles : video.subtitles) {
                text.append(subtitles.language).append(", ");
            }
            text.delete(text.length() - 2, text.length());
            videoSubtitlesText.setText(text);
            videoSubtitlesText.setVisibility(View.VISIBLE);
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
        if (layout != null) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layout.setFitsSystemWindows(true);

                actionBar.hide();

                if (mediaRouteButton != null) {
                    mediaRouteButton.setVisibility(CastUtil.isAvailable()
                        ? View.VISIBLE : View.GONE);
                }

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getRealSize(size);

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

                ((ViewGroup.MarginLayoutParams) ((ViewGroup) videoContainer.getParent()).getLayoutParams()).topMargin = 0;

                settingsContainer.setPadding(
                    paddingLeft,
                    0,
                    paddingRight,
                    videoOffset > systemBarHeight ? videoOffset : systemBarHeight);
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

                ((ViewGroup.MarginLayoutParams) ((ViewGroup) videoContainer.getParent()).getLayoutParams()).topMargin = -actionBarHeight;

                settingsContainer.setPadding(0,0,0,0);
            }
        }
    }

    private void hideSystemBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions;
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
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
            uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
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
    public void onUserLeaveHint() {
        if (supportsPip()) {
            super.onUserLeaveHint();
            enterPip();
        }
    }

    @Override
    public void onBackPressed() {
        if (videoHelper.handleBackPress()) {
            if (supportsPip()) {
                enterPip();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onStop() {
        if (videoHelper != null) {
            videoHelper.pause();
            presenter.onPause(videoHelper.getCurrentPosition());
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (videoHelper != null) {
            videoHelper.release();
        }
        super.onDestroy();
    }

    private boolean supportsPip() {
        return Build.VERSION.SDK_INT >= 26 && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
    }

    @TargetApi(26)
    private boolean hasPipPermissions() {
        try {
            AppOpsManager manager = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
            if (manager != null && manager.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).uid,
                getPackageName()
            ) == AppOpsManager.MODE_ALLOWED) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @TargetApi(26)
    private void enterPip() {
        if (hasPipPermissions()) {
            enterPictureInPictureMode(getPipParams(videoHelper.isPlaying()));
        }
    }

    @TargetApi(26)
    private PictureInPictureParams getPipParams(boolean playing) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            new Intent(ACTION_SWITCH_PLAYBACK_STATE),
            0);

        List<RemoteAction> actionList = new ArrayList<>();
        actionList.add(
            new RemoteAction(
                Icon.createWithResource(
                    this,
                    playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play),
                playing ? "Pause" : "Play",
                playing ? "Pause" : "PPlay",
                pendingIntent
            )
        );

        Rect pipBounds = new Rect();
        pipBounds.set(
            videoHelper.getVideoContainer().getLeft(),
            videoHelper.getVideoContainer().getTop(),
            videoHelper.getVideoContainer().getRight(),
            videoHelper.getVideoContainer().getBottom()
        );

        return new PictureInPictureParams.Builder()
            .setSourceRectHint(pipBounds)
            .setActions(actionList)
            .build();
    }

    @Override
    @TargetApi(26)
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (isInPictureInPictureMode) {
            videoHelper.hide();
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent == null || ACTION_SWITCH_PLAYBACK_STATE.equals(intent.getAction())) {
                        return;
                    }


                    setPictureInPictureParams(getPipParams(videoHelper.isPlaying()));
                    if (videoHelper.isPlaying()) {
                        videoHelper.pause();
                    } else {
                        videoHelper.play();
                    }
                }
            };
            registerReceiver(broadcastReceiver, new IntentFilter(ACTION_SWITCH_PLAYBACK_STATE));
        } else {
            videoHelper.show();
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
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
