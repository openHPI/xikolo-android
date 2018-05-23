package de.xikolo.presenters.course;

import de.xikolo.managers.CourseManager;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmResults;

public class CertificatesPresenter extends LoadingStatePresenter<CertificatesView> {

    public static final String TAG = CertificatesPresenter.class.getSimpleName();

    private Realm realm;

    private CourseManager courseManager;

    private Course course;

    private String courseId;

    private RealmResults<?> enrollmentPromise;


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
        if (course.isEnrolled())
            enrollmentPromise = courseManager.listEnrollments(realm, e -> {
                Enrollment enrollment = e.where().equalTo("id", course.enrollmentId).findFirst();
                if (getView() != null) {
                    getViewOrThrow().showContent();
                    if (enrollment != null)
                        course.certificates.setCertificateUrls(
                                enrollment.confirmationOfParticipationUrl,
                                enrollment.recordOfAchievementUrl,
                                enrollment.qualifiedCertificateUrl);
                    getViewOrThrow().showCertificates(course);
                }
            });
        else {
            getViewOrThrow().showContent();
            getViewOrThrow().showCertificates(course);
        }
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (enrollmentPromise != null) {
            enrollmentPromise.removeAllChangeListeners();
        }
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
