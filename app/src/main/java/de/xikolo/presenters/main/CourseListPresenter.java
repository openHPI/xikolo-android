package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.jobs.base.RequestJobCallback;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static de.xikolo.jobs.base.RequestJobCallback.ErrorCode.NO_NETWORK;

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
            requestCourses(false);
        }

        setCourseListPromise();
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
        requestCourses(true);
    }

    public void onSearch(final String query, boolean withEnrollment) {
        courseListPromise.removeAllChangeListeners();
        if (query != null && !"".equals(query)) {
            this.courseListPromise = courseManager.searchCourses(query, withEnrollment, realm, new RealmChangeListener<RealmResults<Course>>() {
                @Override
                public void onChange(RealmResults<Course> results) {
                    courseList.clear();
                    courseList.add(null, results);
                    getViewOrThrow().showCourseList(courseList);
                }
            });
        } else {
            setCourseListPromise();
        }
    }

    protected abstract void setCourseListPromise();

    protected RealmChangeListener<RealmResults<Course>> getCourseListChangeListener() {
        return new RealmChangeListener<RealmResults<Course>>() {
            @Override
            public void onChange(RealmResults<Course> results) {
                if (results.size() > 0) {
                    getViewOrThrow().showContent();
                    updateContent();
                }
            }
        };
    }

    public void onEnrollButtonClicked(final String courseId) {
        getViewOrThrow().showBlockingProgress();

        courseManager.createEnrollment(courseId, new RequestJobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgress();
                    Course course = Course.get(courseId);
                    if (course.accessible) {
                        getView().enterCourse(courseId);
                    }
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
                        getView().openLogin();
                    }
                }
            }
        });
    }

    public void onCourseEnterButtonClicked(String courseId) {
        if (!UserManager.isAuthorized()) {
            getViewOrThrow().showLoginRequiredMessage();
            getViewOrThrow().openLogin();
        } else {
            getViewOrThrow().enterCourse(courseId);
        }
    }

    public void onCourseDetailButtonClicked(String courseId) {
        getViewOrThrow().enterCourseDetails(courseId);
    }

    protected abstract void updateContent();

    public void requestCourses(boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
        courseManager.requestCourseList(getDefaultJobCallback(userRequest));
    }

}
