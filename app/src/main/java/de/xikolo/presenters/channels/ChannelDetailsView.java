package de.xikolo.presenters.channels;

import de.xikolo.models.Channel;
import de.xikolo.presenters.base.LoadingStateView;

public interface ChannelDetailsView extends LoadingStateView {

    void setupView(Channel channel);
}
