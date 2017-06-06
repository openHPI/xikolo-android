package de.xikolo.presenters.course_items;

import de.xikolo.presenters.base.PresenterFactory;

public class CourseItemsPresenterFactory implements PresenterFactory<CourseItemsPresenter> {

    private final String sectionId;

    public CourseItemsPresenterFactory(String courseId) {
        this.sectionId = courseId;
    }

    @Override
    public CourseItemsPresenter create() {
        return new CourseItemsPresenter(sectionId);
    }

}
