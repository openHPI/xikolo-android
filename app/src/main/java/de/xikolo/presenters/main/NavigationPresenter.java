package de.xikolo.presenters.main;

import de.xikolo.managers.AnnouncementManager;
import de.xikolo.models.Announcement;
import de.xikolo.presenters.base.Presenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
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

        this.announcementListPromise = announcementManager.listGlobalAnnouncements(realm, new RealmChangeListener<RealmResults<Announcement>>() {
            @Override
            public void onChange(RealmResults<Announcement> results) {
                getViewOrThrow().updateDrawer();
            }
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
