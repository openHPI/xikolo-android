package de.xikolo.presenters.main;

import androidx.lifecycle.LifecycleOwner;
import de.xikolo.models.dao.AnnouncementDao;
import de.xikolo.presenters.base.Presenter;
import io.realm.Realm;

public class NavigationPresenter extends Presenter<NavigationView> {

    private Realm realm;

    private AnnouncementDao announcementDao;

    private LifecycleOwner lifecycleOwner;

    NavigationPresenter(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
        this.realm = Realm.getDefaultInstance();
        this.announcementDao = new AnnouncementDao(realm);
    }

    @Override
    public void onViewAttached(NavigationView view) {
        super.onViewAttached(view);

        announcementDao.all().observe(lifecycleOwner, announcements -> {
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
