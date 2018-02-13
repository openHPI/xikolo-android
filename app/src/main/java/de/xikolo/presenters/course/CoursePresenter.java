package de.xikolo.presenters.course;

import android.net.Uri;

import com.crashlytics.android.Crashlytics;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.RequestJobCallback;
import de.xikolo.managers.CourseManager;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.presenters.base.Presenter;
import de.xikolo.utils.DeepLinkingUtil;
import de.xikolo.utils.LanalyticsUtil;
import io.realm.Realm;

public class CoursePresenter extends Presenter<CourseView> {

    public static final String TAG = CoursePresenter.class.getSimpleName();

    private CourseManager courseManager;

    private Realm realm;

    private int courseTab;
    private int lastTrackedCourseTab;

    private String courseId;

    CoursePresenter() {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.courseTab = Course.TAB_LEARNINGS;

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
            setupCourse(Course.get(courseId));
        }
    }

    public void setCourseTab(int tab) {
        courseTab = tab;
        if (lastTrackedCourseTab != courseTab) {
            switch (tab) {
                case Course.TAB_LEARNINGS:
                    break;
                case Course.TAB_DISCUSSIONS:
                    LanalyticsUtil.trackVisitedPinboard(courseId);
                    break;
                case Course.TAB_PROGRESS:
                    LanalyticsUtil.trackVisitedProgress(courseId);
                    break;
                case Course.TAB_COLLAB_SPACE:
                    LanalyticsUtil.trackVisitedLearningRooms(courseId);
                    break;
                case Course.TAB_COURSE_DETAILS:
                    break;
                case Course.TAB_ANNOUNCEMENTS:
                    LanalyticsUtil.trackVisitedAnnouncements(courseId);
                    break;
                case Course.TAB_RECAP:
                    LanalyticsUtil.trackVisitedRecap(courseId);
                    break;
            }
        }
        lastTrackedCourseTab = courseTab;
    }

    private void setupCourse(Course course) {
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

    public void onShareClicked() {
        if (isViewAttached()) {
            getView().shareLink(Config.HOST_URL + "courses/" + Course.get(courseId).slug);
        }
    }

    public void unenroll(final String courseId) {
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
