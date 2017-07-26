package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static de.xikolo.jobs.base.JobCallback.ErrorCode.NO_NETWORK;

public abstract class CourseListPresenter extends LoadingStatePresenter<CourseListView> {

    protected CourseManager courseManager;

    protected Realm realm;

    protected SectionList<String, List<Course>> courseList;

    protected RealmResults courseListPromise;

    CourseListPresenter() {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.courseList = new SectionList<>();
    }

    @Override
    public void onViewAttached(CourseListView view) {
        super.onViewAttached(view);

        if (courseList == null || courseList.size() == 0) {
            requestCourses();
        }

        this.courseListPromise = courseManager.listCourses(realm, new RealmChangeListener<RealmResults<Course>>() {
            @Override
            public void onChange(RealmResults<Course> results) {
                getViewOrThrow().showContent();
                updateContent();
            }
        });
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (courseListPromise != null) {
            courseListPromise.removeAllChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestCourses();
    }

    public void onEnrollButtonClicked(String courseId) {
        getViewOrThrow().showBlockingProgress();

        courseManager.createEnrollment(courseId, new JobCallback() {
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
                    if (code == NO_NETWORK) {
                        getView().showNetworkRequiredMessage();
                    } else if (code == ErrorCode.NO_AUTH) {
                        getView().showLoginRequiredMessage();
                        getView().goToProfile();
                    }
                }
            }
        });
    }

    public void onCourseEnterButtonClicked(String courseId) {
        if (!UserManager.isAuthorized()) {
            getViewOrThrow().showLoginRequiredMessage();
            getViewOrThrow().goToProfile();
        } else {
            getViewOrThrow().enterCourse(courseId);
        }
    }

    public void onCourseDetailButtonClicked(String courseId) {
        getViewOrThrow().enterCourseDetails(courseId);
    }

    protected abstract void updateContent();

    public void requestCourses() {
        if (getView() != null) {
            getView().showProgress();
        }
        courseManager.requestCourseList(new JobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgress();
                }
            }

            @Override
            public void onError(ErrorCode code) {
                if (getView() != null) {
                    switch (code) {
                        case NO_NETWORK:
                            getView().showNetworkRequiredMessage();
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

}
