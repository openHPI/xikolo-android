package de.xikolo.presenters.base;

import java.util.List;

import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.network.jobs.base.RequestJobCallback;
import de.xikolo.utils.SectionList;
import io.realm.Realm;

import static de.xikolo.network.jobs.base.RequestJobCallback.ErrorCode.NO_NETWORK;

// ToDo Remove this with architecture change. This has code duplications with CourseListFragment.
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
        // ToDo Remove this with architecture change. This is being overridden in Channels.
    }

}
