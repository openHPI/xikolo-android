package de.xikolo.presenters;

public class CourseListPresenterFactory implements PresenterFactory<CourseListPresenter> {

    @Override
    public CourseListPresenter create() {
        return new CourseListPresenter();
    }

}
