package de.xikolo.presenters;

public class WebViewPresenterFactory implements PresenterFactory<WebViewPresenter> {

    @Override
    public WebViewPresenter create() {
        return new WebViewPresenter();
    }

}
