package de.xikolo.presenters.course;

import de.xikolo.presenters.base.PresenterFactory;

public class LearningsPresenterFactory implements PresenterFactory<LearningsPresenter> {

    private final String courseId;

    public LearningsPresenterFactory(String courseId) {
        this.courseId = courseId;
    }

    @Override
    public LearningsPresenter create() {
        return new LearningsPresenter(courseId);
    }

}
