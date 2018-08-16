package de.xikolo.presenters.main;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;

public class CertificateListPresenter extends LoadingStatePresenter<CertificateListView> {

    protected CourseManager courseManager;

    protected Realm realm;

    protected List<Course> courseList;

    CertificateListPresenter() {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.courseList = new ArrayList<>();
    }

    @Override
    public void onViewAttached(CertificateListView view) {
        super.onViewAttached(view);

        if (courseList == null || courseList.size() == 0) {
            requestCourses(false);
        }

        courseManager.listCourses(realm, courses -> {
            courseList = realm.copyFromRealm(courses);
            for (int i = 0; i < courseList.size(); i++) {
                Enrollment e = Enrollment.getForCourse(courseList.get(i).id);
                if (e != null)
                    if (e.confirmationOfParticipationUrl != null
                        || e.recordOfAchievementUrl != null
                        || e.qualifiedCertificateUrl != null)
                        continue;
                courseList.remove(i);
                i--;
            }
            getViewOrThrow().showContent();
            if (!UserManager.isAuthorized()) {
                getViewOrThrow().showLoginRequiredMessage();
            } else {
                if (courseList.size() == 0)
                    getViewOrThrow().showNoCertificatesMessage();
                else
                    getViewOrThrow().showCertificateList(courseList);
            }
        });
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestCourses(true);
    }

    public void requestCourses(boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
        courseManager.requestCourseList(getDefaultJobCallback(userRequest));
    }

}