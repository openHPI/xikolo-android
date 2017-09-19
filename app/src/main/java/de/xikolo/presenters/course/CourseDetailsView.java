package de.xikolo.presenters.course;

import de.xikolo.models.Course;
import de.xikolo.presenters.base.LoadingStateView;

public interface CourseDetailsView extends LoadingStateView {

    void showErrorToast();

    void showNoNetworkToast();

    void setupView(Course course);

    void finishActivity();

}
