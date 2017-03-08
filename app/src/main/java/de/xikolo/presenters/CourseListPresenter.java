package de.xikolo.presenters;

import java.util.List;

import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.models.Course;
import de.xikolo.utils.HeaderAndSectionsList;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public abstract class CourseListPresenter implements LoadingStatePresenter<CourseListView> {

    protected CourseListView view;

    protected CourseManager courseManager;

    protected Realm realm;

    protected HeaderAndSectionsList<String, List<Course>> courseList;

    protected RealmResults courseListPromise;

    CourseListPresenter() {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public void onViewAttached(CourseListView view) {
        this.view = view;

        this.courseListPromise = courseManager.listCoursesAsync(realm, new RealmChangeListener<RealmResults<Course>>() {
            @Override
            public void onChange(RealmResults<Course> element) {
                updateContent();
            }
        });
    }

    @Override
    public void onViewDetached() {
        this.view = null;

        if (courseListPromise != null) {
            courseListPromise.removeChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    public void onCreate() {
        requestCourses();
    }

    @Override
    public void onRefresh() {
        requestCourses();
    }

    public void onEnrollButtonClicked(String courseId) {
        view.showProgressDialog();

        courseManager.createEnrollment(courseId, new JobCallback() {
            @Override
            public void onSuccess() {
                view.hideAnyProgress();
            }

            @Override
            public void onError(ErrorCode code) {
                view.hideAnyProgress();
                if (code == ErrorCode.NO_NETWORK) {
                    view.showNetworkRequiredToast();
                } else if (code == ErrorCode.NO_AUTH) {
                    view.showLoginRequiredToast();
                    view.goToProfile();
                }
            }
        });
    }

    public void onCourseEnterButtonClicked(String courseId) {
        if (!UserManager.isLoggedIn()) {
            view.showLoginRequiredToast();
            view.goToProfile();
        } else {
            view.enterCourse(courseId);
        }
    }

    public void onCourseDetailButtonClicked(String courseId) {
        view.enterCourseDetails(courseId);
    }

    protected abstract void updateContent();

    public void requestCourses() {
        if (courseList == null || courseList.size() == 0) {
            view.showProgressMessage();
        } else {
            view.showRefreshProgress();
        }
        courseManager.requestCourses(new JobCallback() {
            @Override
            public void onSuccess() {
                view.hideAnyProgress();
            }

            @Override
            public void onError(ErrorCode code) {
                switch (code) {
                    case NO_NETWORK:
                        if (courseList == null || courseList.size() == 0) {
                            view.showNetworkRequiredMessage();
                        } else {
                            view.showNetworkRequiredToast();
                        }
                        break;
                    case CANCEL:
                    case ERROR:
                        view.showErrorToast();
                        break;
                }
            }
        });
    }

}
