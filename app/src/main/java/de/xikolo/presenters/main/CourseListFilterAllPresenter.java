package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.models.Course;

public class CourseListFilterAllPresenter extends CourseListPresenter {

    @Override
    protected void updateContent() {
        courseList.clear();
        List<Course> subList;

        subList = courseManager.listCurrentAndFutureCourses(realm);
        if (subList.size() > 0) {
            courseList.add(App.getInstance().getString(R.string.header_current_courses),
                    subList);
        }
        subList = courseManager.listPastCourses(realm);
        if (subList.size() > 0) {
            courseList.add(App.getInstance().getString(R.string.header_self_paced_courses),
                    subList);
        }

        getViewOrThrow().showCourseList(courseList);
    }

}
