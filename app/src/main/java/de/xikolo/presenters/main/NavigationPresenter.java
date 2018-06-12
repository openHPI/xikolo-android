package de.xikolo.presenters.main;

import de.xikolo.managers.AnnouncementManager;
import de.xikolo.presenters.base.Presenter;
import io.realm.Realm;
import io.realm.RealmResults;

public class NavigationPresenter extends Presenter<NavigationView> {

    private AnnouncementManager announcementManager;

    private Realm realm;

    private RealmResults announcementListPromise;

    NavigationPresenter() {
        this.announcementManager = new AnnouncementManager();
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public void onViewAttached(NavigationView view) {
        super.onViewAttached(view);

        this.announcementListPromise = announcementManager.listGlobalAnnouncements(realm, (announcements) -> {
            if (isViewAttached()) getView().updateDrawer();
        });
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

}
