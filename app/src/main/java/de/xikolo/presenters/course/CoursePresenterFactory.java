package de.xikolo.presenters.course;

import de.xikolo.presenters.base.PresenterFactory;

public class CoursePresenterFactory implements PresenterFactory<CoursePresenter> {

    @Override
    public CoursePresenter create() {
        return new CoursePresenter();
    }

}
