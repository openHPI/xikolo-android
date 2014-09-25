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

import de.xikolo.R;
import de.xikolo.controller.VideoActivity;
import de.xikolo.controller.helper.VideoController;
import de.xikolo.entities.Course;
import de.xikolo.entities.Item;
import de.xikolo.entities.ItemVideo;
import de.xikolo.entities.Module;
import de.xikolo.model.ItemModel;
import de.xikolo.model.OnModelResponseListener;
import de.xikolo.util.NetworkUtil;

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

    private ItemModel mItemModel;

    private boolean wasSaved = false;

    public VideoFragment() {

    }

    public static PagerFragment newInstance(Course course, Module module, Item item) {
        return PagerFragment.newInstance(new VideoFragment(), course, module, item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItemModel = new ItemModel(getActivity(), jobManager);
        mItemModel.setRetrieveItemDetailListener(new OnModelResponseListener<Item>() {
            @Override
            public void onResponse(final Item response) {
                if (response != null) {
                    mItem = response;
                    setupVideo();
                }
            }
        });
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
            if (NetworkUtil.isOnline(getActivity())) {
                mItemModel.retrieveItemDetail(mCourse.id, mModule.id, mItem.id, Item.TYPE_VIDEO, true);
            } else {
                NetworkUtil.showNoConnectionToast(getActivity());
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
        if (mVideoController != null) {
            mVideoController.pause();
            mVideoController.show();
        }
    }

    @Override
    public void pageScrolling(int state) {
        if (mVideoController != null) {
            mVideoController.hide();
        }
    }

}
