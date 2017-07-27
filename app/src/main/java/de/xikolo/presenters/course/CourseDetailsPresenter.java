package de.xikolo.presenters.course;

import de.xikolo.managers.CourseManager;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.presenters.base.Presenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;

public class CourseDetailsPresenter extends Presenter<CourseDetailsView> {

    public static final String TAG = CourseDetailsPresenter.class.getSimpleName();

    private CourseManager courseManager;

    private Realm realm;

    private Course coursePromise;

    private String courseId;

    CourseDetailsPresenter(String courseId) {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
    }

    @Override
    public void onViewAttached(CourseDetailsView v) {
        super.onViewAttached(v);

        coursePromise = courseManager.getCourse(courseId, realm, new RealmChangeListener<Course>() {
            @Override
            public void onChange(Course course) {
                getViewOrThrow().setupView(course);
            }
        });
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

    public void unenroll(final String courseId) {
        getViewOrThrow().showProgressDialog();
        courseManager.deleteEnrollment(Enrollment.getForCourse(courseId).id, new JobCallback() {
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
