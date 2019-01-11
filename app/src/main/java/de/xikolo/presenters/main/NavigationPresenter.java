package de.xikolo.presenters.main;

import androidx.lifecycle.LifecycleOwner;

import de.xikolo.models.dao.AnnouncementsDao;
import de.xikolo.presenters.base.Presenter;
import io.realm.Realm;

public class NavigationPresenter extends Presenter<NavigationView> {

    private Realm realm;

    private AnnouncementsDao announcementsDao;

    private LifecycleOwner lifecycleOwner;

    NavigationPresenter(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
        this.realm = Realm.getDefaultInstance();
        this.announcementsDao = new AnnouncementsDao(realm);
    }

    @Override
    public void onViewAttached(NavigationView view) {
        super.onViewAttached(view);

        announcementsDao.getGlobalAnnouncements().observe(lifecycleOwner, announcements -> {
            if (isViewAttached()) {
                getView().updateDrawer();
            }
        });
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

}
