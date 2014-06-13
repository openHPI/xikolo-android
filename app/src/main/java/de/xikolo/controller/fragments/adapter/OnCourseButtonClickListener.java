package de.xikolo.controller.fragments.adapter;

import de.xikolo.model.Course;

public interface OnCourseButtonClickListener {

    public void onEnrollButtonClicked(Course course);

    public void onEnterButtonClicked(Course course);

    public void onDetailButtonClicked(Course course);

}
