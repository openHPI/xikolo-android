package de.xikolo.presenters.video;

import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.presenters.base.View;

public interface VideoView extends View {

    void setupVideo(Course course, Section section, Item item, Video video);

}
