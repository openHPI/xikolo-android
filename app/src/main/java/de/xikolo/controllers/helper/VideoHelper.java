package de.xikolo.controllers.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntRange;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.devbrackets.android.exomedia.listener.OnBufferUpdateListener;
import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.managers.DownloadManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.storages.ApplicationPreferences;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.PlaybackSpeedUtil;
import de.xikolo.views.CustomFontTextView;
import de.xikolo.views.CustomSizeVideoView;

public class VideoHelper {

    public static final String TAG = VideoHelper.class.getSimpleName();

    private static final int MILLISECONDS = 100;

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int FADE_OUT = 1;

    private static final int PLAYBACK_PARAMS_SDK_LEVEL = 23;

    private DownloadManager downloadManager;

    private Activity activity;

    private View videoContainer;

    @BindView(R.id.videoView) CustomSizeVideoView videoView;
    @BindView(R.id.videoController) View videoController;
    @BindView(R.id.videoProgress) View videoProgress;
    @BindView(R.id.videoSeekBar) SeekBar seekBar;

    @BindView(R.id.btnPlay) CustomFontTextView buttonPlay;
    @BindView(R.id.btnRetry) TextView buttonRetry;

    @BindView(R.id.currentTime) TextView textCurrentTime;
    @BindView(R.id.totalTime) TextView textTotalTime;
    @BindView(R.id.hdSwitch) CustomFontTextView textHdSwitch;
    @BindView(R.id.playbackSpeed) TextView textPlaybackSpeed;
    @BindView(R.id.offlineHint) View viewOfflineHint;

    @BindView(R.id.videoWarning) View viewVideoWarning;
    @BindView(R.id.videoWarningText) TextView textVideoWarning;

    private ControllerListener controllerListener;

    private Runnable seekBarUpdater;

    private Handler handler = new MessageHandler(this);

    private boolean seekBarUpdaterIsRunning = false;

    private boolean userIsSeeking = false;

    private boolean isPlaying = true;

    private PlaybackSpeedUtil currentPlaybackSpeed = PlaybackSpeedUtil.x10;

    private Course course;
    private Section module;
    private Item item;
    private Video video;

    private enum VideoMode {
        SD, HD
    }

    private VideoMode videoMode;

