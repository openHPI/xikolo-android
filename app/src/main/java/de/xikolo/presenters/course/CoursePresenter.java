package de.xikolo.presenters.course;

import android.arch.lifecycle.LifecycleOwner;
import android.net.Uri;

import com.crashlytics.android.Crashlytics;

import androidx.annotation.NonNull;
import de.xikolo.controllers.helper.CourseArea;
import de.xikolo.managers.CourseManager;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.network.jobs.base.RequestJobCallback;
import de.xikolo.presenters.base.Presenter;
import de.xikolo.utils.DeepLinkingUtil;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.viewmodels.CourseViewModel;
import de.xikolo.viewmodels.base.NetworkCode;
import io.realm.Realm;

public class CoursePresenter extends Presenter<CourseView> {

    public static final String TAG = CoursePresenter.class.getSimpleName();

    private CourseManager courseManager;

    private Realm realm;

    private Course course;

    private CourseArea courseTab = CourseArea.LEARNINGS;

    private CourseArea lastTrackedCourseTab;

    private LifecycleOwner lifecycleOwner;

    CoursePresenter(LifecycleOwner lifecycleOwner) {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public void onViewAttached(CourseView v) {
        super.onViewAttached(v);

        if (course != null) {
            initCourse(course.id);
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    public void initCourse(String id) {
        initCourse(id, courseTab);
    }

    private void initCourse(String id, CourseArea tab) {
        courseTab = tab;

        Crashlytics.setString("course_id", id);

        if (isViewAttached()) {
            course = Course.get(id);//
            setupCourse(course);
        }
    }

    public void setCourseTab(CourseArea tab) {
        courseTab = tab;
        if (lastTrackedCourseTab != courseTab) {
            switch (tab) {
                case DISCUSSIONS:
                    LanalyticsUtil.trackVisitedPinboard(course.id);
                    break;
                case PROGRESS:
                    LanalyticsUtil.trackVisitedProgress(course.id);
                    break;
                case ANNOUNCEMENTS:
                    LanalyticsUtil.trackVisitedAnnouncements(course.id);
                    break;
                case RECAP:
                    LanalyticsUtil.trackVisitedRecap(course.id);
                    break;
            }
        }
        lastTrackedCourseTab = courseTab;
    }

    private void setupCourse(Course course) {
        getViewOrThrow().setupView(course, courseTab);

        if (!course.isEnrolled()) {
            getViewOrThrow().setAreaState(CourseArea.Locked.INSTANCE);
            getViewOrThrow().showEnrollBar();
        } else if (course.accessible) {
            getViewOrThrow().setAreaState(CourseArea.All.INSTANCE);
            getViewOrThrow().hideEnrollBar();
        } else {
            getViewOrThrow().setAreaState(CourseArea.Locked.INSTANCE);
            getViewOrThrow().showCourseUnavailableEnrollBar();
        }
    }

    public void handleDeepLink(Uri uri) {
        String identifier = DeepLinkingUtil.getCourseIdentifierFromResumeUri(uri);
        CourseArea tab = DeepLinkingUtil.getTab(uri.getPath());

        Crashlytics.setString("deep_link", uri.toString());

        if (getView() != null) {
            getView().showProgressDialog();
        }

        // ToDo Refactor this with architecture change. This is a workaround for new ViewModel architecture because CourseManager.requestCourse() does not exist anymore.
        CourseViewModel viewModel = new CourseViewModel(identifier);
        viewModel.getNetworkState().observe(lifecycleOwner, networkState -> {
            if (networkState != null && networkState.getCode() == NetworkCode.SUCCESS) {
                viewModel.getNetworkState().removeObservers(lifecycleOwner);
                if (getView() != null) {
                    getView().hideProgressDialog();
                    initCourse(Course.find(identifier).id, tab);
                }
            } else if (networkState != null && networkState.getCode() != NetworkCode.STARTED) {
                viewModel.getNetworkState().removeObservers(lifecycleOwner);
                if (getView() != null) {
                    getView().hideProgressDialog();
                    getView().showErrorToast();
                    getView().finishActivity();
                }
            }
        });
        viewModel.requestCourse(false);
    }

    public void enroll() {
        getViewOrThrow().showProgressDialog();

        courseManager.createEnrollment(course.id, new RequestJobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgressDialog();
                    getView().restartActivity();
                }
            }

            @Override
            public void onError(@NonNull ErrorCode code) {
                if (getView() != null) {
                    getView().hideProgressDialog();
                    if (code == ErrorCode.NO_NETWORK) {
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
        courseManager.deleteEnrollment(Enrollment.getForCourse(course.id).id, new RequestJobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgressDialog();
                    getView().finishActivity();
                }
            }

            @Override
            public void onError(@NonNull ErrorCode code) {
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
