package de.xikolo.controller.module;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;

import de.xikolo.R;
import de.xikolo.controller.VideoActivity;
import de.xikolo.controller.helper.CacheController;
import de.xikolo.controller.helper.ImageController;
import de.xikolo.controller.helper.NotificationController;
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
import de.xikolo.view.CustomSizeImageView;

public class VideoFragment extends PagerFragment<VideoItemDetail> {

    public static final String TAG = VideoFragment.class.getSimpleName();

    public static final String KEY_COURSE = "key_course";
    public static final String KEY_MODULE = "key_module";
    public static final String KEY_ITEM = "key_item";

    private VideoItemDetail itemDetail;

    private TextView mTitle;
    private View mContainer;
    private NotificationController mNotificationController;
    private LinearLayout mLinearLayoutDownloads;
    private ViewGroup mVideoPreview;
    private CustomSizeImageView mVideoThumbnail;
    private ViewGroup mVideoMetadata;
    private View mPlayButton;
    private TextView mDurationtext;

    private ItemModel mItemModel;

    private VideoCastManager mCastManager;
    public VideoFragment() {

    }

    public static PagerFragment newInstance(Course course, Module module, Item item) {
        return PagerFragment.newInstance(new VideoFragment(), course, module, item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItemModel = new ItemModel(jobManager);

        mCastManager = VideoCastManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_video, container, false);

        mContainer = layout.findViewById(R.id.container);

        mNotificationController = new NotificationController(layout);

        mTitle = (TextView) layout.findViewById(R.id.textTitle);

        mLinearLayoutDownloads = (LinearLayout) layout.findViewById(R.id.containerDownloads);

        mVideoPreview = (ViewGroup) layout.findViewById(R.id.videoPreview);
        mVideoThumbnail = (CustomSizeImageView) layout.findViewById(R.id.videoThumbnail);
        mVideoMetadata = (ViewGroup) layout.findViewById(R.id.videoMetadata);

        mPlayButton = layout.findViewById(R.id.playButton);
        mDurationtext = (TextView) layout.findViewById(R.id.durationText);

        mContainer.setVisibility(View.GONE);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mVideoThumbnail.setDimensions(size.x, size.x / 16 * 9);
        } else {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mVideoThumbnail.setDimensions((int) (size.x * 0.6), (int) (size.x * 0.6 / 16 * 9));

            ViewGroup.LayoutParams params_meta = mVideoMetadata.getLayoutParams();
            params_meta.width = (int) (size.x * 0.6);
            mVideoMetadata.setLayoutParams(params_meta);
        }

