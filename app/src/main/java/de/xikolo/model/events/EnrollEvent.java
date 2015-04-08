package de.xikolo.model.events;

import de.xikolo.data.entities.Course;

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
