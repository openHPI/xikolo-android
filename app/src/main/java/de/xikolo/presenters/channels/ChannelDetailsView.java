package de.xikolo.presenters.channels;

import de.xikolo.models.Channel;
import de.xikolo.presenters.base.BaseCourseListView;

public interface ChannelDetailsView extends BaseCourseListView {

    void setupView(Channel channel);

}
