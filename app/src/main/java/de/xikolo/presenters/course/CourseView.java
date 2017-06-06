package de.xikolo.presenters.course;

import de.xikolo.models.Course;
import de.xikolo.presenters.base.View;

public interface CourseView extends View {

    void showProgressDialog();

    void hideProgressDialog();

    void showErrorToast();

    void showNoNetworkToast();

    void showCourseLockedToast();

    void showNotEnrolledToast();

    void finishActivity();

    void startCourseDetailsActivity(String courseId);

    void setupView(Course course, Course.Tab courseTab);

}
