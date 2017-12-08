package de.xikolo.presenters.course;

import de.xikolo.presenters.base.PresenterFactory;

public class CourseDetailsPresenterFactory implements PresenterFactory<CourseDetailsPresenter> {

    private final String courseId;

    public CourseDetailsPresenterFactory(String courseId) {
        this.courseId = courseId;
    }

    @Override
    public CourseDetailsPresenter create() {
        return new CourseDetailsPresenter(courseId);
    }

}
