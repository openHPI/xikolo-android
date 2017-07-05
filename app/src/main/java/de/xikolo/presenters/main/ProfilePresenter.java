package de.xikolo.presenters.main;

import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Enrollment;
import de.xikolo.models.Profile;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class ProfilePresenter extends LoadingStatePresenter<ProfileView> {

    public static final String TAG = ProfilePresenter.class.getSimpleName();

    private UserManager userManager;
    private CourseManager courseManager;

    private Realm realm;

    private RealmObject profilePromise;
    private RealmResults enrollmentListPromise;

    private Profile profile;

    ProfilePresenter() {
        this.userManager = new UserManager();
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public void onViewAttached(ProfileView v) {
        super.onViewAttached(v);

        if (profile == null) {
            requestProfile();
        }

        profilePromise = userManager.getProfile(realm, new RealmChangeListener<Profile>() {
            @Override
            public void onChange(Profile p) {
                profile = p;
                getViewOrThrow().showProfile(p);
            }
        });

        enrollmentListPromise = courseManager.listEnrollments(realm, new RealmChangeListener<RealmResults<Enrollment>>() {
            @Override
            public void onChange(RealmResults<Enrollment> enrollments) {
                getViewOrThrow().showEnrollmentCount(enrollments.size());
            }
        });
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (profilePromise != null) {
            profilePromise.removeAllChangeListeners();
        }
        if (enrollmentListPromise != null) {
            enrollmentListPromise.removeAllChangeListeners();
        }
    }

    private void requestProfile() {
        if (getView() != null) {
            if (profilePromise == null) {
                getView().showProgressMessage();
            } else {
                getView().showRefreshProgress();
            }
        }
        userManager.requestProfile(new JobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideAnyProgress();
                }
                requestEnrollmentList();
            }

            @Override
            public void onError(ErrorCode code) {
                if (getView() != null) {
                    switch (code) {
                        case NO_NETWORK:
                            if (profilePromise == null) {
                                getView().showNetworkRequiredMessage();
                            } else {
                                getView().showNetworkRequiredToast();
                            }
                            break;
                        default:
                            getView().showErrorToast();
                            break;
                    }
                }
            }
        });
    }

    private void requestEnrollmentList() {
        if (getView() != null) {
            getView().showRefreshProgress();
        }
        courseManager.requestEnrollmentList(new JobCallback() {
            @Override
            public void onSuccess() {
                getView().hideAnyProgress();
            }

            @Override
            public void onError(ErrorCode code) {
                getView().hideAnyProgress();
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
