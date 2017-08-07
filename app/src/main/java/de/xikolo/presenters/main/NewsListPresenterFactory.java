package de.xikolo.presenters.main;

import de.xikolo.presenters.base.PresenterFactory;

public class NewsListPresenterFactory implements PresenterFactory<NewsListPresenter> {

    @Override
    public NewsListPresenter create() {
        return new NewsListPresenter();
    }

}
