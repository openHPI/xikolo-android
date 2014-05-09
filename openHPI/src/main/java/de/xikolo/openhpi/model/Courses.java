package de.xikolo.openhpi.model;

import java.util.ArrayList;
import java.util.List;

public class Courses {

    private List<Course> courses;

    public Courses() {
        this.courses = new ArrayList<Course>();
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
    
}
