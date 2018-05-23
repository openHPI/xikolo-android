package de.xikolo.presenters.course;

import de.xikolo.models.Course;
import de.xikolo.presenters.base.LoadingStateView;

public interface CourseDescriptionView extends LoadingStateView {

    void setupView(Course course);
}
