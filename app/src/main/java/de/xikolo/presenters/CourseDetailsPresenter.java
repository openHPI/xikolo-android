package de.xikolo.presenters;

import de.xikolo.managers.CourseManager;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.models.Course;
import io.realm.Realm;
import io.realm.RealmChangeListener;

public class CourseDetailsPresenter implements Presenter<CourseDetailsView> {

    public static final String TAG = CourseDetailsPresenter.class.getSimpleName();

    private CourseDetailsView view;

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
        this.view = v;

        coursePromise = courseManager.getCourse(courseId, realm, new RealmChangeListener<Course>() {
            @Override
            public void onChange(Course course) {
                if (view != null) {
                    view.setupView(course);
                }
            }
        });
    }

    @Override
    public void onViewDetached() {
        this.view = null;

        if (coursePromise != null) {
            coursePromise.removeChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    public void unenroll(final String courseId) {
        view.showProgressDialog();
        courseManager.deleteEnrollment(courseId, new JobCallback() {
            @Override
            public void onSuccess() {
                view.hideProgressDialog();
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
