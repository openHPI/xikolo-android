package de.xikolo.presenters.course;

import de.xikolo.models.Course;
import de.xikolo.presenters.base.View;

public interface CourseView extends View {

    void showProgressDialog();

    void hideProgressDialog();

    void showErrorToast();

    void showNoNetworkToast();

    void showLoginRequiredMessage();

    void openLogin();

    void setEnrollmentFunctionsAvailable(boolean available);

    void hideEnrollBar();

    void showEnrollOption();

    void showCourseStartsSoon();

    void finishActivity();

    void restartActivity();

    void setupView(Course course, int courseTab);

}
