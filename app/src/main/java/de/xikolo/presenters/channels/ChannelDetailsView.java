package de.xikolo.presenters.channels;

import java.util.List;

import de.xikolo.models.Channel;
import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;
import de.xikolo.presenters.base.LoadingStateView;

public interface ChannelDetailsView extends LoadingStateView {

    void setupView(Channel channel);

    void showContent(SectionList<String, List<Course>> courseList);

    void enterCourse(String courseId);

    void enterCourseDetails(String courseId);

    void openLogin();
}
