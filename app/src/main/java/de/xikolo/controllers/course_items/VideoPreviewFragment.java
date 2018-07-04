package de.xikolo.controllers.course_items;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yatatsu.autobundle.AutoBundleField;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.controllers.video.VideoActivityAutoBundle;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course_items.VideoPreviewPresenter;
import de.xikolo.presenters.course_items.VideoPreviewPresenterFactory;
import de.xikolo.presenters.course_items.VideoPreviewView;
import de.xikolo.utils.CastUtil;
import de.xikolo.utils.DownloadUtil;
import de.xikolo.views.CustomSizeImageView;

import static de.xikolo.utils.DownloadUtil.AbstractItemAsset.SLIDES;
import static de.xikolo.utils.DownloadUtil.AbstractItemAsset.VIDEO_HD;
import static de.xikolo.utils.DownloadUtil.AbstractItemAsset.VIDEO_SD;

public class VideoPreviewFragment extends LoadingStatePresenterFragment<VideoPreviewPresenter, VideoPreviewView> implements VideoPreviewView {

    public static final String TAG = VideoPreviewFragment.class.getSimpleName();

    @AutoBundleField String courseId;
    @AutoBundleField String sectionId;
    @AutoBundleField String itemId;

    @BindView(R.id.textTitle) TextView textTitle;
    @BindView(R.id.durationText) TextView textDuration;
    @BindView(R.id.videoThumbnail) CustomSizeImageView imageVideoThumbnail;
    @BindView(R.id.containerDownloads) LinearLayout linearLayoutDownloads;
    @BindView(R.id.refresh_layout) View viewContainer;
    @BindView(R.id.playButton) View viewPlay;
    @BindView(R.id.videoMetadata) ViewGroup videoMetadata;

    DownloadViewController hdVideo;
    DownloadViewController sdVideo;
    DownloadViewController slides;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_video;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.viewContainer.setVisibility(View.GONE);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            imageVideoThumbnail.setDimensions(size.x, size.x / 16 * 9);
        } else {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            imageVideoThumbnail.setDimensions((int) (size.x * 0.6), (int) (size.x * 0.6 / 16 * 9));

            ViewGroup.LayoutParams params_meta = videoMetadata.getLayoutParams();
            params_meta.width = (int) (size.x * 0.6);
            videoMetadata.setLayoutParams(params_meta);
        }
    }

    @Override
    public void setupView(Course course, Section section, Item item, Video video) {
        hideProgress();
        viewContainer.setVisibility(View.VISIBLE);

        GlideApp.with(this)
                .load(video.thumbnailUrl)
                .override(imageVideoThumbnail.getForcedWidth(), imageVideoThumbnail.getForcedHeight())
                .into(imageVideoThumbnail);

        textTitle.setText(video.title);

        linearLayoutDownloads.removeAllViews();

        hdVideo = new DownloadViewController(getActivity(), DownloadUtil.getDefaultVideoAssetDownload(DownloadUtil.getVideoAssetType(course, section, item, video, VIDEO_HD)));
        linearLayoutDownloads.addView(hdVideo.getLayout());
        sdVideo = new DownloadViewController(getActivity(), DownloadUtil.getDefaultVideoAssetDownload(DownloadUtil.getVideoAssetType(course, section, item, video, VIDEO_SD)));
        linearLayoutDownloads.addView(sdVideo.getLayout());
        slides = new DownloadViewController(getActivity(), DownloadUtil.getDefaultVideoAssetDownload(DownloadUtil.getVideoAssetType(course, section, item, video, SLIDES)));
        linearLayoutDownloads.addView(slides.getLayout());

        long minutes = TimeUnit.SECONDS.toMinutes(video.duration);
        long seconds = video.duration - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(video.duration));
        textDuration.setText(getString(R.string.duration, minutes, seconds));

        viewPlay.setOnClickListener(v -> presenter.onPlayClicked());
    }

    @Override
    public void startVideo(Video video) {
        Intent intent = VideoActivityAutoBundle.builder(courseId, sectionId, itemId, video.id).build(getActivity());
        startActivity(intent);
    }

    @Override
    public void startCast(Video video) {
        CastUtil.loadMedia(getActivity(), video, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                presenter.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (hdVideo != null) {
            hdVideo.onDestroy();
        }
        if (sdVideo != null) {
            sdVideo.onDestroy();
        }
        if (slides != null) {
            slides.onDestroy();
        }
    }

    @NonNull
    @Override
    protected PresenterFactory<VideoPreviewPresenter> getPresenterFactory() {
        return new VideoPreviewPresenterFactory(courseId, sectionId, itemId);
    }

}
