package de.xikolo.presenters.main;

import de.xikolo.presenters.base.PresenterFactory;

public class NewsListPresenterFactory implements PresenterFactory<NewsListPresenter> {

    private String courseId;

    public NewsListPresenterFactory() {
    }

    public NewsListPresenterFactory(String courseId) {
        this.courseId = courseId;
    }

    @Override
    public NewsListPresenter create() {
        return new NewsListPresenter(courseId);
    }

}
