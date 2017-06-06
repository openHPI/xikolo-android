package de.xikolo.presenters.shared;

import de.xikolo.presenters.base.PresenterFactory;

public class WebViewPresenterFactory implements PresenterFactory<WebViewPresenter> {

    @Override
    public WebViewPresenter create() {
        return new WebViewPresenter();
    }

}
