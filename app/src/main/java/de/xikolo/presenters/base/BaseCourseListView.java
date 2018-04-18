package de.xikolo.presenters.base;

import java.util.List;

import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;

public interface BaseCourseListView extends LoadingStateView {

    void enterCourse(String courseId);

    void enterCourseDetails(String courseId);

    void showCourseList(SectionList<String, List<Course>> courseList);

    void openLogin();
}
