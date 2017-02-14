package de.xikolo.events;

import de.xikolo.models.Course;

public class UnenrollEvent extends Event {

    private Course course;

    public UnenrollEvent(Course course) {
        super(UnenrollEvent.class.getSimpleName() + ": course = " + course.slug);
        this.course = course;
    }

    public Course getCourse() {
        return course;
    }

}
