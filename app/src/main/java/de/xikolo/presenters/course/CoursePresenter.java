package de.xikolo.presenters.course;

import android.net.Uri;

import de.xikolo.managers.CourseManager;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.Presenter;
import de.xikolo.utils.DeepLinkingUtil;
import io.realm.Realm;
import io.realm.RealmChangeListener;

public class CoursePresenter implements Presenter<CourseView> {

    public static final String TAG = CoursePresenter.class.getSimpleName();

    private CourseView view;

    private CourseManager courseManager;

    private Realm realm;

    private Course coursePromise;

    private Course.Tab courseTab;

    private String courseId;

    CoursePresenter() {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.courseTab = Course.Tab.RESUME;
    }

    @Override
    public void onViewAttached(CourseView v) {
        this.view = v;

        if (courseId != null) {
            coursePromise = courseManager.getCourse(courseId, realm, new RealmChangeListener<Course>() {
                @Override
                public void onChange(Course course) {
                    if (view != null) {
                        handleCourse(course);
                    }
                }
            });
        }
    }

    @Override
    public void onViewDetached() {
        this.view = null;

        if (coursePromise != null) {
            coursePromise.removeAllChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    public void handleCourse(String id) {
        courseId = id;

        coursePromise = courseManager.getCourse(courseId, realm, new RealmChangeListener<Course>() {
            @Override
            public void onChange(Course course) {
                if (view != null) {
                    handleCourse(course);
                }
            }
        });
    }

    private void handleCourse(Course course) {
        if (!course.accessible) {
            view.showCourseLockedToast();
            view.startCourseDetailsActivity(course.id);
            view.finishActivity();
            return;
        }
        if (!course.isEnrolled()) {
            view.showNotEnrolledToast();
            view.startCourseDetailsActivity(course.id);
            view.finishActivity();
            return;
        }

        view.setupView(course, courseTab);
    }

    public void handleDeepLink(Uri uri) {
        courseId = DeepLinkingUtil.getCourseIdentifierFromResumeUri(uri);
        courseTab = DeepLinkingUtil.getTab(uri.getPath());

        handleCourse(courseId);

        view.showProgressDialog();
        courseManager.requestCourse(courseId, new JobCallback() {
            @Override
            public void onSuccess() {
                view.hideProgressDialog();
            }

            @Override
            public void onError(ErrorCode code) {
                view.hideProgressDialog();
                view.showErrorToast();
                view.finishActivity();
            }
        });
    }

    public void unenroll(final String courseId) {
        view.showProgressDialog();
        courseManager.deleteEnrollment(courseId, new JobCallback() {
            @Override
            public void onSuccess() {
                view.hideProgressDialog();
                view.finishActivity();
            }

            @Override
            public void onError(ErrorCode code) {
                view.hideProgressDialog();
                if (code == ErrorCode.NO_NETWORK) {
                    view.showNoNetworkToast();
                } else {
                    view.showErrorToast();
                }
            }
        });
    }

}
