package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.models.Channel;
import de.xikolo.presenters.base.LoadingStateView;

public interface ChannelListView extends LoadingStateView {

    void showChannelList(List<Channel> channelList);
}
