package de.xikolo.presenters.main;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Certificates;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CertificateListPresenter extends LoadingStatePresenter<CertificateListView> {

    protected CourseManager courseManager;

    protected Realm realm;

    protected List<Course> courseList;

    protected RealmResults courseListPromise;

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

        this.courseListPromise = courseManager.listCourses(realm, getCourseListRealmChangeListener());
    }

    private RealmChangeListener<RealmResults<Course>> getCourseListRealmChangeListener() {
        return results -> {
            getViewOrThrow().showContent();
            if (!UserManager.isAuthorized()) {
                getViewOrThrow().showLoginRequiredMessage();
            } else {
                if (results.size() > 0) {
                    courseList = realm.copyFromRealm(results);
                    for (int i = 0; i < courseList.size(); ) {
                        Certificates c = courseList.get(i).certificates;
                        if (c.qualifiedCertificateUrl == null
                                && c.recordOfAchievementUrl == null
                                && c.confirmationOfParticipationUrl == null)
                            courseList.remove(i); //ToDo change
                        else
                            i++;
                    }

                    if (courseList.size() == 0)
                        getViewOrThrow().showNoCertificatesMessage();
                    else
                        getViewOrThrow().showCertificateList(courseList);
                }
            }
        };
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (courseListPromise != null) {
            courseListPromise.removeAllChangeListeners();
        }
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