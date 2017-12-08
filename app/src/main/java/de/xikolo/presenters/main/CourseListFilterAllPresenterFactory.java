package de.xikolo.presenters.main;

public class CourseListFilterAllPresenterFactory extends CourseListPresenterFactory {

    @Override
    public CourseListPresenter create() {
        return new CourseListFilterAllPresenter();
    }

}
