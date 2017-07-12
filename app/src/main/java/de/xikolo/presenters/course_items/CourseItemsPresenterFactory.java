package de.xikolo.presenters.course_items;

import de.xikolo.presenters.base.PresenterFactory;

public class CourseItemsPresenterFactory implements PresenterFactory<CourseItemsPresenter> {

    private final String courseId;

    private final String sectionId;

    private final String itemId;

    public CourseItemsPresenterFactory(String courseId, String sectionId, String itemId) {
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.itemId = itemId;
    }

    @Override
    public CourseItemsPresenter create() {
        return new CourseItemsPresenter(courseId, sectionId, itemId);
    }

}
