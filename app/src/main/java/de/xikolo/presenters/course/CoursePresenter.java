package de.xikolo.presenters.course;

import android.net.Uri;

import de.xikolo.managers.CourseManager;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.Presenter;
import de.xikolo.utils.DeepLinkingUtil;
import io.realm.Realm;
import io.realm.RealmChangeListener;

public class CoursePresenter extends Presenter<CourseView> {

    public static final String TAG = CoursePresenter.class.getSimpleName();

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
        super.onViewAttached(v);

        if (courseId != null) {
            coursePromise = courseManager.getCourse(courseId, realm, new RealmChangeListener<Course>() {
                @Override
                public void onChange(Course course) {
                    handleCourse(course);
                }
            });
        }
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

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
                handleCourse(course);
            }
        });
    }

    private void handleCourse(Course course) {
        if (!course.accessible) {
            getViewOrThrow().showCourseLockedToast();
            getViewOrThrow().startCourseDetailsActivity(course.id);
            getViewOrThrow().finishActivity();
            return;
        }
        if (!course.isEnrolled()) {
            getViewOrThrow().showNotEnrolledToast();
            getViewOrThrow().startCourseDetailsActivity(course.id);
            getViewOrThrow().finishActivity();
            return;
        }

        getViewOrThrow().setupView(course, courseTab);
    }

    public void handleDeepLink(Uri uri) {
        courseId = DeepLinkingUtil.getCourseIdentifierFromResumeUri(uri);
        courseTab = DeepLinkingUtil.getTab(uri.getPath());

        handleCourse(courseId);

        getViewOrThrow().showProgressDialog();
        courseManager.requestCourse(courseId, new JobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgressDialog();
                }
            }

            @Override
            public void onError(ErrorCode code) {
                if (getView() != null) {
                    getView().hideProgressDialog();
                    getView().showErrorToast();
                    getView().finishActivity();
                }
            }
        });
    }

    public void unenroll(final String courseId) {
        getViewOrThrow().showProgressDialog();
        courseManager.deleteEnrollment(courseId, new JobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgressDialog();
                    getView().finishActivity();
                }
            }

            @Override
            public void onError(ErrorCode code) {
                if (getView() != null) {
                    getView().hideProgressDialog();
                    if (code == ErrorCode.NO_NETWORK) {
                        getView().showNoNetworkToast();
                    } else {
                        getView().showErrorToast();
                    }
                }
            }
        });
    }

}
