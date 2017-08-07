package de.xikolo.presenters.announcement;

import de.xikolo.managers.AnnouncementManager;
import de.xikolo.models.Announcement;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;

public class AnnouncementPresenter extends LoadingStatePresenter<AnnouncementView> {

    public static final String TAG = AnnouncementPresenter.class.getSimpleName();

    private AnnouncementManager announcementManager;

    private Realm realm;

    private String announcementId;

    private Announcement announcement;

    AnnouncementPresenter(String announcementId) {
        this.announcementManager = new AnnouncementManager();
        this.realm = Realm.getDefaultInstance();
        this.announcementId = announcementId;

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
    }

    private void loadModels() {
        if (announcement == null) {
            announcement = Announcement.get(announcementId);
        }
    }

}
