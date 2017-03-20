package de.xikolo.presenters;

import de.xikolo.models.Course;

public interface CourseView {

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
