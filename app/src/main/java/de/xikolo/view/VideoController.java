package de.xikolo.view;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import de.xikolo.R;

public class VideoController {

    public static final String TAG = VideoController.class.getSimpleName();

    public static final String KEY_TIME = "key_time";
    public static final String KEY_ISPLAYING = "key_isplaying";

    private static final int MILLISECONDS = 100;

    private Activity mActivity;

    private View mVideoContainer;

    private View mVideoProgress;

    private MediaController mMediaController;

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

    private Runnable mSeekBarUpdater;

    private boolean wasSaved = false;
    private int savedTime = 0;
    private boolean savedIsPlaying = false;

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

        setup();
    }

    private void setup() {
        mMediaController = new MediaController(mActivity) {
            @Override
            public void show() {
                super.show();
                showController();
            }

            @Override
            public void hide() {
                super.hide();
                hideController();
            }

        };

        mMediaController.setKeepScreenOn(true);
        mMediaController.setMediaPlayer(mVideo);
        mVideo.setMediaController(mMediaController);

        mMediaController.setVisibility(View.GONE);
        mMediaController.removeAllViews();

        mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoProgress.setVisibility(View.GONE);
                mSeekBar.setMax(mVideo.getDuration());
                mMediaController.show();

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

        mSeekBarUpdater = new Runnable() {
            @Override
            public void run() {
                mSeekBar.setProgress(mVideo.getCurrentPosition());
                mCurrentTime.setText(getTimeString(mVideo.getCurrentPosition()));
                if (mVideo.getCurrentPosition() < mVideo.getDuration()) {
                    mSeekBar.postDelayed(this, MILLISECONDS);
                }
            }
        };

        mFullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFullscreenListener != null) {
                    mFullscreenListener.onFullscreenClick(mVideo.getCurrentPosition(), mVideo.isPlaying());
                }
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    mMediaController.show();
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
    }

    public void start() {
        mPlayButton.setText(mActivity.getString(R.string.icon_pause));
        mVideo.start();
    }

    public void pause() {
        mPlayButton.setText(mActivity.getString(R.string.icon_play));
        mVideo.pause();
    }

    public void showController() {
        this.mVideoController.setVisibility(View.VISIBLE);
    }

    public void hideController() {
        this.mVideoController.setVisibility(View.GONE);
    }

    public void setDimensions(int w, int h) {
        mVideo.setDimensions(w, h);
    }

    public void setVideoURI(Uri uri) {
        mVideo.setVideoURI(uri);
    }

    public void enableHeader() {
        mVideoHeader.setVisibility(View.VISIBLE);
    }

    public void disableHeader() {
        mVideoHeader.setVisibility(View.GONE);
    }

    public void setOnFullscreenButtonClickedListener(OnFullscreenClickListener listener) {
        this.mFullscreenListener = listener;
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

    public interface OnFullscreenClickListener {

        public void onFullscreenClick(int currentPosition, boolean isPlaying);

    }

}
