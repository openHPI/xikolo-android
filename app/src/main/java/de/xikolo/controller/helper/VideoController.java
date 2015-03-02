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

import de.xikolo.R;
import de.xikolo.data.preferences.AppPreferences;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;
import de.xikolo.view.CustomFontTextView;
import de.xikolo.view.CustomSizeVideoView;

public class VideoController {

    public static final String TAG = VideoController.class.getSimpleName();

    public static final String KEY_TIME = "key_time";
    public static final String KEY_ISPLAYING = "key_isplaying";

    private static final int MILLISECONDS = 100;

    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;

    private Activity mActivity;

    private View mVideoContainer;

    private View mVideoProgress;

    private CustomSizeVideoView mVideo;
    private View mVideoController;

    private View mVideoHeader;
    private View mFullscreenButton;

    private View mVideoFooter;
    private SeekBar mSeekBar;
    private TextView mCurrentTime;
    private TextView mTotalTime;

    private CustomFontTextView mPlayButton;

    private OnFullscreenClickListener mFullscreenListener;

    private ControllerListener mControllerListener;

    private Runnable mSeekBarUpdater;

    private Handler mHandler = new MessageHandler(this);

    private boolean wasSaved = false;
    private int savedTime = 0;
    private boolean savedIsPlaying = false;

    private boolean seekBarUpdaterIsRunning = false;

    private boolean error = false;

    public VideoController(Activity activity, View videoContainer) {
        mActivity = activity;

        mVideoContainer = videoContainer;
        mVideo = (CustomSizeVideoView) mVideoContainer.findViewById(R.id.videoView);
        mVideoController = mVideoContainer.findViewById(R.id.videoController);

        mVideoProgress = mVideoContainer.findViewById(R.id.videoProgress);

        mVideoHeader = mVideoContainer.findViewById(R.id.videoHeader);
        mFullscreenButton = mVideoContainer.findViewById(R.id.btnFullscreen);

        mVideoFooter = mVideoContainer.findViewById(R.id.videoFooter);
        mSeekBar = (SeekBar) mVideoContainer.findViewById(R.id.videoSeekBar);
        mCurrentTime = (TextView) mVideoController.findViewById(R.id.currentTime);
        mTotalTime = (TextView) mVideoController.findViewById(R.id.totalTime);

        mPlayButton = (CustomFontTextView) mVideoContainer.findViewById(R.id.btnPlay);

        mVideoContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                show();
                return false;
            }
        });

        setup();
    }

    private void setup() {
        mVideo.setKeepScreenOn(true);
        mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoProgress.setVisibility(View.GONE);
                seekTo(0);
                mSeekBar.setMax(mVideo.getDuration());
                show();

                mTotalTime.setText(getTimeString(mVideo.getDuration()));
                mCurrentTime.setText(getTimeString(0));

                if (wasSaved) {
                    seekTo(savedTime);
                    if (savedIsPlaying) {
                        start();
                    }
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
        mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                pause();
                mPlayButton.setText(mActivity.getString(R.string.icon_reload));
                show();
            }
        });
        mVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
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
                        mSeekBar.setProgress(mVideo.getCurrentPosition());
                        mCurrentTime.setText(getTimeString(mVideo.getCurrentPosition()));
                    }
                });
                if (mVideo.getCurrentPosition() < mVideo.getDuration()) {
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
                    mFullscreenListener.onFullscreenClick(mVideo.getCurrentPosition(), mVideo.isPlaying());
                }
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
                if (mVideo.isPlaying()) {
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

    }

    public void seekTo(int progress) {
        mVideo.seekTo(progress);
        mCurrentTime.setText(getTimeString(progress));
        mSeekBar.setProgress(progress);
        if (!seekBarUpdaterIsRunning) {
            new Thread(mSeekBarUpdater).start();
        }
    }

    public void start() {
        mPlayButton.setText(mActivity.getString(R.string.icon_pause));
        mVideo.start();
        if (!seekBarUpdaterIsRunning) {
            new Thread(mSeekBarUpdater).start();
        }
    }

    public void pause() {
        mPlayButton.setText(mActivity.getString(R.string.icon_play));
        mVideo.pause();
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
        if (mVideo.isPlaying()) {
            mVideoController.setVisibility(View.GONE);
            if (mControllerListener != null) {
                mControllerListener.onControllerHide();
            }
        } else {
            show();
        }
    }

    public void setDimensions(int w, int h) {
        mVideo.setDimensions(w, h);
    }

    public void setVideoURI(String uri) {
        if (Config.DEBUG) {
            Log.i(TAG, "Video URI: " + uri);
        }
        mVideo.setVideoURI(Uri.parse(uri));
    }

    public void setVideo(Item<VideoItemDetail> video) {
        int connectivityStatus = NetworkUtil.getConnectivityStatus(mActivity);

        if (connectivityStatus == NetworkUtil.TYPE_WIFI
                || (connectivityStatus == NetworkUtil.TYPE_MOBILE && !AppPreferences.isVideoQualityLimitedOnMobile(mActivity))) {
            setVideoURI(video.detail.stream.hd_url);
        } else {
            setVideoURI(video.detail.url);
        }
    }

    public void enableHeader() {
        mVideoHeader.setVisibility(View.VISIBLE);
    }

    public void disableHeader() {
        mVideoHeader.setVisibility(View.GONE);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mVideo != null) {
            outState.putInt(KEY_TIME, mVideo.getCurrentPosition());
            outState.putBoolean(KEY_ISPLAYING, mVideo.isPlaying());
        }
    }

    public void returnFromSavedInstanceState(Bundle savedInstanceState) {
        if (mVideo != null) {
            wasSaved = true;
            savedTime = savedInstanceState.getInt(KEY_TIME);
            savedIsPlaying = savedInstanceState.getBoolean(KEY_ISPLAYING);
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

    public interface OnFullscreenClickListener {

        public void onFullscreenClick(int currentPosition, boolean isPlaying);

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
