package de.xikolo.presenters.course_items;

import android.content.res.Configuration;

import de.xikolo.managers.ItemManager;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.presenters.base.LoadingStatePresenter;
import de.xikolo.utils.CastUtil;
import de.xikolo.utils.LanalyticsUtil;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;

public class VideoPreviewPresenter implements LoadingStatePresenter<VideoPreviewView> {

    public static final String TAG = VideoPreviewPresenter.class.getSimpleName();

    private VideoPreviewView view;

    private ItemManager itemManager;

    private Realm realm;

    private String courseId;
    private String sectionId;
    private String itemId;

    private Course course;
    private Section section;
    private Item item;

    private RealmObject videoPromise;
    private Video video;

    VideoPreviewPresenter(String courseId, String sectionId, String itemId) {
        this.itemManager = new ItemManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.itemId = itemId;
    }

    @Override
    public void onViewAttached(VideoPreviewView v) {
        this.view = v;

        loadModels();

        if (video == null) {
            requestVideo();
        }

        videoPromise = itemManager.getVideoForItem(itemId, realm, new RealmChangeListener<Video>() {
            @Override
            public void onChange(Video v) {
                video = v;
                view.setupView(course, section, item, video);
            }
        });
    }

    @Override
    public void onViewDetached() {
        this.view = null;

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
            view.startCast(video);
        } else {
            view.startVideo(video);
        }
    }

    private void loadModels() {
        if (course == null) {
            course = Course.get(courseId);
        }
        if (section == null) {
            section = Section.get(sectionId);
        }
        if (item == null) {
            item = Item.get(itemId);
        }
    }

    private void requestVideo() {
        if (video == null) {
            view.showProgressMessage();
        } else {
            view.showRefreshProgress();
        }
        itemManager.requestItemWithContent(itemId, new JobCallback() {
            @Override
            public void onSuccess() {
                view.hideAnyProgress();
            }

            @Override
            public void onError(JobCallback.ErrorCode code) {
                switch (code) {
                    case NO_NETWORK:
                        if (video == null) {
                            view.showNetworkRequiredMessage();
                        } else {
                            view.showNetworkRequiredToast();
                        }
                        break;
                    case CANCEL:
                    case ERROR:
                        view.showErrorToast();
                        break;
                }
            }
        });
    }

}
