package de.xikolo.presenters.course_items;

import de.xikolo.presenters.base.PresenterFactory;

public class CourseItemsPresenterFactory implements PresenterFactory<CourseItemsPresenter> {

    private final String courseId;

    private final String sectionId;

    private final int index;

    public CourseItemsPresenterFactory(String courseId, String sectionId, int index) {
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.index = index;
    }

    @Override
    public CourseItemsPresenter create() {
        return new CourseItemsPresenter(courseId, sectionId, index);
    }

}
