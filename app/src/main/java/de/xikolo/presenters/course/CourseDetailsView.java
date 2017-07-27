package de.xikolo.presenters.course;

import de.xikolo.models.Course;
import de.xikolo.presenters.base.View;

public interface CourseDetailsView extends View {

    void showProgressDialog();

    void hideProgressDialog();

    void showErrorToast();

    void showNoNetworkToast();

    void setupView(Course course);

    void finishActivity();

}
