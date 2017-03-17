package de.xikolo.presenters;

import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.models.Enrollment;
import de.xikolo.models.Profile;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ProfilePresenter implements LoadingStatePresenter<ProfileView> {

    public static final String TAG = ProfilePresenter.class.getSimpleName();

    private ProfileView view;

    private UserManager userManager;
    private CourseManager courseManager;

    private Realm realm;

    private Profile profilePromise;
    private RealmResults enrollmentListPromise;

    ProfilePresenter() {
        this.userManager = new UserManager();
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public void onViewAttached(ProfileView v) {
        this.view = v;

        profilePromise = userManager.getProfile(realm, new RealmChangeListener<Profile>() {
            @Override
            public void onChange(Profile profile) {
                if (view != null) {
                    view.showProfile(profile);
                }
            }
        });

        enrollmentListPromise = courseManager.listEnrollments(realm, new RealmChangeListener<RealmResults<Enrollment>>() {
            @Override
            public void onChange(RealmResults<Enrollment> enrollments) {
                if (view != null) {
                    view.showEnrollmentCount(enrollments.size());
                }
            }
        });
    }

    @Override
    public void onViewDetached() {
        this.view = null;

        if (profilePromise != null) {
            profilePromise.removeChangeListeners();
        }
        if (enrollmentListPromise != null) {
            enrollmentListPromise.removeChangeListeners();
        }
    }

    public void onCreate() {
        requestProfile();
    }

    private void requestProfile() {
        if (profilePromise == null) {
            view.showProgressMessage();
        } else {
            view.showRefreshProgress();
        }
        userManager.requestProfile(new JobCallback() {
            @Override
            public void onSuccess() {
                view.hideAnyProgress();
                requestEnrollmentList();
            }

            @Override
            public void onError(ErrorCode code) {
                switch (code) {
                    case NO_NETWORK:
                        if (profilePromise == null) {
                            view.showNetworkRequiredMessage();
                        } else {
                            view.showNetworkRequiredToast();
                        }
                        break;
                    default:
                        view.showErrorToast();
                        break;
                }
            }
        });
    }

    private void requestEnrollmentList() {
        view.showRefreshProgress();
        courseManager.requestEnrollmentList(new JobCallback() {
            @Override
            public void onSuccess() {
                view.hideAnyProgress();
            }

            @Override
            public void onError(ErrorCode code) {
                view.hideAnyProgress();
            }
        });
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestProfile();
    }

}
