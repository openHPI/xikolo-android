package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;

public interface CourseListView extends MainView {

    void showNoEnrollmentsMessage();

    void enterCourse(String courseId);

    void enterCourseDetails(String courseId);

    void showCourseList(SectionList<String, List<Course>> courseList);

}
