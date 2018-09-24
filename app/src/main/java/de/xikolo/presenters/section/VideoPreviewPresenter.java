package de.xikolo.presenters.section;

import android.content.res.Configuration;

import de.xikolo.models.Video;
import de.xikolo.utils.CastUtil;
import de.xikolo.utils.LanalyticsUtil;
import io.realm.RealmResults;

public class VideoPreviewPresenter extends ItemPresenter<VideoPreviewView> {

    public static final String TAG = VideoPreviewPresenter.class.getSimpleName();

    private RealmResults videoPromise;
    private Video video;

    VideoPreviewPresenter(String courseId, String sectionId, String itemId) {
        super(courseId, sectionId, itemId);
    }

    @Override
    public void onViewAttached(VideoPreviewView v) {
        super.onViewAttached(v);

        if (video == null) {
            requestItem(false);
        }

        videoPromise = itemManager.getVideoForItem(item.contentId, realm, (result) -> {
            if (result.size() > 0) {
                video = realm.copyFromRealm(result.first());
                if (isViewAttached()) {
                    getView().showContent();
                    getView().setupView(course, section, item, video);
                }
            }
        });
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (videoPromise != null) {
            videoPromise.removeAllChangeListeners();
        }
    }

    public void onPlayClicked() {
        if (CastUtil.isConnected()) {
            LanalyticsUtil.trackVideoPlay(item.id, course.id, section.id, video.progress, 1.0f,
                    Configuration.ORIENTATION_LANDSCAPE, "hd", "cast");
            getViewOrThrow().startCast(video);
        } else {
            getViewOrThrow().startVideo(video);
        }
    }

}
