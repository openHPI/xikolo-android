package de.xikolo.presenters.course;

import de.xikolo.presenters.base.PresenterFactory;

public class CourseDescriptionPresenterFactory implements PresenterFactory<CourseDescriptionPresenter> {

    private final String courseId;

    public CourseDescriptionPresenterFactory(String courseId) {
        this.courseId = courseId;
    }

    @Override
    public CourseDescriptionPresenter create() {
        return new CourseDescriptionPresenter(courseId);
    }

}
