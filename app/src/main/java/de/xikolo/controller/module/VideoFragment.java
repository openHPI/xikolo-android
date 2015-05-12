package de.xikolo.controller.module;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.xikolo.R;
import de.xikolo.controller.VideoActivity;
import de.xikolo.controller.helper.NotificationController;
import de.xikolo.controller.helper.VideoController;
import de.xikolo.controller.module.helper.DownloadViewController;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.model.DownloadModel;
import de.xikolo.model.ItemModel;
import de.xikolo.model.Result;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class VideoFragment extends PagerFragment<VideoItemDetail> {

    public static final String TAG = VideoFragment.class.getSimpleName();

    public static final int FULL_SCREEN_REQUEST = 1;

    public static final String KEY_COURSE = "key_course";
    public static final String KEY_MODULE = "key_module";
    public static final String KEY_ITEM = "key_item";

    private TextView mTitle;
    private View mContainer;

    private NotificationController mNotificationController;

    private LinearLayout mLinearLayoutDownloads;

    private ViewGroup mVideoContainer;
    private ViewGroup mVideoMetadata;

    private VideoController mVideoController;

    private ItemModel mItemModel;

    private boolean wasSaved = false;

    Result<Void> saveVideoProgressResult = new Result<Void>() {
        @Override
        protected void onSuccess(Void result, DataSource dataSource) {
            super.onSuccess(result, dataSource);
            System.out.println("SUCCESS SAVING VIDEO");
        }

        @Override
        protected void onWarning(WarnCode warnCode) {
            super.onWarning(warnCode);
            System.out.println("WARNING SAVING VIDEO");
        }

        @Override
        protected void onError(ErrorCode errorCode) {
            super.onError(errorCode);
            System.out.println("ERROR SAVING VIDEO");
        }
    };

    public VideoFragment() {

    }

    public static PagerFragment newInstance(Course course, Module module, Item item) {
        return PagerFragment.newInstance(new VideoFragment(), course, module, item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItemModel = new ItemModel(getActivity(), jobManager, databaseHelper);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_video, container, false);

        mContainer = layout.findViewById(R.id.container);

        mNotificationController = new NotificationController(layout);

        mTitle = (TextView) layout.findViewById(R.id.textTitle);

        mLinearLayoutDownloads = (LinearLayout) layout.findViewById(R.id.containerDownloads);
        
        mVideoContainer = (ViewGroup) layout.findViewById(R.id.videoContainer);
        mVideoMetadata = (ViewGroup) layout.findViewById(R.id.videoMetadata);

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

            ViewGroup.LayoutParams params_meta = mVideoMetadata.getLayoutParams();
            params_meta.width = (int) (size.x * 0.6);
            mVideoMetadata.setLayoutParams(params_meta);
        }

        mVideoController.setOnFullscreenButtonClickedListener(new VideoController.OnFullscreenClickListener() {
            @Override
            public void onFullscreenClick(int currentPosition, boolean isPlaying, boolean isVideoQualityInHD, boolean didUserChangeVideoQuality) {
                Intent intent = new Intent(getActivity(), VideoActivity.class);
                Bundle b = new Bundle();
                b.putParcelable(KEY_COURSE, mCourse);
                b.putParcelable(KEY_MODULE, mModule);
                b.putParcelable(KEY_ITEM, mItem);
//                b.putInt(VideoController.KEY_TIME, currentPosition);
                b.putBoolean(VideoController.KEY_ISPLAYING, isPlaying);
                b.putBoolean(VideoController.KEY_VIDEO_QUALITY, isVideoQualityInHD);
                b.putBoolean(VideoController.KEY_DID_USER_CHANGE_QUALITY, didUserChangeVideoQuality);
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

    @Override
    public void onStart() {
        super.onStart();

        if (!wasSaved) {
            requestVideo(false);
        } else {
            setupVideo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mItemModel.updateVideo(saveVideoProgressResult, mVideoController.getVideoItemDetail());
    }

    private void requestVideo(final boolean userRequest) {
        Result<Item> result = new Result<Item>() {
            @Override
            protected void onSuccess(Item result, DataSource dataSource) {
                mItem = result;

                if (!NetworkUtil.isOnline(getActivity()) && dataSource.equals(DataSource.LOCAL) && result.detail == null) {
                    mNotificationController.setTitle(R.string.notification_no_network);
                    mNotificationController.setSummary(R.string.notification_no_network_with_offline_mode_summary);
                    mNotificationController.setNotificationVisible(true);
                    mNotificationController.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestVideo(true);
                        }
                    });
                } else if (result.detail != null) {
                    setupVideo();
                }
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                ToastUtil.show(getActivity(), R.string.error);
                mNotificationController.setInvisible();
                mContainer.setVisibility(View.GONE);
            }

            @Override
            protected void onWarning(WarnCode warnCode) {
                if (warnCode == WarnCode.NO_NETWORK && userRequest) {
                    NetworkUtil.showNoConnectionToast(getActivity());
                }
            }
        };

        mContainer.setVisibility(View.GONE);
        mNotificationController.setProgressVisible(true);
        mItemModel.getItemDetail(result, mCourse, mModule, mItem, Item.TYPE_VIDEO);
    }

    private void setupVideo() {
        wasSaved = true;
        mNotificationController.setInvisible();
        mContainer.setVisibility(View.VISIBLE);

        mVideoController.setVideo(mCourse, mModule, mItem);

        mTitle.setText(mItem.detail.title);

        mLinearLayoutDownloads.removeAllViews();
        DownloadViewController hdVideo = new DownloadViewController(getActivity(), mVideoController, DownloadModel.DownloadFileType.VIDEO_HD, mCourse, mModule, mItem);
        mLinearLayoutDownloads.addView(hdVideo.getView());
        DownloadViewController sdVideo = new DownloadViewController(getActivity(), mVideoController, DownloadModel.DownloadFileType.VIDEO_SD, mCourse, mModule, mItem);
        mLinearLayoutDownloads.addView(sdVideo.getView());
        DownloadViewController slides = new DownloadViewController(getActivity(), mVideoController, DownloadModel.DownloadFileType.SLIDES, mCourse, mModule, mItem);
        mLinearLayoutDownloads.addView(slides.getView());
//        DownloadViewController transcript = new DownloadViewController(DownloadModel.DownloadFileType.TRANSCRIPT, mCourse, mModule, mItem);
//        mLinearLayoutDownloads.addView(transcript.getView());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ITEM, mItem);
        if (mVideoController != null) {
            mVideoController.onSaveInstanceState(outState);
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
        if (mVideoController != null) {
            mVideoController.pause();
            mVideoController.show();
            mItemModel.updateVideo(saveVideoProgressResult, mVideoController.getVideoItemDetail());
        }
    }

    @Override
    public void pageScrolling(int state) {
        if (mVideoController != null) {
            mVideoController.hide();
        }
    }

}
