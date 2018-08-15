package de.xikolo.presenters.course;

import de.xikolo.managers.CourseManager;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;

public class CertificatesPresenter extends LoadingStatePresenter<CertificatesView> {

    public static final String TAG = CertificatesPresenter.class.getSimpleName();

    private Realm realm;

    private CourseManager courseManager;

    private Course course;

    private String courseId;

    CertificatesPresenter(String courseId) {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
    }

    @Override
    public void onViewAttached(CertificatesView v) {
        super.onViewAttached(v);

        if (course == null)
            requestCourse(false);

        course = Course.find(courseId);

        getViewOrThrow().showContent();
        getViewOrThrow().showCertificates(course);
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestCourse(true);
    }

    private void requestCourse(boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
        courseManager.requestCourse(courseId, getDefaultJobCallback(userRequest));
    }

}
