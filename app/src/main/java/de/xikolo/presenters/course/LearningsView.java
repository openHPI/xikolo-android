package de.xikolo.presenters.course;

import java.util.List;

import de.xikolo.models.Course;
import de.xikolo.models.Section;
import de.xikolo.presenters.base.LoadingStateView;

public interface LearningsView extends LoadingStateView {

    void setupSections(List<Section> sectionList);

    void startCourseItemsActivity(String courseId, String sectionId, int position);

    void startSectionDownload(Course course, Section section);

}
