package de.xikolo.presenters.course_items;

import de.xikolo.presenters.base.PresenterFactory;

public class RichTextPresenterFactory implements PresenterFactory<RichTextPresenter> {

    private final String courseId;

    private final String sectionId;

    private final String itemId;

    public RichTextPresenterFactory(String courseId, String sectionId, String itemId) {
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.itemId = itemId;
    }

    @Override
    public RichTextPresenter create() {
        return new RichTextPresenter(courseId, sectionId, itemId);
    }

}
