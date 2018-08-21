package de.xikolo.presenters.course;

import de.xikolo.controllers.helper.CourseArea;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.View;

public interface CourseView extends View {

    void showProgressDialog();

    void hideProgressDialog();

    void showErrorToast();

    void showNoNetworkToast();

    void showLoginRequiredMessage();

    void openLogin();

    void setAreaState(CourseArea.State state);

    void showEnrollBar();

    void hideEnrollBar();

    void showCourseUnavailableEnrollBar();

    void restartActivity();

    void finishActivity();

    void setupView(Course course, CourseArea courseTab);

}
