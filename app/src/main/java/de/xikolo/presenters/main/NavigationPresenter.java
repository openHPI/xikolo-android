package de.xikolo.presenters.main;

import de.xikolo.models.dao.AnnouncementsDao;
import de.xikolo.presenters.base.Presenter;
import io.realm.Realm;

public class NavigationPresenter extends Presenter<NavigationView> {

    private Realm realm;

    private AnnouncementsDao announcementsDao;

    NavigationPresenter() {
        this.realm = Realm.getDefaultInstance();
        this.announcementsDao = new AnnouncementsDao(realm);
    }

    @Override
    public void onViewAttached(NavigationView view) {
        super.onViewAttached(view);

        if (isViewAttached()) {
            announcementsDao.getGlobalAnnouncements();   //ToDo observe
            getView().updateDrawer();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

}
