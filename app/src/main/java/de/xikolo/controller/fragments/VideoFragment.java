package de.xikolo.controller.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
    private static final String KEY_TIME = "key_time";
    private static final String KEY_ISPLAYING = "key_isplaying";
    private static final String KEY_ITEM = "key_item";
    private ImageView mBtnFullscreen;
    private CustomVideoView mVideo;
    private TextView mTitle;
    private TextView mTime;
    private ViewGroup mVideoContainer;
    private View mContainer;
    private ProgressBar mProgress;
    private MediaController mVideoController;
    private ItemObjectManager mItemManager;
    private int time = 0;
    private boolean isPlaying = false;

    private boolean isLoaded = false;

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
        mContainer.setVisibility(View.GONE);

        mBtnFullscreen = (ImageView) layout.findViewById(R.id.btnFullscreen);
        mTitle = (TextView) layout.findViewById(R.id.textTitle);
        mTime = (TextView) layout.findViewById(R.id.textTime);
        mVideoContainer = (ViewGroup) layout.findViewById(R.id.videoContainer);
        mVideo = (CustomVideoView) layout.findViewById(R.id.video);
        mBtnFullscreen.setVisibility(View.GONE);

        mVideoController = new MediaController(getActivity()) {
            @Override
            public void show() {
                super.show();
                mBtnFullscreen.setVisibility(View.VISIBLE);
            }

            @Override
            public void hide() {
                super.hide();
                mBtnFullscreen.setVisibility(View.GONE);
            }
        };
        mVideoController.setAnchorView(mVideo);
        mVideoController.setKeepScreenOn(true);

        mVideo.setMediaController(mVideoController);

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
                b.putParcelable(VideoActivity.ARG_ITEM, mItem);
                b.putInt(VideoActivity.ARG_TIME, time);
                intent.putExtras(b);
                startActivity(intent);
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
            Log.w(TAG, "return");
            time = savedInstanceState.getInt(KEY_TIME);
            isPlaying = savedInstanceState.getBoolean(KEY_ISPLAYING);
            mItem = savedInstanceState.getParcelable(KEY_ITEM);
            setupVideo();
            mVideo.seekTo(time);
            if (isPlaying) {
                mVideo.start();
            }
        }

        return layout;
    }

    private void setupVideo() {
        isLoaded = true;

        mProgress.setVisibility(View.GONE);
        mContainer.setVisibility(View.VISIBLE);

        Uri uri = Uri.parse(mItem.object.url);
        mVideo.setVideoURI(uri);
        mTitle.setText(mItem.object.title);
        mTime.setText(mItem.object.minutes + ":" + mItem.object.seconds);
//                mVideoController.show();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!isLoaded) {
            Log.w(TAG, "new");
            if (Network.isOnline(getActivity())) {
                Type type = new TypeToken<Item<Video>>() {
                }.getType();
                mItemManager.requestItemObject(mCourse, mModule, mItem, type, true);
            } else {
                Network.showNoConnectionToast(getActivity());
            }
        }
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
    public void pageScrolling() {
        if (mVideoController != null) {
            mVideoController.hide();
        }
    }
}
