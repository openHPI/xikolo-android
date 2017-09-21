package de.xikolo.presenters.course;

import de.xikolo.models.Course;
import de.xikolo.presenters.base.LoadingStateView;

public interface CourseDetailsView extends LoadingStateView {

    void setupView(Course course);

    void enterCourse(String courseId);

    void openLogin();

    void showCourseNotAccessibleToast();

    void hideEnrollButton();

}
