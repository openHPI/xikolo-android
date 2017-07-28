package de.xikolo.presenters.course;

import de.xikolo.presenters.base.PresenterFactory;

public class ProgressPresenterFactory implements PresenterFactory<ProgressPresenter> {

    private final String courseId;

    public ProgressPresenterFactory(String courseId) {
        this.courseId = courseId;
    }

    @Override
    public ProgressPresenter create() {
        return new ProgressPresenter(courseId);
    }

}
