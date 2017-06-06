package de.xikolo.presenters.main;

public class CourseListFilterMyPresenterFactory extends CourseListPresenterFactory {

    @Override
    public CourseListPresenter create() {
        return new CourseListFilterMyPresenter();
    }

}
