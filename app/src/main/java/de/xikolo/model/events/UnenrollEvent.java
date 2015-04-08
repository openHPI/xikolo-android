package de.xikolo.model.events;

import de.xikolo.data.entities.Course;

public class UnenrollEvent extends Event {

    private Course course;

    public UnenrollEvent(Course course) {
        super(UnenrollEvent.class.getSimpleName() + ": course = " + course.course_code);
        this.course = course;
    }

    public Course getCourse() {
        return course;
    }

}
