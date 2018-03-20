package de.xikolo.presenters.channels;

import de.xikolo.presenters.base.PresenterFactory;

public class ChannelDetailsPresenterFactory implements PresenterFactory<ChannelDetailsPresenter> {

    private final String channelId;

    public ChannelDetailsPresenterFactory(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public ChannelDetailsPresenter create() {
        return new ChannelDetailsPresenter(channelId);
    }

}
