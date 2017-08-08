package de.xikolo.presenters.announcement;

import de.xikolo.managers.AnnouncementManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Announcement;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;

public class AnnouncementPresenter extends LoadingStatePresenter<AnnouncementView> {

    public static final String TAG = AnnouncementPresenter.class.getSimpleName();

    private AnnouncementManager announcementManager;

    private Realm realm;

    private String announcementId;

    private Announcement announcement;

    private boolean global;

    AnnouncementPresenter(String announcementId, boolean global) {
        this.announcementManager = new AnnouncementManager();
        this.realm = Realm.getDefaultInstance();
        this.announcementId = announcementId;
        this.global = global;

        loadModels();
    }

    @Override
    public void onRefresh() {
        getView().showProgress();
        loadModels();
        getView().hideProgress();
    }

    @Override
    public void onViewAttached(AnnouncementView v) {
        super.onViewAttached(v);
        getView().showContent();
        getView().showAnnouncement(announcement);

        if (global && announcement.courseId != null) {
            Course course = Course.get(announcement.courseId);
            if (course.accessible && course.isEnrolled()) {
                getView().enableCourseButton();
            }
        }

        if (!announcement.visited && UserManager.isAuthorized()) {
            announcementManager.updateAnnouncementVisited(announcementId);
            announcement.visited = true;
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    public void onCourseButtonClicked() {
        getView().enterCourse(announcement.courseId);
    }

    private void loadModels() {
        if (announcement == null) {
            announcement = realm.copyFromRealm(Announcement.get(announcementId));
        }
    }

}
