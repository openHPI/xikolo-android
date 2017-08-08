package de.xikolo.presenters.course_items;

import android.content.res.Configuration;

import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.ItemManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.presenters.base.LoadingStatePresenter;
import de.xikolo.utils.CastUtil;
import de.xikolo.utils.LanalyticsUtil;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class VideoPreviewPresenter extends LoadingStatePresenter<VideoPreviewView> {

    public static final String TAG = VideoPreviewPresenter.class.getSimpleName();

    private ItemManager itemManager;

    private Realm realm;

    private String courseId;
    private String sectionId;
    private String itemId;

    private Course course;
    private Section section;
    private Item item;

    private RealmResults videoPromise;
    private Video video;

    VideoPreviewPresenter(String courseId, String sectionId, String itemId) {
        this.itemManager = new ItemManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.itemId = itemId;

        loadModels();
    }

    @Override
    public void onViewAttached(VideoPreviewView v) {
        super.onViewAttached(v);

        if (video == null) {
            requestVideo();
        }

        videoPromise = itemManager.getVideoForItem(itemId, realm, new RealmChangeListener<RealmResults<Video>>() {
            @Override
            public void onChange(RealmResults<Video> result) {
                if (result.size() > 0) {
                    video = realm.copyFromRealm(result.first());
                    getViewOrThrow().showContent();
                    getViewOrThrow().setupView(course, section, item, video);
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

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestVideo();
    }

    public void onPlayClicked() {
        if (CastUtil.isConnected()) {
            LanalyticsUtil.trackVideoPlay(item.id, course.id, section.id, video.progress, 1.0f,
                    Configuration.ORIENTATION_LANDSCAPE, "hd", LanalyticsUtil.CONTEXT_CAST);
            getViewOrThrow().startCast(video);
        } else {
            getViewOrThrow().startVideo(video);
        }
    }

    private void loadModels() {
        if (course == null) {
            course = realm.copyFromRealm(Course.get(courseId));
        }
        if (section == null) {
            section = realm.copyFromRealm(Section.get(sectionId));
        }
        if (item == null) {
            item = realm.copyFromRealm(Item.get(itemId));
        }
    }

    private void requestVideo() {
        if (getView() != null) {
            getView().showProgress();
        }
        itemManager.requestItemWithContent(itemId, new JobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgress();
                }
            }

            @Override
            public void onError(JobCallback.ErrorCode code) {
                if (getView() != null) {
                    switch (code) {
                        case NO_NETWORK:
                            getView().showNetworkRequiredMessage();
                            break;
                        case CANCEL:
                        case ERROR:
                            getView().showErrorMessage();
                            break;
                    }
                }
            }
        });
    }

}
