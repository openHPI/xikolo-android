package de.xikolo.events;

import de.xikolo.models.Course;

public class EnrollEvent extends Event {

    private Course course;

    public EnrollEvent(Course course) {
        super(EnrollEvent.class.getSimpleName() + ": course = " + course.course_code);
        this.course = course;
    }

    public Course getCourse() {
        return course;
    }

}
