package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;
import de.xikolo.presenters.base.LoadingStateView;

public interface CourseListView extends LoadingStateView {

    void showNoEnrollmentsMessage();

    void enterCourse(String courseId);

    void enterCourseDetails(String courseId);

    void showCourseList(SectionList<String, List<Course>> courseList);

    void goToProfile();

}
