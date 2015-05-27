package de.xikolo.controller.helper;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.data.preferences.AppPreferences;
import de.xikolo.model.DownloadModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;
import de.xikolo.view.CustomFontTextView;
import de.xikolo.view.CustomSizeVideoView;

public class VideoController {

    public static final String TAG = VideoController.class.getSimpleName();

    public static final String KEY_TIME = "key_time";
    public static final String KEY_ISPLAYING = "key_isplaying";
    public static final String KEY_VIDEO_QUALITY = "key_quality";
    public static final String KEY_DID_USER_CHANGE_QUALITY = "key_changedquality";

    private static final int MILLISECONDS = 100;

    /**
     * Beim drehen des Bildschirms wird das Video für einen kurzen Moment auf 00:00 gesetzt
     * Um einen Neustart zu verhindern muss die vergangene Zeit >100ms betragen um als
     * gespeicherter Wert registriert zu werden. Die Variable könnte aber theoretisch jeden
     * Wert >0ms annehmen, zur Sicherheit ist sie aber auf 100ms gesetzt.
     */
    private static final int minimumTimeNeeded = 100;

    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;

    private DownloadModel mDownloadModel;

    private Activity mActivity;

    private View mVideoContainer;

    private View mVideoProgress;

    private CustomSizeVideoView mVideoView;
    private View mVideoController;

    private View mVideoHeader;
    private View mFullscreenButton;

    private View mVideoFooter;
    private SeekBar mSeekBar;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private CustomFontTextView mHDSwitch;

    private View mVideoWarning;
    private TextView mVideoWarningText;
    private TextView mRetryButton;

    private CustomFontTextView mPlayButton;

    private OnFullscreenClickListener mFullscreenListener;

    private ControllerListener mControllerListener;

    private Runnable mSeekBarUpdater;

    private Handler mHandler = new MessageHandler(this);

    private boolean wasSaved = false;
    private int savedTime = 0;

    private boolean seekBarUpdaterIsRunning = false;

    private Course mCourse;
    private Module mModule;
    private Item<VideoItemDetail> mVideoItemDetails;

    private boolean playVideoInHD = false;
    private boolean userChangedVideoQuality = false;
    // Umgeht Fehler, dass bei Veränderung der Qualität und Wechsel der Bildschirmausrichtung das Video stehen bleibt bzw. weiter läuft
    private boolean savedIsPlaying = false;
    private int currentPosition = 0;

    private boolean error = false;

