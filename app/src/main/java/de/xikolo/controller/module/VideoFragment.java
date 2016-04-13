package de.xikolo.controller.module;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;

import de.xikolo.GlobalApplication;
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
import de.xikolo.util.CastUtil;
import de.xikolo.util.LanalyticsUtil;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;
import de.xikolo.view.CustomSizeImageView;

public class VideoFragment extends PagerFragment<VideoItemDetail> {

    public static final String TAG = VideoFragment.class.getSimpleName();

    public static final String KEY_COURSE = "key_course";
    public static final String KEY_MODULE = "key_module";
    public static final String KEY_ITEM = "key_item";

    private TextView title;
    private TextView duration;
    private CustomSizeImageView videoThumbnail;
    private LinearLayout linearLayoutDownloads;
    private View container;
    private View playButton;

    private NotificationController notificationController;

    private ItemModel itemModel;

    private VideoCastManager castManager;

    public VideoFragment() {

    }

    public static PagerFragment newInstance(Course course, Module module, Item item) {
        return PagerFragment.newInstance(new VideoFragment(), course, module, item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemModel = new ItemModel(jobManager);

        castManager = VideoCastManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_video, container, false);

        this.container = layout.findViewById(R.id.container);

        notificationController = new NotificationController(layout);

        title = (TextView) layout.findViewById(R.id.textTitle);

        linearLayoutDownloads = (LinearLayout) layout.findViewById(R.id.containerDownloads);

        videoThumbnail = (CustomSizeImageView) layout.findViewById(R.id.videoThumbnail);

        ViewGroup videoMetadata;
        videoMetadata = (ViewGroup) layout.findViewById(R.id.videoMetadata);

        playButton = layout.findViewById(R.id.playButton);
        duration = (TextView) layout.findViewById(R.id.durationText);

        this.container.setVisibility(View.GONE);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            videoThumbnail.setDimensions(size.x, size.x / 16 * 9);
        } else {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            videoThumbnail.setDimensions((int) (size.x * 0.6), (int) (size.x * 0.6 / 16 * 9));

            ViewGroup.LayoutParams params_meta = videoMetadata.getLayoutParams();
            params_meta.width = (int) (size.x * 0.6);
            videoMetadata.setLayoutParams(params_meta);
        }

        if (savedInstanceState != null) {
            item = savedInstanceState.getParcelable(KEY_ITEM);
            setupView();
        } else {
            requestVideoDetails(false);
        }

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ITEM, item);
    }

    private void requestVideoDetails(final boolean userRequest) {
        Result<Item> result = new Result<Item>() {
            @Override
            protected void onSuccess(Item result, DataSource dataSource) {
                @SuppressWarnings("unchecked")
                Item<VideoItemDetail> item = (Item<VideoItemDetail>) result;
                VideoFragment.this.item = item;

                if (!NetworkUtil.isOnline(getActivity()) && dataSource.equals(DataSource.LOCAL) && result.detail == null) {
                    notificationController.setTitle(R.string.notification_no_network);
                    notificationController.setSummary(R.string.notification_no_network_with_offline_mode_summary);
                    notificationController.setNotificationVisible(true);
                    notificationController.setOnClickListener(new View.OnClickListener() {
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
                notificationController.setInvisible();
                container.setVisibility(View.GONE);
            }

            @Override
            protected void onWarning(WarnCode warnCode) {
                if (warnCode == WarnCode.NO_NETWORK && userRequest) {
                    NetworkUtil.showNoConnectionToast();
                }
            }
        };

        container.setVisibility(View.GONE);
        notificationController.setProgressVisible(true);
        itemModel.getItemDetail(result, course, module, item, Item.TYPE_VIDEO);
    }

    private void setupView() {
        if (isAdded()) {
            notificationController.setInvisible();
            container.setVisibility(View.VISIBLE);

            if (item.detail == null) {
                throw new NullPointerException("Item Detail is null for Course " + course.name + " (" + course.id + ")" +
                        " and Module " + module.name + " (" + module.id + ")" +
                        " and Item " + item.title + " (" + item.id + ")");
            } else if (item.detail.stream == null) {
                throw new NullPointerException("Item Stream is null for Course " + course.name + " (" + course.id + ")" +
                        " and Module " + module.name + " (" + module.id + ")" +
                        " and Item " + item.title + " (" + item.id + ")");
            }

            ImageController.load(item.detail.stream.poster, videoThumbnail,
                    ImageController.DEFAULT_PLACEHOLDER,
                    videoThumbnail.getForcedWidth(), videoThumbnail.getForcedHeight());

            title.setText(item.detail.title);

            linearLayoutDownloads.removeAllViews();
            DownloadViewController hdVideo = new DownloadViewController(getActivity(), DownloadModel.DownloadFileType.VIDEO_HD, course, module, item);
            linearLayoutDownloads.addView(hdVideo.getView());
            DownloadViewController sdVideo = new DownloadViewController(getActivity(), DownloadModel.DownloadFileType.VIDEO_SD, course, module, item);
            linearLayoutDownloads.addView(sdVideo.getView());
            DownloadViewController slides = new DownloadViewController(getActivity(), DownloadModel.DownloadFileType.SLIDES, course, module, item);
            linearLayoutDownloads.addView(slides.getView());
//        DownloadViewController transcript = new DownloadViewController(getActivity(), DownloadModel.DownloadFileType.TRANSCRIPT, course, module, item);
//        linearLayoutDownloads.addView(transcript.getView());

            duration.setText(getString(R.string.duration, Integer.valueOf(item.detail.minutes), Integer.valueOf(item.detail.seconds)));

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (castManager.isConnected()) {
                        itemModel.getLocalVideoProgress(new Result<VideoItemDetail>() {
                            @Override
                            protected void onSuccess(VideoItemDetail result, DataSource dataSource) {
                                LanalyticsUtil.trackVideoStartCast(item.id, course.id, module.id, result.progress);

                                item.detail = result;
                                setCurrentCourse();
                                castManager.startVideoCastControllerActivity(getActivity(), CastUtil.buildCastMetadata(item), result.progress, true);
                            }

                            @Override
                            protected void onError(ErrorCode errorCode) {
                                LanalyticsUtil.trackVideoStartCast(item.id, course.id, module.id, 0);

                                setCurrentCourse();
                                castManager.startVideoCastControllerActivity(getActivity(), CastUtil.buildCastMetadata(item), 0, true);
                            }
                        }, item.detail);
                    } else {
                        LanalyticsUtil.trackVideoPlay(item.id,
                                course.id, module.id,
                                item.detail.progress,
                                GlobalApplication.getInstance().getPreferencesFactory().getAppPreferences()
                                        .getVideoPlaybackSpeed().getSpeed(),
                                getActivity().getResources().getConfiguration().orientation);

                        Intent intent = new Intent(getActivity(), VideoActivity.class);
                        Bundle b = new Bundle();
                        b.putParcelable(KEY_COURSE, course);
                        b.putParcelable(KEY_MODULE, module);
                        b.putParcelable(KEY_ITEM, item);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                }
            });
        }
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
