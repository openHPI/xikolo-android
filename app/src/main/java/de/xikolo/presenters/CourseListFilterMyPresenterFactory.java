package de.xikolo.presenters;

public class CourseListFilterMyPresenterFactory extends CourseListPresenterFactory {

    @Override
    public CourseListPresenter create() {
        return new CourseListFilterMyPresenter();
    }

}
