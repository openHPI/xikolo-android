package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.App;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;
import de.xikolo.models.Course;

public class CourseListFilterAllPresenter extends CourseListPresenter {

    @Override
    protected void updateContent() {
        courseList.clear();
        List<Course> subList;

        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
            subList = courseManager.listCurrentAndPastCourses(realm);
            if (subList.size() > 0) {
                courseList.add(App.getInstance().getString(R.string.header_current_courses),
                        subList);
            }
            subList = courseManager.listFutureCourses(realm);
            if (subList.size() > 0) {
                courseList.add(App.getInstance().getString(R.string.header_future_courses),
                        subList);
            }
        } else {
            subList = courseManager.listCurrentAndFutureCourses(realm);
            if (subList.size() > 0) {
                courseList.add(App.getInstance().getString(R.string.header_current_and_upcoming_courses),
                        subList);
            }
            subList = courseManager.listPastCourses(realm);
            if (subList.size() > 0) {
                courseList.add(App.getInstance().getString(R.string.header_self_paced_courses),
                        subList);
            }
        }

        getViewOrThrow().showCourseList(courseList);
    }

    @Override
    protected void setCourseListPromise() {
        this.courseListPromise = courseManager.listCourses(realm, getCourseListChangeListener());
    }

}