    public VideoHelper(FragmentActivity activity, View videoContainer) {
        this.activity = activity;
        this.videoContainer = videoContainer;

        ButterKnife.bind(this, activity);

        this.videoContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                show();
                return false;
            }
        });

        downloadManager = new DownloadManager(activity);

        setupView();
    }

    private void setupView() {
        videoView.setKeepScreenOn(true);
        videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                videoProgress.setVisibility(View.GONE);
                viewVideoWarning.setVisibility(View.GONE);
                seekBar.setMax(getDuration());
                show();

                textTotalTime.setText(getTimeString(getDuration()));
                textCurrentTime.setText(getTimeString(0));

                seekTo(video.progress);

                if (Build.VERSION.SDK_INT >= PLAYBACK_PARAMS_SDK_LEVEL) {
                    setPlaybackSpeed(currentPlaybackSpeed);
                }

                isPlaying = true;
                play();

                new Thread(seekBarUpdater).start();
            }
        });
        videoView.setOnBufferUpdateListener(new OnBufferUpdateListener() {
            @Override
            public void onBufferingUpdate(@IntRange(from = 0L, to = 100L) int percent) {
                seekBar.setSecondaryProgress((int) (seekBar.getMax() * (percent / 100.)));
            }
        });
        videoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion() {
                pause();
                buttonPlay.setText(activity.getString(R.string.icon_reload));
                show();
            }
        });
        videoView.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(Exception e) {
                saveCurrentPosition();
                viewVideoWarning.setVisibility(View.VISIBLE);
                textVideoWarning.setText(activity.getString(R.string.error_plain));

                return true;
            }
        });

        seekBarUpdater = new Runnable() {
            @Override
            public void run() {
                seekBarUpdaterIsRunning = true;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!userIsSeeking) {
                            seekBar.setProgress(getCurrentPosition());
                            textCurrentTime.setText(getTimeString(getCurrentPosition()));
                        }
                    }
                });
                if (getCurrentPosition() < getDuration()) {
                    seekBar.postDelayed(this, MILLISECONDS);
                } else {
                    seekBarUpdaterIsRunning = false;
                }
            }
        };

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
                if (videoView.isPlaying()) {
                    pause();

                    LanalyticsUtil.trackVideoPause(item.id,
                            course.id, module.id,
                            getCurrentPosition(),
                            currentPlaybackSpeed.getSpeed(),
                            activity.getResources().getConfiguration().orientation,
                            getQualityString(),
                            getSourceString());
                } else {
                    play();

                    LanalyticsUtil.trackVideoPlay(item.id,
                            course.id, module.id,
                            getCurrentPosition(),
                            currentPlaybackSpeed.getSpeed(),
                            activity.getResources().getConfiguration().orientation,
                            getQualityString(),
                            getSourceString());
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    show();
                    this.progress = progress;
                    textCurrentTime.setText(getTimeString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                LanalyticsUtil.trackVideoSeek(item.id,
                        course.id, module.id,
                        getCurrentPosition(),
                        progress,
                        currentPlaybackSpeed.getSpeed(),
                        activity.getResources().getConfiguration().orientation,
                        getQualityString(),
                        getSourceString());

                userIsSeeking = false;
                seekTo(progress);
            }
        });

        textHdSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldQuality = getQualityString();
                String oldSource = getSourceString();
                int position = getCurrentPosition();

                toggleHdButton();

                LanalyticsUtil.trackVideoChangeQuality(item.id,
                        course.id, module.id,
                        position,
                        currentPlaybackSpeed.getSpeed(),
                        activity.getResources().getConfiguration().orientation,
                        oldQuality,
                        getQualityString(),
                        oldSource,
                        getSourceString());
            }
        });

        if (Build.VERSION.SDK_INT >= PLAYBACK_PARAMS_SDK_LEVEL) {
            textPlaybackSpeed.setVisibility(View.VISIBLE);
            textPlaybackSpeed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlaybackSpeedUtil oldSpeed = currentPlaybackSpeed;

                    togglePlaybackSpeed();

                    LanalyticsUtil.trackVideoChangeSpeed(item.id,
                            course.id, module.id,
                            getCurrentPosition(),
                            oldSpeed.getSpeed(),
                            currentPlaybackSpeed.getSpeed(),
                            activity.getResources().getConfiguration().orientation,
                            getQualityString(),
                            getSourceString());
                }
            });
        } else {
            textPlaybackSpeed.setVisibility(View.GONE);
            textPlaybackSpeed.setClickable(false);
        }

        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateVideo(course, module, item, video);
            }
        });
    }

    public void play() {
        buttonPlay.setText(activity.getString(R.string.icon_pause));
        videoView.start();
        isPlaying = true;
        if (!seekBarUpdaterIsRunning) {
            new Thread(seekBarUpdater).start();
        }
    }

    public void pause() {
        buttonPlay.setText(activity.getString(R.string.icon_play));
        videoView.pause();
        isPlaying = false;
        saveCurrentPosition();
    }

    public void seekTo(int progress) {
        videoView.seekTo(progress);
        textCurrentTime.setText(getTimeString(progress));
        seekBar.setProgress(progress);
        if (!seekBarUpdaterIsRunning) {
            new Thread(seekBarUpdater).start();
        }
    }

    @TargetApi(23)
    public void togglePlaybackSpeed() {
        switch (currentPlaybackSpeed) {
            case x07:
                setPlaybackSpeed(PlaybackSpeedUtil.x10);
                break;
            case x10:
                setPlaybackSpeed(PlaybackSpeedUtil.x13);
                break;
            case x13:
                setPlaybackSpeed(PlaybackSpeedUtil.x15);
                break;
            case x15:
                setPlaybackSpeed(PlaybackSpeedUtil.x18);
                break;
            case x18:
                setPlaybackSpeed(PlaybackSpeedUtil.x20);
                break;
            case x20:
                setPlaybackSpeed(PlaybackSpeedUtil.x07);
                break;
        }
    }

    @TargetApi(23)
    public void setPlaybackSpeed(PlaybackSpeedUtil speed) {
        try {
            videoView.setPlaybackSpeed(speed.getSpeed());

            currentPlaybackSpeed = speed;
            textPlaybackSpeed.setText(speed.toString());

            if (!isPlaying) {
                pause();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    public void show(int timeout) {
        videoController.setVisibility(View.VISIBLE);
        if (controllerListener != null) {
            controllerListener.onControllerShow();
        }
        Message msg = handler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            handler.removeMessages(FADE_OUT);
            handler.sendMessageDelayed(msg, timeout);
        }
    }

    public void hide() {
        if (videoView.isPlaying()) {
            videoController.setVisibility(View.GONE);
            if (controllerListener != null) {
                controllerListener.onControllerHide();
            }
        } else {
            show();
        }
    }

    private void toggleHdButton() {
        if (videoMode == VideoMode.HD) {
            videoMode = VideoMode.SD;
        } else {
            videoMode = VideoMode.HD;
        }

        saveCurrentPosition();
        updateVideo(course, module, item, video);
    }

    public PlaybackSpeedUtil getCurrentPlaybackSpeed() {
        return currentPlaybackSpeed;
    }

    public int getCurrentPosition() {
        return (int) videoView.getCurrentPosition();
    }

    public int getDuration() {
        return (int) videoView.getDuration();
    }

    public void setupVideo(Course course, Section module, Item item, Video video) {
        this.course = course;
        this.module = module;
        this.item = item;
        this.video = video;

        int connectivityStatus = NetworkUtil.getConnectivityStatus();
        ApplicationPreferences appPreferences = new ApplicationPreferences();

        if (videoDownloadPresent(DownloadManager.DownloadFileType.VIDEO_HD, course, module, item)) { // hd video download available
            videoMode = VideoMode.HD;
        } else if (videoDownloadPresent(DownloadManager.DownloadFileType.VIDEO_SD, course, module, item)) { // sd video download available
            videoMode = VideoMode.SD;
        } else if (connectivityStatus == NetworkUtil.TYPE_WIFI || !appPreferences.isVideoQualityLimitedOnMobile()) {
            videoMode = VideoMode.HD;
        } else {
            videoMode = VideoMode.SD;
        }

        if (Build.VERSION.SDK_INT >= PLAYBACK_PARAMS_SDK_LEVEL) {
            currentPlaybackSpeed = appPreferences.getVideoPlaybackSpeed();
        }

        updateVideo(course, module, item, video);
    }

    private void updateVideo(Course course, Section module, Item item, Video video) {
        String stream;
        DownloadManager.DownloadFileType fileType;

        viewVideoWarning.setVisibility(View.GONE);

        if (videoMode == VideoMode.HD) {
            stream = video.singleStream.hdUrl;
            fileType = DownloadManager.DownloadFileType.VIDEO_HD;
        } else {
            stream = video.singleStream.sdUrl;
            fileType = DownloadManager.DownloadFileType.VIDEO_SD;
        }

        viewOfflineHint.setVisibility(View.GONE);

        if (videoDownloadPresent(fileType, course, module, item)) {
            setLocalVideoURI(fileType, course, module, item);
        } else if (NetworkUtil.isOnline()) {
            setVideoURI(stream);
        } else if (videoMode == VideoMode.HD) {
            videoMode = VideoMode.SD;
            updateVideo(course, module, item, video);
        } else {
            viewVideoWarning.setVisibility(View.VISIBLE);
            textVideoWarning.setText(activity.getString(R.string.video_notification_no_offline_video));
        }

        updateHdSwitchColor();
    }

    private boolean videoDownloadPresent(DownloadManager.DownloadFileType fileType, Course course, Section module, Item item) {
        return !downloadManager.downloadRunning(fileType, course, module, item)
                && downloadManager.downloadExists(fileType, course, module, item);
    }

    private void setLocalVideoURI(DownloadManager.DownloadFileType fileType, Course course, Section module, Item item) {
        setVideoURI("file://" + downloadManager.getDownloadFile(fileType, course, module, item).getAbsolutePath());
        viewOfflineHint.setVisibility(View.VISIBLE);
    }

    private void setVideoURI(String uri) {
        if (Config.DEBUG) {
            Log.i(TAG, "Video HOST_URL: " + uri);
        }
        videoView.setVideoURI(Uri.parse(uri));
    }

    private void updateHdSwitchColor() {
        if (videoMode == VideoMode.HD) {
            textHdSwitch.setTextColor(ContextCompat.getColor(activity, R.color.video_hd_enabled));
        } else {
            textHdSwitch.setTextColor(ContextCompat.getColor(activity, R.color.video_hd_disabled));
        }
    }

    public void saveCurrentPosition() {
        saveCurrentPosition(getCurrentPosition());
    }

    public void saveCurrentPosition(int position) {
        if (video != null) {
            video.progress = position;
        }
    }

    public View getControllerView() {
        return videoController;
    }

    public View getVideoContainer() {
        return videoContainer;
    }

    private String getTimeString(int millis) {
        return String.format(Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public String getSourceString() {
        if (viewOfflineHint != null) {
            return viewOfflineHint.getVisibility() == View.VISIBLE ? LanalyticsUtil.CONTEXT_OFFLINE : LanalyticsUtil.CONTEXT_ONLINE;
        } else return null;
    }

    public String getQualityString() {
        return videoMode.name().toLowerCase();
    }

    public void setControllerListener(ControllerListener listener) {
        this.controllerListener = listener;
    }

    public interface ControllerListener {

        void onControllerShow();

        void onControllerHide();

    }

    private static class MessageHandler extends Handler {
        VideoHelper mController;

        MessageHandler(VideoHelper controller) {
            mController = controller;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADE_OUT:
                    mController.hide();
                    break;
            }
        }
    }

}
