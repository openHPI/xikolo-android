package de.xikolo.presenters.course;

import de.xikolo.presenters.base.PresenterFactory;

public class DescriptionPresenterFactory implements PresenterFactory<DescriptionPresenter> {

    private final String courseId;

    public DescriptionPresenterFactory(String courseId) {
        this.courseId = courseId;
    }

    @Override
    public DescriptionPresenter create() {
        return new DescriptionPresenter(courseId);
    }

}