        if (savedInstanceState != null) {
            mItem = savedInstanceState.getParcelable(KEY_ITEM);
            setupView();
        } else {
            requestVideoDetails(false);
        }

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ITEM, mItem);
    }

    private void requestVideoDetails(final boolean userRequest) {
        Result<Item> result = new Result<Item>() {
            @Override
            protected void onSuccess(Item result, DataSource dataSource) {
                @SuppressWarnings("unchecked")
                Item<VideoItemDetail> item = (Item<VideoItemDetail>) result;
                mItem = item;

                if (!NetworkUtil.isOnline(getActivity()) && dataSource.equals(DataSource.LOCAL) && result.detail == null) {
                    mNotificationController.setTitle(R.string.notification_no_network);
                    mNotificationController.setSummary(R.string.notification_no_network_with_offline_mode_summary);
                    mNotificationController.setNotificationVisible(true);
                    mNotificationController.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestVideoDetails(true);
                        }
                    });
                } else if (result.detail != null) {
                    setupView();
                }
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                ToastUtil.show(R.string.error);
                mNotificationController.setInvisible();
                mContainer.setVisibility(View.GONE);
            }

            @Override
            protected void onWarning(WarnCode warnCode) {
                if (warnCode == WarnCode.NO_NETWORK && userRequest) {
                    NetworkUtil.showNoConnectionToast();
                }
            }
        };

        mContainer.setVisibility(View.GONE);
        mNotificationController.setProgressVisible(true);
        mItemModel.getItemDetail(result, mCourse, mModule, mItem, Item.TYPE_VIDEO);
    }

    private void setupView() {
        if (isAdded()) {
            mNotificationController.setInvisible();
            mContainer.setVisibility(View.VISIBLE);

            if (mItem.detail == null) {
                throw new NullPointerException("Item Detail is null for Course " + mCourse.name + " (" + mCourse.id + ")" +
                        " and Module " + mModule.name + " (" + mModule.id + ")" +
                        " and Item " + mItem.title + " (" + mItem.id + ")");
            } else if (mItem.detail.stream == null) {
                throw new NullPointerException("Item Stream is null for Course " + mCourse.name + " (" + mCourse.id + ")" +
                        " and Module " + mModule.name + " (" + mModule.id + ")" +
                        " and Item " + mItem.title + " (" + mItem.id + ")");
            }

            ImageController.load(mItem.detail.stream.poster, mVideoThumbnail);

            mTitle.setText(mItem.detail.title);

            mLinearLayoutDownloads.removeAllViews();
            DownloadViewController hdVideo = new DownloadViewController(getActivity(), DownloadModel.DownloadFileType.VIDEO_HD, mCourse, mModule, mItem);
            mLinearLayoutDownloads.addView(hdVideo.getView());
            DownloadViewController sdVideo = new DownloadViewController(getActivity(), DownloadModel.DownloadFileType.VIDEO_SD, mCourse, mModule, mItem);
            mLinearLayoutDownloads.addView(sdVideo.getView());
            DownloadViewController slides = new DownloadViewController(getActivity(), DownloadModel.DownloadFileType.SLIDES, mCourse, mModule, mItem);
            mLinearLayoutDownloads.addView(slides.getView());
//        DownloadViewController transcript = new DownloadViewController(getActivity(), DownloadModel.DownloadFileType.TRANSCRIPT, mCourse, mModule, mItem);
//        mLinearLayoutDownloads.addView(transcript.getView());

            mDurationtext.setText(getString(R.string.duration, Integer.valueOf(mItem.detail.minutes), Integer.valueOf(mItem.detail.seconds)));

            mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCastManager.isConnected()) {
                        mItemModel.getLocalVideoProgress(new Result<VideoItemDetail>() {
                            @Override
                            protected void onSuccess(VideoItemDetail result, DataSource dataSource) {
                                mItem.detail = result;
                                setCurrentCourse();
                                mCastManager.startVideoCastControllerActivity(getActivity(), buildCastMetadata(), result.progress, true);
                            }

                            @Override
                            protected void onError(ErrorCode errorCode) {
                                setCurrentCourse();
                                mCastManager.startVideoCastControllerActivity(getActivity(), buildCastMetadata(), 0, true);
                            }
                        }, mItem.detail);
                    } else {
                        Intent intent = new Intent(getActivity(), VideoActivity.class);
                        Bundle b = new Bundle();
                        b.putParcelable(KEY_COURSE, mCourse);
                        b.putParcelable(KEY_MODULE, mModule);
                        b.putParcelable(KEY_ITEM, mItem);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    private MediaInfo buildCastMetadata() {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, mItem.title);
        WebImage image = new WebImage(Uri.parse(mItem.detail.stream.poster));
        // small size image used for notification, mini­controller and Lock Screen on JellyBean
        mediaMetadata.addImage(image);
        // large image, used on the Cast Player page and Lock Screen on KitKat
        mediaMetadata.addImage(image);
        MediaInfo mediaInfo = new MediaInfo.Builder(
                mItem.detail.stream.hd_url)
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
        return mediaInfo;
    }

    private void setCurrentCourse() {
        CacheController cacheController = new CacheController();
        Intent i = getActivity().getIntent();
        if (i.getExtras() != null) {
            Bundle b = getActivity().getIntent().getExtras();
            cacheController.setCachedExtras(b);
        }
    }

}
