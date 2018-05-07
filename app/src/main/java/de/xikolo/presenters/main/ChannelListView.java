package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.models.Channel;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.LoadingStateView;

public interface ChannelListView extends LoadingStateView {

    void showChannelList(List<Channel> channelList);

    void showChannel(String channelId);

    void showCourse(Course course);

    void showChannelCourses(String courseId, int coursePosition);
}
