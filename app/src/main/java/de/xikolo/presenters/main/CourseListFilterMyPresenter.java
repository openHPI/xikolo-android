package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;

public class CourseListFilterMyPresenter extends CourseListPresenter {

    @Override
    public void updateContent() {
        if (!UserManager.isAuthorized()) {
            view.showLoginRequiredMessage();
        } else if (courseList == null || courseList.size() == 0) {
            view.showNoEnrollmentsMessage();
        } else {
            courseList.clear();
            List<Course> subList;

            subList = courseManager.listCurrentAndPastCoursesWithEnrollment(realm);
            if (subList.size() > 0) {
                courseList.add(GlobalApplication.getInstance().getString(R.string.header_my_current_courses),
                        subList);
            }
            subList = courseManager.listFutureCoursesWithEnrollment(realm);
            if (subList.size() > 0) {
                courseList.add(GlobalApplication.getInstance().getString(R.string.header_my_future_courses),
                        subList);
            }
        }

        if (view != null) {
            view.showCourseList(courseList);
        }
    }

    @Override
    public void requestCourses() {
        if (!UserManager.isAuthorized()) {
            view.showLoginRequiredMessage();
            view.hideAnyProgress();
        } else {
            super.requestCourses();
        }
    }

}
