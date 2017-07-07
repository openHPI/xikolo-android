package de.xikolo.presenters.main;

import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Enrollment;
import de.xikolo.models.Profile;
import de.xikolo.models.User;
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

    private RealmObject userPromise;
    private RealmResults enrollmentListPromise;

    private User user;
    private Profile profile;

    ProfilePresenter() {
        this.userManager = new UserManager();
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public void onViewAttached(ProfileView v) {
        super.onViewAttached(v);

        if (user == null) {
            requestUser();
        }

        userPromise = userManager.getUser(realm, new RealmChangeListener<User>() {
            @Override
            public void onChange(User u) {
                user = u;
                profile = Profile.get(user.profileId);

                getViewOrThrow().showUser(user, profile);
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

        if (userPromise != null) {
            userPromise.removeAllChangeListeners();
        }
        if (enrollmentListPromise != null) {
            enrollmentListPromise.removeAllChangeListeners();
        }
    }

    private void requestUser() {
        if (getView() != null) {
            getView().showRefreshProgress();
        }
        userManager.requestUserWithProfile(new JobCallback() {
            @Override
            public void onSuccess() {
                requestEnrollmentList();
            }

            @Override
            public void onError(ErrorCode code) {
                if (getView() != null) {
                    getView().hideAnyProgress();
                    switch (code) {
                        case NO_NETWORK:
                            if (userPromise == null) {
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
        courseManager.requestEnrollmentList(new JobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideAnyProgress();
                }
            }

            @Override
            public void onError(ErrorCode code) {
                if (getView() != null) {
                    getView().hideAnyProgress();
                }
            }
        });
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestUser();
    }

}
