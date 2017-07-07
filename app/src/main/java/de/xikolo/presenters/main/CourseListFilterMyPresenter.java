package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;

public class CourseListFilterMyPresenter extends CourseListPresenter {

    @Override
    public void updateContent() {
        courseList.clear();

        if (!UserManager.isAuthorized()) {
            getViewOrThrow().showLoginRequiredMessage();
        } else if (courseManager.countEnrollments(realm) == 0) {
            getViewOrThrow().showNoEnrollmentsMessage();
        } else {
            List<Course> subList = courseManager.listCurrentAndPastCoursesWithEnrollment(realm);
            if (subList.size() > 0) {
                courseList.add(App.getInstance().getString(R.string.header_my_current_courses),
                        subList);
            }
            subList = courseManager.listFutureCoursesWithEnrollment(realm);
            if (subList.size() > 0) {
                courseList.add(App.getInstance().getString(R.string.header_my_future_courses),
                        subList);
            }
        }

        getViewOrThrow().showCourseList(courseList);
    }

    @Override
    public void requestCourses() {
        if (!UserManager.isAuthorized() && getView() != null) {
            getView().showLoginRequiredMessage();
            getView().hideAnyProgress();
        } else {
            super.requestCourses();
        }
    }

}
