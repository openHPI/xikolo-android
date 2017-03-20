package de.xikolo.presenters;

import de.xikolo.models.Course;

public interface CourseDetailsView {

    void showProgressDialog();

    void hideProgressDialog();

    void showErrorToast();

    void showNoNetworkToast();

    void setupView(Course course);

}
