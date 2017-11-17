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
            requestUser(false);
        }

        userPromise = userManager.getUser(realm, new RealmChangeListener<User>() {
            @Override
            public void onChange(User u) {
                user = u;
                profile = Profile.get(user.profileId);

                getViewOrThrow().showContent();
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

    private void requestUser(final boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
        userManager.requestUserWithProfile(new JobCallback() {
            @Override
            public void onSuccess() {
                requestEnrollmentList(userRequest);
            }

            @Override
            public void onError(ErrorCode code) {
                if (getView() != null) {
                    getView().hideProgress();
                    switch (code) {
                        case NO_NETWORK:
                            if (userRequest || !getView().isContentViewVisible()) getView().showNetworkRequiredMessage();
                            break;
                        case CANCEL:
                        case ERROR:
                            getView().showErrorMessage();
                            break;
                    }
                }
            }
        });
    }

    private void requestEnrollmentList(final boolean userRequest) {
        courseManager.requestEnrollmentList(new JobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgress();
                }
            }

            @Override
            public void onError(ErrorCode code) {
                if (getView() != null) {
                    getView().hideProgress();
                    switch (code) {
                        case NO_NETWORK:
                            if (userRequest || !getView().isContentViewVisible()) getView().showNetworkRequiredMessage();
                            break;
                        case CANCEL:
                        case ERROR:
                            getView().showErrorMessage();
                            break;
                    }
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
        requestUser(true);
    }

}
