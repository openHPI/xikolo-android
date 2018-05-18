package de.xikolo.presenters.base;

import java.util.List;

import de.xikolo.App;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;
import de.xikolo.jobs.base.RequestJobCallback;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;
import io.realm.Realm;

import static de.xikolo.jobs.base.RequestJobCallback.ErrorCode.NO_NETWORK;

public abstract class BaseCourseListPresenter<V extends BaseCourseListView> extends LoadingStatePresenter<V> {

    protected Realm realm;

    protected CourseManager courseManager;

    protected SectionList<String, List<Course>> courseList;

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

    public void buildCourseList() {
        List<Course> subList;

        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
            subList = courseManager.listFutureCourses(realm);
            if (subList.size() > 0) {
                courseList.add(
                        App.getInstance().getString(R.string.header_future_courses),
                        subList
                );
            }
            subList = courseManager.listCurrentAndPastCourses(realm);
            if (subList.size() > 0) {
                courseList.add
                        (App.getInstance().getString(R.string.header_self_paced_courses),
                        subList
                );
            }
        } else {
            subList = courseManager.listCurrentAndFutureCourses(realm);
            if (subList.size() > 0) {
                courseList.add(
                        App.getInstance().getString(R.string.header_current_and_upcoming_courses),
                        subList
                );
            }
            subList = courseManager.listPastCourses(realm);
            if (subList.size() > 0) {
                courseList.add(
                        App.getInstance().getString(R.string.header_self_paced_courses),
                        subList
                );
            }
        }
    }

}
