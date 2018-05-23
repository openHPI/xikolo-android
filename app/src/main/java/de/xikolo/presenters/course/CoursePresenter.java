package de.xikolo.presenters.course;

import android.net.Uri;

import com.crashlytics.android.Crashlytics;

import de.xikolo.controllers.helper.CourseArea;
import de.xikolo.jobs.base.RequestJobCallback;
import de.xikolo.managers.CourseManager;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.presenters.base.Presenter;
import de.xikolo.utils.DeepLinkingUtil;
import de.xikolo.utils.LanalyticsUtil;
import io.realm.Realm;

import static de.xikolo.jobs.base.RequestJobCallback.ErrorCode.NO_NETWORK;

public class CoursePresenter extends Presenter<CourseView> {

    public static final String TAG = CoursePresenter.class.getSimpleName();

    private CourseManager courseManager;

    private Realm realm;

    private int courseTab;
    private int lastTrackedCourseTab;

    private String courseId;

    private Course course;

    CoursePresenter() {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.courseTab = CourseArea.LEARNINGS.getIndex();

        lastTrackedCourseTab = -1;
    }

    @Override
    public void onViewAttached(CourseView v) {
        super.onViewAttached(v);

        if (courseId != null) {
            initCourse(courseId);
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    public void initCourse(String id) {
        initCourse(id, courseTab);
    }

    private void initCourse(String id, int tab) {
        courseId = id;
        courseTab = tab;

        Crashlytics.setString("course_id", id);

        if (isViewAttached()) {
            course = Course.get(courseId);
            setupCourse(course);
        }
    }

    public void setCourseTab(int tab) {
        courseTab = tab;
        if (lastTrackedCourseTab != courseTab) {
            switch (CourseArea.get(tab)) {
                case DISCUSSIONS:
                    LanalyticsUtil.trackVisitedPinboard(courseId);
                    break;
                case PROGRESS:
                    LanalyticsUtil.trackVisitedProgress(courseId);
                    break;
                case ANNOUNCEMENTS:
                    LanalyticsUtil.trackVisitedAnnouncements(courseId);
                    break;
                case RECAP:
                    LanalyticsUtil.trackVisitedRecap(courseId);
                    break;
            }
        }
        lastTrackedCourseTab = courseTab;
    }

    private void setupCourse(Course course) {
        getViewOrThrow().setupView(course, courseTab);

        if (!course.isEnrolled()) {
            getViewOrThrow().setEnrollmentFunctionsAvailable(false);
            getViewOrThrow().showEnrollOption();
        } else if (course.accessible) {
            getViewOrThrow().setEnrollmentFunctionsAvailable(true);
            getViewOrThrow().hideEnrollBar();
        } else {
            getViewOrThrow().setEnrollmentFunctionsAvailable(false);
            getViewOrThrow().showCourseStartsSoon();
        }
    }

    public void handleDeepLink(Uri uri) {
        String identifier = DeepLinkingUtil.getCourseIdentifierFromResumeUri(uri);
        int tab = DeepLinkingUtil.getTab(uri.getPath());

        Crashlytics.setString("deep_link", uri.toString());

        if (getView() != null) {
            getView().showProgressDialog();
        }
        courseManager.requestCourse(identifier, new RequestJobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgressDialog();
                    initCourse(Course.find(identifier).id, tab);
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

    public void enroll() {
        getViewOrThrow().showProgressDialog();

        courseManager.createEnrollment(courseId, new RequestJobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgressDialog();
                    getViewOrThrow().restartActivity();
                }
            }

            @Override
            public void onError(ErrorCode code) {
                if (getView() != null) {
                    getView().hideProgressDialog();
                    if (code == NO_NETWORK) {
                        getView().showNoNetworkToast();
                    } else if (code == ErrorCode.NO_AUTH) {
                        getView().showLoginRequiredMessage();
                        getView().openLogin();
                    }
                }
            }
        });
    }

    public void unenroll() {
        getViewOrThrow().showProgressDialog();
        courseManager.deleteEnrollment(Enrollment.getForCourse(courseId).id, new RequestJobCallback() {
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