    public VideoController(Activity activity, View videoContainer) {
        mActivity = activity;

        mVideoContainer = videoContainer;
        mVideoView = (CustomSizeVideoView) mVideoContainer.findViewById(R.id.videoView);
        mVideoController = mVideoContainer.findViewById(R.id.videoController);

        mVideoProgress = mVideoContainer.findViewById(R.id.videoProgress);

        mVideoHeader = mVideoContainer.findViewById(R.id.videoHeader);
        mFullscreenButton = mVideoContainer.findViewById(R.id.btnFullscreen);

        mVideoFooter = mVideoContainer.findViewById(R.id.videoFooter);
        mSeekBar = (SeekBar) mVideoContainer.findViewById(R.id.videoSeekBar);
        mCurrentTime = (TextView) mVideoController.findViewById(R.id.currentTime);
        mTotalTime = (TextView) mVideoController.findViewById(R.id.totalTime);
        mHDSwitch = (CustomFontTextView) mVideoController.findViewById(R.id.hdSwitch);

        mPlayButton = (CustomFontTextView) mVideoContainer.findViewById(R.id.btnPlay);

        mVideoWarning = mVideoContainer.findViewById(R.id.videoWarning);
        mVideoWarningText = (TextView) mVideoContainer.findViewById(R.id.videoWarningText);
        mRetryButton = (TextView) mVideoContainer.findViewById(R.id.btnRetry);

        mVideoContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                show();
                return false;
            }
        });

        mDownloadModel = new DownloadModel(activity, GlobalApplication.getInstance().getJobManager());

        setup();
    }

    private void setup() {
        mVideoView.setKeepScreenOn(true);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoProgress.setVisibility(View.GONE);
                mSeekBar.setMax(mVideoView.getDuration());
                show();

                mTotalTime.setText(getTimeString(mVideoView.getDuration()));
                mCurrentTime.setText(getTimeString(0));

                if (wasSaved) {
                    seekTo(savedTime);
                    if (savedIsPlaying) {
                        start();
                    }
                } else {
                    seekTo(0);
                }

                mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        mSeekBar.setSecondaryProgress((int) (mSeekBar.getMax() * (percent / 100.)));
                    }
                });

                new Thread(mSeekBarUpdater).start();
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                pause();
                mPlayButton.setText(mActivity.getString(R.string.icon_reload));
                show();
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        Log.w(TAG, "MediaPlayer.MEDIA_ERROR_UNKNOWN appeared");
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        Log.w(TAG, "MediaPlayer.MEDIA_ERROR_SERVER_DIED appeared");
                        break;
                }
                // TODO proper error handling
                error = true;
                mVideoProgress.setVisibility(View.GONE);
                hide();
                ToastUtil.show(mActivity, R.string.error);

                return true;
            }
        });

        mSeekBarUpdater = new Runnable() {
            @Override
            public void run() {
                seekBarUpdaterIsRunning = true;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSeekBar.setProgress(mVideoView.getCurrentPosition());
                        mCurrentTime.setText(getTimeString(mVideoView.getCurrentPosition()));
                    }
                });
                if (mVideoView.getCurrentPosition() < mVideoView.getDuration()) {
                    mSeekBar.postDelayed(this, MILLISECONDS);
                } else {
                    seekBarUpdaterIsRunning = false;
                }
            }
        };

        mFullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
                if (mFullscreenListener != null) {
                    saveCurrentPosition();
                    mFullscreenListener.onFullscreenClick(savedTime, savedIsPlaying || mVideoView.isPlaying(), playVideoInHD, userChangedVideoQuality);
                }
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
                if (mVideoView.isPlaying()) {
                    pause();
                } else {
                    start();
                }
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    show();
                    seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mHDSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleHD();
            }
        });

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateVideoQuality(mCourse, mModule, mVideoItemDetails);
            }
        });
    }

    public void seekTo(int progress) {
        mVideoView.seekTo(progress);
        mCurrentTime.setText(getTimeString(progress));
        mSeekBar.setProgress(progress);
        if (!seekBarUpdaterIsRunning) {
            new Thread(mSeekBarUpdater).start();
        }
    }

    public void start() {
        mPlayButton.setText(mActivity.getString(R.string.icon_pause));
        mVideoView.start();
        savedIsPlaying = true;
        if (!seekBarUpdaterIsRunning) {
            new Thread(mSeekBarUpdater).start();
        }
    }

    public void pause() {
        mPlayButton.setText(mActivity.getString(R.string.icon_play));
        mVideoView.pause();
        savedIsPlaying = false;
    }

    public void show() {
        show(sDefaultTimeout);
    }

    public void show(int timeout) {
        if (!error) {
            mVideoController.setVisibility(View.VISIBLE);
            if (mControllerListener != null) {
                mControllerListener.onControllerShow();
            }
            Message msg = mHandler.obtainMessage(FADE_OUT);
            if (timeout != 0) {
                mHandler.removeMessages(FADE_OUT);
                mHandler.sendMessageDelayed(msg, timeout);
            }
        }
    }

    public void hide() {
        if (mVideoView.isPlaying()) {
            mVideoController.setVisibility(View.GONE);
            if (mControllerListener != null) {
                mControllerListener.onControllerHide();
            }
        } else {
            show();
        }
    }

    public void setDimensions(int w, int h) {
        mVideoView.setDimensions(w, h);
    }

    private void setVideoURI(String uri) {
        if (Config.DEBUG) {
            Log.i(TAG, "Video URI: " + uri);
        }
        mVideoView.setVideoURI(Uri.parse(uri));
    }

    public void setVideo(Course course, Module module, Item<VideoItemDetail> video) {
        mCourse = course;
        mModule = module;
        mVideoItemDetails = video;

        if (!userChangedVideoQuality) {
            int connectivityStatus = NetworkUtil.getConnectivityStatus(mActivity);

            if (connectivityStatus == NetworkUtil.TYPE_WIFI || connectivityStatus == NetworkUtil.TYPE_NOT_CONNECTED
                    || (connectivityStatus == NetworkUtil.TYPE_MOBILE && !AppPreferences.isVideoQualityLimitedOnMobile(mActivity))) {
                playVideoInHD = true;
            } else {
                playVideoInHD = false;
            }
        }

        if (mVideoItemDetails.detail.progress > minimumTimeNeeded) {
            savedTime = mVideoItemDetails.detail.progress;
            wasSaved = true;
        }
        updateVideoQuality(course, module, mVideoItemDetails);
    }

    public VideoItemDetail getVideoItemDetail() {
        saveCurrentPosition();
        return mVideoItemDetails.detail;
    }

    public void enableHeader() {
        mVideoHeader.setVisibility(View.VISIBLE);
    }

    public void disableHeader() {
        mVideoHeader.setVisibility(View.GONE);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mVideoView != null) {
            saveCurrentPosition();

            outState.putBoolean(KEY_ISPLAYING, savedIsPlaying || mVideoView.isPlaying());
            outState.putBoolean(KEY_VIDEO_QUALITY, playVideoInHD);
            outState.putBoolean(KEY_DID_USER_CHANGE_QUALITY, userChangedVideoQuality);
        }
    }

    public void returnFromSavedInstanceState(Bundle savedInstanceState) {
        if (mVideoView != null) {
            wasSaved = true;

            savedIsPlaying = savedInstanceState.getBoolean(KEY_ISPLAYING);
            playVideoInHD = savedInstanceState.getBoolean(KEY_VIDEO_QUALITY);
            userChangedVideoQuality = savedInstanceState.getBoolean(KEY_DID_USER_CHANGE_QUALITY);

            setHDSwitchColor(playVideoInHD);
        }
    }

    public View getControllerView() {
        return mVideoController;
    }

    private String getTimeString(int millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public void setOnFullscreenButtonClickedListener(OnFullscreenClickListener listener) {
        this.mFullscreenListener = listener;
    }

    public void setControllerListener(ControllerListener listener) {
        this.mControllerListener = listener;
    }

    private void toggleHD() {
        userChangedVideoQuality = true;

        playVideoInHD = !playVideoInHD;

        saveCurrentPosition();
        updateVideoQuality(mCourse, mModule, mVideoItemDetails);
        seekTo(savedTime);
    }

    public void playHD() {
        userChangedVideoQuality = true;

        playVideoInHD = true;

        saveCurrentPosition();
        updateVideoQuality(mCourse, mModule, mVideoItemDetails);
        seekTo(savedTime);
        start();
    }

    public void playSD() {
        userChangedVideoQuality = true;

        playVideoInHD = false;

        saveCurrentPosition();
        updateVideoQuality(mCourse, mModule, mVideoItemDetails);
        seekTo(savedTime);
        start();
    }

    private void saveCurrentPosition() {
        if (mVideoView != null) {
            currentPosition = mVideoView.getCurrentPosition();
            if (currentPosition > minimumTimeNeeded) {
                savedTime = currentPosition;
                savedIsPlaying = mVideoView.isPlaying();
                wasSaved = true;
            }

            mVideoItemDetails.detail.progress = savedTime;
        }
    }

    private void updateVideoQuality(Course course, Module module, Item<VideoItemDetail> video) {
        String stream;
        DownloadModel.DownloadFileType fileType;

        mVideoWarning.setVisibility(View.GONE);

        if (playVideoInHD) {
            stream = video.detail.stream.hd_url;
            fileType = DownloadModel.DownloadFileType.VIDEO_HD;
        } else {
            stream = video.detail.stream.sd_url;
            fileType = DownloadModel.DownloadFileType.VIDEO_SD;
        }

        if (!mDownloadModel.downloadRunning(fileType, course, module, video)
                && mDownloadModel.downloadExists(fileType, course, module, video)) {
            setVideoURI("file://" + mDownloadModel.getDownloadFile(fileType, course, module, video).getAbsolutePath());
        } else if (NetworkUtil.isOnline(mActivity)) {
            setVideoURI(stream);
        } else if (playVideoInHD) {
            playVideoInHD = false;
            updateVideoQuality(course, module, video);
        } else {
            mVideoWarning.setVisibility(View.VISIBLE);
            mVideoWarningText.setText(mActivity.getString(R.string.video_notification_no_offline_video));
        }

        setHDSwitchColor(playVideoInHD);
    }

    private void setHDSwitchColor(boolean isHD) {
        if (isHD) {
            mHDSwitch.setTextColor(mActivity.getResources().getColor(R.color.video_hd_enabled));
        } else {
            mHDSwitch.setTextColor(mActivity.getResources().getColor(R.color.video_hd_disabled));
        }
    }

    public interface OnFullscreenClickListener {

        public void onFullscreenClick(int currentPosition, boolean isPlaying, boolean videoQualityInHD, boolean didUserChangeVideoQuality);

    }

    public interface ControllerListener {

        public void onControllerShow();

        public void onControllerHide();

    }

    private static class MessageHandler extends Handler {
        VideoController mController;

        public MessageHandler(VideoController controller) {
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
