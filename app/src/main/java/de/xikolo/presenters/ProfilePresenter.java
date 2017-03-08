package de.xikolo.presenters;

import android.util.Log;

import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Enrollment;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ProfilePresenter implements LoadingStatePresenter<ProfileView> {

    public static final String TAG = ProfilePresenter.class.getSimpleName();

    private ProfileView view;

    private UserManager userManager;

    private CourseManager courseManager;

    private Realm realm;

    private RealmResults enrollmentListPromise;

    public ProfilePresenter() {
        this.userManager = new UserManager();
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public void onViewAttached(ProfileView view) {
        this.view = view;

        enrollmentListPromise = courseManager.listEnrollmentsAsync(realm, new RealmChangeListener<RealmResults<Enrollment>>() {
            @Override
            public void onChange(RealmResults<Enrollment> enrollments) {
                view.showEnrollmentCount(enrollments.size());
            }
        });
    }

    @Override
    public void onViewDetached() {
        this.view = null;

        enrollmentListPromise.removeChangeListeners();
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {

    }

}
