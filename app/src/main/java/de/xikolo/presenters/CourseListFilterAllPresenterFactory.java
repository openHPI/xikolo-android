package de.xikolo.presenters;

public class CourseListFilterAllPresenterFactory extends CourseListPresenterFactory {

    @Override
    public CourseListPresenter create() {
        return new CourseListFilterAllPresenter();
    }

}
