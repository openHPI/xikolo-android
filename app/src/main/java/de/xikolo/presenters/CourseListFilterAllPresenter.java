package de.xikolo.presenters;

import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.models.Course;

public class CourseListFilterAllPresenter extends CourseListPresenter {

    @Override
    protected void updateContent() {
        courseList.clear();
        List<Course> subList;

        subList = courseManager.listCurrentAndFutureCourses(realm, null);
        if (subList.size() > 0) {
            courseList.add(GlobalApplication.getInstance().getString(R.string.header_current_courses),
                    subList);
        }
        subList = courseManager.listPastCourses(realm, null);
        if (subList.size() > 0) {
            courseList.add(GlobalApplication.getInstance().getString(R.string.header_self_paced_courses),
                    subList);
        }

        if (view != null) {
            view.showCourseList(courseList);
        }
    }

}
