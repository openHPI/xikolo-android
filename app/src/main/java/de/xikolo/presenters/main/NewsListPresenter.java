package de.xikolo.presenters.main;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.managers.AnnouncementManager;
import de.xikolo.models.Announcement;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class NewsListPresenter extends LoadingStatePresenter<NewsListView> {

    private String courseId;

    private AnnouncementManager announcementManager;

    private Realm realm;

    private List<Announcement> announcementList;

    private RealmResults announcementListPromise;

    NewsListPresenter(String courseId) {
        this.courseId = courseId;
        this.announcementManager = new AnnouncementManager();
        this.realm = Realm.getDefaultInstance();
        this.announcementList = new ArrayList<>();
    }

    @Override
    public void onViewAttached(NewsListView view) {
        super.onViewAttached(view);

        if (announcementList == null || announcementList.size() == 0) {
            requestAnnouncements();
        }

        if (courseId == null) {
            this.announcementListPromise = announcementManager.listGlobalAnnouncements(realm, getAnnouncementListRealmChangeLictener());
        } else {
            this.announcementListPromise = announcementManager.listCourseAnnouncements(courseId, realm, getAnnouncementListRealmChangeLictener());
        }
    }

    private RealmChangeListener<RealmResults<Announcement>> getAnnouncementListRealmChangeLictener() {
        return new RealmChangeListener<RealmResults<Announcement>>() {
            @Override
            public void onChange(RealmResults<Announcement> results) {
                announcementList = results;
                getViewOrThrow().showContent();
                getViewOrThrow().showAnnouncementList(announcementList);
            }
        };
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (announcementListPromise != null) {
            announcementListPromise.removeAllChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestAnnouncements();
    }

    public void onAnnouncementClicked(String announcementId) {
        getViewOrThrow().openAnnouncement(announcementId);
    }

    private void requestAnnouncements() {
        if (getView() != null) {
            getView().showProgress();
        }
        if (courseId == null) {
            announcementManager.requestGlobalAnnouncementList(getDefaultJobCallback());
        } else {
            announcementManager.requestCourseAnnouncementList(courseId, getDefaultJobCallback());
        }
    }

}
