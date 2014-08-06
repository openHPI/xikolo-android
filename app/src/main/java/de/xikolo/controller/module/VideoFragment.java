package de.xikolo.controller.module;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import de.xikolo.R;
import de.xikolo.controller.VideoActivity;
import de.xikolo.manager.ItemDetailManager;
import de.xikolo.model.Course;
import de.xikolo.model.Item;
import de.xikolo.model.ItemVideo;
import de.xikolo.model.Module;
import de.xikolo.util.Network;
import de.xikolo.view.VideoController;

public class VideoFragment extends PagerFragment<ItemVideo> {

    public static final String TAG = VideoFragment.class.getSimpleName();

    public static final int FULL_SCREEN_REQUEST = 1;

    public static final String KEY_ITEM = "key_item";

    private TextView mTitle;
    private TextView mTime;
    private View mContainer;
    private ProgressBar mProgress;

    private ViewGroup mVideoContainer;

    private VideoController mVideoController;

    private ItemDetailManager mItemManager;

    private boolean wasSaved = false;

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

        mTitle = (TextView) layout.findViewById(R.id.textTitle);

        mTime = (TextView) layout.findViewById(R.id.textTime);
        mVideoContainer = (ViewGroup) layout.findViewById(R.id.videoContainer);

        mContainer.setVisibility(View.GONE);

        mVideoController = new VideoController(getActivity(), mVideoContainer);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mVideoController.setDimensions(size.x, size.x / 16 * 9);
        } else {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mVideoController.setDimensions((int) (size.x * 0.6), (int) (size.x * 0.6 / 16 * 9));
            ViewGroup.LayoutParams params = mVideoContainer.getLayoutParams();
            params.width = (int) (size.x * 0.6);
            mVideoContainer.setLayoutParams(params);
        }

        mVideoController.setOnFullscreenButtonClickedListener(new VideoController.OnFullscreenClickListener() {
            @Override
            public void onFullscreenClick(int currentPosition, boolean isPlaying) {
                Intent intent = new Intent(getActivity(), VideoActivity.class);
                Bundle b = new Bundle();
                b.putParcelable(KEY_ITEM, mItem);
                b.putInt(VideoController.KEY_TIME, currentPosition);
                b.putBoolean(VideoController.KEY_ISPLAYING, isPlaying);
                intent.putExtras(b);
                startActivityForResult(intent, FULL_SCREEN_REQUEST);
            }
        });

        mItemManager = new ItemDetailManager(getActivity()) {
            @Override
            public void onItemDetailRequestReceived(Item item) {
                mItem = item;
                setupVideo();
            }

            @Override
            public void onItemDetailRequestCancelled() {
            }
        };

        if (savedInstanceState != null) {
            wasSaved = true;
            mVideoController.returnFromSavedInstanceState(savedInstanceState);
            mItem = savedInstanceState.getParcelable(KEY_ITEM);
        } else {
            mContainer.setVisibility(View.GONE);
        }

        return layout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FULL_SCREEN_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                wasSaved = true;
                mVideoController.returnFromSavedInstanceState(data.getExtras());
                mItem = data.getExtras().getParcelable(KEY_ITEM);
            }
        }
    }

    private void setupVideo() {
        wasSaved = true;
        mProgress.setVisibility(View.GONE);
        mContainer.setVisibility(View.VISIBLE);

        Uri uri = Uri.parse(mItem.object.url);
        mVideoController.setVideoURI(uri);

        mTitle.setText(mItem.object.title);
        mTime.setText(mItem.object.minutes + ":" + mItem.object.seconds);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!wasSaved) {
            if (Network.isOnline(getActivity())) {
                Type type = new TypeToken<Item<ItemVideo>>() {
                }.getType();
                mItemManager.requestItemDetail(mCourse, mModule, mItem, type, true);
            } else {
                Network.showNoConnectionToast(getActivity());
            }
        } else {
            setupVideo();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ITEM, mItem);
        mVideoController.onSaveInstanceState(outState);
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
        mVideoController.pause();
        mVideoController.show();
    }

    @Override
    public void pageScrolling(int state) {
        mVideoController.hide();
    }

}
