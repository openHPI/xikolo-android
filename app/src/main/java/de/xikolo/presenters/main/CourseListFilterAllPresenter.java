package de.xikolo.presenters.main;

public class CourseListFilterAllPresenter extends CourseListPresenter {

    @Override
    protected void updateContent() {
        courseList.clear();
        buildCourseList();
        getViewOrThrow().showCourseList(courseList);
    }

    @Override
    protected void setCourseListPromise() {
        this.courseListPromise = courseManager.listCourses(realm, getCourseListChangeListener());
    }

}
