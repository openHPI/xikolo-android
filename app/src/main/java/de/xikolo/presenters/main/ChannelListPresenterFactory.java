package de.xikolo.presenters.main;

import de.xikolo.presenters.base.PresenterFactory;

public class ChannelListPresenterFactory implements PresenterFactory<ChannelListPresenter> {

    @Override
    public ChannelListPresenter create() {
        return new ChannelListPresenter();
    }

}
