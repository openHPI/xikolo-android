package de.xikolo.controller.helper;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
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

    private static final int MILLISECONDS = 100;

    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;

    private DownloadModel mDownloadModel;

    private Activity mActivity;

    private View mVideoContainer;

    private View mVideoProgress;

    private CustomSizeVideoView mVideoView;
    private View mVideoController;

    private View mVideoHeader;

    private View mVideoFooter;
    private SeekBar mSeekBar;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private CustomFontTextView mHdSwitch;

    private View mVideoWarning;
    private TextView mVideoWarningText;
    private TextView mRetryButton;

    private CustomFontTextView mPlayButton;

    private View mOfflineHint;

    private ControllerListener mControllerListener;

    private Runnable mSeekBarUpdater;

    private Handler mHandler = new MessageHandler(this);

    private boolean seekBarUpdaterIsRunning = false;

    private boolean mUserIsSeeking = false;

    private boolean mIsPlaying = true;

    private Course mCourse;
    private Module mModule;
    private Item<VideoItemDetail> mVideoItemDetails;

    private boolean error = false;

    private enum VideoMode {
        SD, HD
    }

    private VideoMode mVideoMode;

    public VideoController(Activity activity, View videoContainer) {
        mActivity = activity;

        mVideoContainer = videoContainer;
        mVideoView = (CustomSizeVideoView) mVideoContainer.findViewById(R.id.videoView);
        mVideoController = mVideoContainer.findViewById(R.id.videoController);

        mVideoProgress = mVideoContainer.findViewById(R.id.videoProgress);

        mVideoHeader = mVideoContainer.findViewById(R.id.videoHeader);

        mVideoFooter = mVideoContainer.findViewById(R.id.videoFooter);
        mSeekBar = (SeekBar) mVideoContainer.findViewById(R.id.videoSeekBar);
        mCurrentTime = (TextView) mVideoController.findViewById(R.id.currentTime);
        mTotalTime = (TextView) mVideoController.findViewById(R.id.totalTime);
        mHdSwitch = (CustomFontTextView) mVideoController.findViewById(R.id.hdSwitch);

        mPlayButton = (CustomFontTextView) mVideoContainer.findViewById(R.id.btnPlay);

        mOfflineHint = mVideoContainer.findViewById(R.id.offlineHint);

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

        mDownloadModel = new DownloadModel(GlobalApplication.getInstance().getJobManager(), activity);

        setupView();
    }

    private void setupView() {
        mVideoView.setKeepScreenOn(true);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoProgress.setVisibility(View.GONE);
                mSeekBar.setMax(mVideoView.getDuration());
                show();

                mTotalTime.setText(getTimeString(mVideoView.getDuration()));
                mCurrentTime.setText(getTimeString(0));

                seekTo(mVideoItemDetails.detail.progress);
                if (mIsPlaying) {
                    start();
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
                        Log.e(TAG, "MediaPlayer.MEDIA_ERROR_UNKNOWN appeared");
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        Log.e(TAG, "MediaPlayer.MEDIA_ERROR_SERVER_DIED appeared");
                        break;
                }
                switch (extra) {
                    case MediaPlayer.MEDIA_ERROR_IO:
                        Log.w(TAG, "MediaPlayer.MEDIA_ERROR_IO appeared");
                        break;
                    case MediaPlayer.MEDIA_ERROR_MALFORMED:
                        Log.w(TAG, "MediaPlayer.MEDIA_ERROR_MALFORMED appeared");
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                        Log.w(TAG, "MediaPlayer.MEDIA_ERROR_UNSUPPORTED appeared");
                        break;
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        Log.w(TAG, "MediaPlayer.MEDIA_ERROR_TIMED_OUT appeared");
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
                        if (!mUserIsSeeking) {
                            mSeekBar.setProgress(mVideoView.getCurrentPosition());
                            mCurrentTime.setText(getTimeString(mVideoView.getCurrentPosition()));
                        }
                    }
                });
                if (mVideoView.getCurrentPosition() < mVideoView.getDuration()) {
                    mSeekBar.postDelayed(this, MILLISECONDS);
                } else {
                    seekBarUpdaterIsRunning = false;
                }
            }
        };

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
            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    show();
                    this.progress = progress;
                    mCurrentTime.setText(getTimeString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = false;
                seekTo(progress);
            }
        });

        mHdSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleHdButton();
            }
        });

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateVideo(mCourse, mModule, mVideoItemDetails);
            }
        });
    }

    public void start() {
        mPlayButton.setText(mActivity.getString(R.string.icon_pause));
        mVideoView.start();
        mIsPlaying = true;
        if (!seekBarUpdaterIsRunning) {
            new Thread(mSeekBarUpdater).start();
        }
    }

    public void pause() {
        mPlayButton.setText(mActivity.getString(R.string.icon_play));
        mVideoView.pause();
        mIsPlaying = false;
        saveCurrentPosition();
    }

    public void seekTo(int progress) {
        mVideoView.seekTo(progress);
        mCurrentTime.setText(getTimeString(progress));
        mSeekBar.setProgress(progress);
        if (!seekBarUpdaterIsRunning) {
            new Thread(mSeekBarUpdater).start();
        }
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

    private void toggleHdButton() {
        if (mVideoMode == VideoMode.HD) {
            mVideoMode = VideoMode.SD;
        } else {
            mVideoMode = VideoMode.HD;
        }

        saveCurrentPosition();
        updateVideo(mCourse, mModule, mVideoItemDetails);
    }

    public void setupVideo(Course course, Module module, Item<VideoItemDetail> video) {
        mCourse = course;
        mModule = module;
        mVideoItemDetails = video;

        int connectivityStatus = NetworkUtil.getConnectivityStatus(mActivity);
        AppPreferences preferences =  GlobalApplication.getInstance().getPreferencesFactory().getAppPreferences();

        if (connectivityStatus == NetworkUtil.TYPE_MOBILE && preferences.isVideoQualityLimitedOnMobile()) {
            mVideoMode = VideoMode.SD;
        } else {
            mVideoMode = VideoMode.HD;
        }

        updateVideo(course, module, mVideoItemDetails);
    }

    private void updateVideo(Course course, Module module, Item<VideoItemDetail> video) {
        String stream;
        DownloadModel.DownloadFileType fileType;

        mVideoWarning.setVisibility(View.GONE);

        if (mVideoMode == VideoMode.HD) {
            stream = video.detail.stream.hd_url;
            fileType = DownloadModel.DownloadFileType.VIDEO_HD;
        } else {
            stream = video.detail.stream.sd_url;
            fileType = DownloadModel.DownloadFileType.VIDEO_SD;
        }

        mOfflineHint.setVisibility(View.GONE);

        if (!mDownloadModel.downloadRunning(fileType, course, module, video)
                && mDownloadModel.downloadExists(fileType, course, module, video)) {
            setVideoURI("file://" + mDownloadModel.getDownloadFile(fileType, course, module, video).getAbsolutePath());
            mOfflineHint.setVisibility(View.VISIBLE);
        } else if (NetworkUtil.isOnline(mActivity)) {
            setVideoURI(stream);
        } else if (mVideoMode == VideoMode.HD) {
            mVideoMode = VideoMode.SD;
            updateVideo(course, module, video);
        } else {
            mVideoWarning.setVisibility(View.VISIBLE);
            mVideoWarningText.setText(mActivity.getString(R.string.video_notification_no_offline_video));
        }

        updateHdSwitchColor();
    }

    private void setVideoURI(String uri) {
        if (Config.DEBUG) {
            Log.i(TAG, "Video URI: " + uri);
        }
        mVideoView.setVideoURI(Uri.parse(uri));
    }

    private void updateHdSwitchColor() {
        if (mVideoMode == VideoMode.HD) {
            mHdSwitch.setTextColor(ContextCompat.getColor(mActivity, R.color.video_hd_enabled));
        } else {
            mHdSwitch.setTextColor(ContextCompat.getColor(mActivity, R.color.video_hd_disabled));
        }
    }

    public void saveCurrentPosition() {
        if (mVideoView != null && mVideoItemDetails != null) {
            mVideoItemDetails.detail.progress = mVideoView.getCurrentPosition();
        }
    }

    public void setDimensions(int w, int h) {
        mVideoView.setDimensions(w, h);
    }

    public void enableHeader() {
        mVideoHeader.setVisibility(View.VISIBLE);
    }

    public void disableHeader() {
        mVideoHeader.setVisibility(View.GONE);
    }

    public View getControllerView() {
        return mVideoController;
    }

    public View getVideoView() {
        return mVideoView;
    }

    public View getVideoContainer() {
        return mVideoContainer;
    }

    public VideoItemDetail getVideoItemDetail() {
        if(mVideoItemDetails != null) {
            return mVideoItemDetails.detail;
        }
        return null;
    }

    private String getTimeString(int millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public void setControllerListener(ControllerListener listener) {
        this.mControllerListener = listener;
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
