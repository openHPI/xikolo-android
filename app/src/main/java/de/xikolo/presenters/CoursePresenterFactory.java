package de.xikolo.presenters;

public class CoursePresenterFactory implements PresenterFactory<CoursePresenter> {

    @Override
    public CoursePresenter create() {
        return new CoursePresenter();
    }

}
