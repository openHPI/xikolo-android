package de.xikolo.presenters.course_items;

import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.presenters.base.LoadingStateView;

public interface VideoPreviewView extends LoadingStateView {

    void setupView(Course course, Section section, Item item, Video video);

    void startVideo(Video video);

    void startCast(Video video);

}
