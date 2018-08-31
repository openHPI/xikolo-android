package de.xikolo.presenters.course;

import de.xikolo.models.Course;
import de.xikolo.presenters.base.LoadingStateView;

public interface DescriptionView extends LoadingStateView {

    void setupView(Course course);
}
