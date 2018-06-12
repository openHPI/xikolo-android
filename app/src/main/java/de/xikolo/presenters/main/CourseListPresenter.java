package de.xikolo.presenters.main;

import de.xikolo.managers.CourseManager;
import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;
import de.xikolo.presenters.base.BaseCourseListPresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public abstract class CourseListPresenter extends BaseCourseListPresenter<CourseListView> {

    protected RealmResults courseListPromise;

    CourseListPresenter() {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.courseList = new SectionList<>();
    }

    @Override
    public void onViewAttached(CourseListView view) {
        super.onViewAttached(view);

        if (courseList == null || courseList.size() == 0) {
            requestCourses(false);
        }

        setCourseListPromise();
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

    public void onSearch(final String query, boolean withEnrollment) {
        courseListPromise.removeAllChangeListeners();
        if (query != null && !"".equals(query)) {
            this.courseListPromise = courseManager.searchCourses(query, withEnrollment, realm, results -> {
                courseList.clear();
                courseList.add(null, results);
                if (isViewAttached()) getView().showCourseList(courseList);
            });
        } else {
            setCourseListPromise();
        }
    }

    protected abstract void setCourseListPromise();

    protected RealmChangeListener<RealmResults<Course>> getCourseListChangeListener() {
        return results -> {
            if (isViewAttached()) {
                getView().showContent();
                updateContent();
            }
        };
    }

    protected abstract void updateContent();

    public void requestCourses(boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
        courseManager.requestCourseList(getDefaultJobCallback(userRequest));
    }

}
