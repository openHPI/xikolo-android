package de.xikolo.controller.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import de.xikolo.R;
import de.xikolo.controller.VideoActivity;
import de.xikolo.manager.ItemObjectManager;
import de.xikolo.model.Course;
import de.xikolo.model.Item;
import de.xikolo.model.Module;
import de.xikolo.model.Video;
import de.xikolo.util.Network;
import de.xikolo.view.CustomVideoView;

public class VideoFragment extends PagerFragment<Video> {

    public static final String TAG = VideoFragment.class.getSimpleName();

    public static final int FULL_SCREEN_REQUEST = 1;

    public static final String KEY_TIME = "key_time";
    public static final String KEY_ISPLAYING = "key_isplaying";
    public static final String KEY_ITEM = "key_item";

    private ImageView mBtnFullscreen;
    private CustomVideoView mVideo;
    private TextView mTitle;
    private TextView mTime;
    private ViewGroup mVideoContainer;
    private View mContainer;
    private ProgressBar mProgress;
    private View mProgressVideo;
    private View mVideoHeader;
    private MediaController mVideoController;
    private ItemObjectManager mItemManager;

    private int time = 0;
    private boolean isPlaying = false;
    private boolean isLoaded = false;

    private boolean isRunning;

    public VideoFragment() {

    }

    public static PagerFragment newInstance(Course course, Module module, Item item) {
        return PagerFragment.newInstance(new VideoFragment(), course, module, item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_video, container, false);

        mContainer = layout.findViewById(R.id.container);
        mProgress = (ProgressBar) layout.findViewById(R.id.progress);

        mBtnFullscreen = (ImageView) layout.findViewById(R.id.btnFullscreen);
        mTitle = (TextView) layout.findViewById(R.id.textTitle);
        mTime = (TextView) layout.findViewById(R.id.textTime);
        mVideoContainer = (ViewGroup) layout.findViewById(R.id.videoContainer);
        mVideo = (CustomVideoView) layout.findViewById(R.id.video);

        mVideoHeader = layout.findViewById(R.id.videoHeader);
        mProgressVideo = layout.findViewById(R.id.progressVideo);

        mVideoController = new MediaController(getActivity()) {
            @Override
            public void show() {
                super.show();
                mVideoHeader.setVisibility(View.VISIBLE);
            }

            @Override
            public void hide() {
                super.hide();
                mVideoHeader.setVisibility(View.GONE);
            }
        };

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mVideo.setDimensions(size.x, size.x / 16 * 9);
        } else {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mVideo.setDimensions((int) (size.x * 0.6), (int) (size.x * 0.6 / 16 * 9));
            ViewGroup.LayoutParams params = mVideoContainer.getLayoutParams();
            params.width = (int) (size.x * 0.6);
            mVideoContainer.setLayoutParams(params);
        }

        mBtnFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int time = 0;
                if (mVideo != null) {
                    mVideo.pause();
                    time = mVideo.getCurrentPosition();
                }
                Intent intent = new Intent(getActivity(), VideoActivity.class);
                Bundle b = new Bundle();
                b.putParcelable(KEY_ITEM, mItem);
                b.putInt(KEY_TIME, time);
                b.putBoolean(KEY_ISPLAYING, mVideo.isPlaying());
                intent.putExtras(b);
                startActivityForResult(intent, FULL_SCREEN_REQUEST);
                mVideo.getCurrentPosition();
            }
        });

        mItemManager = new ItemObjectManager(getActivity()) {
            @Override
            public void onItemRequestReceived(Item item) {
                mItem = item;
                setupVideo();
            }

            @Override
            public void onItemRequestCancelled() {
            }
        };

        if (savedInstanceState != null) {
            isLoaded = true;
            time = savedInstanceState.getInt(KEY_TIME);
            isPlaying = savedInstanceState.getBoolean(KEY_ISPLAYING);
            mItem = savedInstanceState.getParcelable(KEY_ITEM);
        } else {
            mContainer.setVisibility(View.GONE);
            mVideoController.setVisibility(View.GONE);
        }

        return layout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FULL_SCREEN_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mProgressVideo.setVisibility(View.VISIBLE);
                isLoaded = true;
                time = data.getExtras().getInt(KEY_TIME);
                isPlaying = data.getExtras().getBoolean(KEY_ISPLAYING);
                mItem = data.getExtras().getParcelable(KEY_ITEM);
            }
        }
    }

    private void setupVideo() {
        isLoaded = true;

        mProgress.setVisibility(View.GONE);
        mContainer.setVisibility(View.VISIBLE);
        mVideoController.setVisibility(View.VISIBLE);

        mVideoController.setAnchorView(mVideo);
        mVideoController.setMediaPlayer(mVideo);
        mVideoController.setKeepScreenOn(true);

        Uri uri = Uri.parse(mItem.object.url);
        mVideo.setVideoURI(uri);
        mVideo.setMediaController(mVideoController);

        mTitle.setText(mItem.object.title);
        mTime.setText(mItem.object.minutes + ":" + mItem.object.seconds);

        mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mProgressVideo.setVisibility(View.GONE);
                mVideoController.show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!isLoaded) {
            if (Network.isOnline(getActivity())) {
                Type type = new TypeToken<Item<Video>>() {
                }.getType();
                mItemManager.requestItemObject(mCourse, mModule, mItem, type, true);
            } else {
                Network.showNoConnectionToast(getActivity());
            }
        } else {
            setupVideo();
            mVideo.seekTo(time);
            if (isPlaying) {
                mVideo.start();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        isRunning = true;
    }

    @Override
    public void onPause() {
        super.onPause();

        isRunning = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mVideo != null && isLoaded) {
            outState.putInt(KEY_TIME, mVideo.getCurrentPosition());
            outState.putBoolean(KEY_ISPLAYING, mVideo.isPlaying());
            outState.putParcelable(KEY_ITEM, mItem);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
//            case R.id.action_refresh:
//                onRefresh();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void pageChanged() {
        if (mVideo != null) {
            mVideo.pause();
        }
    }

    @Override
    public void pageScrolling(int state) {
        if (mVideoController != null) {
            mVideoController.hide();
        }
    }
}
