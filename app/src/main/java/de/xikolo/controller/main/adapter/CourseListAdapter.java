package de.xikolo.controller.main.adapter;

import android.widget.BaseAdapter;

import java.util.List;

import de.xikolo.entities.Course;
import de.xikolo.entities.Enrollment;

public abstract class CourseListAdapter extends BaseAdapter {

    public abstract void updateCourses(List<Course> courses);

    public abstract void updateEnrollments(List<Enrollment> enrolls);

    public abstract void clear();

    public interface OnCourseButtonClickListener {

        public void onEnrollButtonClicked(Course course);

        public void onEnterButtonClicked(Course course);

        public void onDetailButtonClicked(Course course);

    }

}
