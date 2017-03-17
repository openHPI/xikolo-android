package de.xikolo.managers;

import java.util.Date;
import java.util.List;

import de.xikolo.managers.jobs.CreateEnrollmentJob;
import de.xikolo.managers.jobs.DeleteEnrollmentJob;
import de.xikolo.managers.jobs.GetCourseJob;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.managers.jobs.ListCoursesJob;
import de.xikolo.managers.jobs.ListEnrollmentsJob;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class CourseManager extends BaseManager {

    public static final String TAG = CourseManager.class.getSimpleName();

    public RealmResults listEnrollments(Realm realm, RealmChangeListener<RealmResults<Enrollment>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        RealmResults<Enrollment> enrollmentListPromise = realm
                .where(Enrollment.class)
                .findAllAsync();

        enrollmentListPromise.addChangeListener(listener);

        return enrollmentListPromise;
    }

    public RealmResults listCourses(Realm realm, RealmChangeListener<RealmResults<Course>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        RealmResults<Course> courseListPromise = realm
                .where(Course.class)
                .equalTo("external", false)
                .findAllSortedAsync("startDate", Sort.DESCENDING);

        courseListPromise.addChangeListener(listener);

        return courseListPromise;
    }

    public List<Course> listCurrentAndFutureCourses(Realm realm) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .greaterThanOrEqualTo("endDate", new Date())
                .findAllSorted("startDate", Sort.ASCENDING);

        return courseList;
    }

    public List<Course> listPastCourses(Realm realm) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .lessThan("endDate", new Date())
                .findAllSorted("startDate", Sort.DESCENDING);

        return courseList;
    }

    public List<Course> listCurrentAndPastCoursesWithEnrollment(Realm realm) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .isNotNull("enrollment")
                .lessThanOrEqualTo("startDate", new Date())
                .findAllSorted("startDate", Sort.DESCENDING);

        return courseList;
    }

    public List<Course> listFutureCoursesWithEnrollment(Realm realm) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .isNotNull("enrollment")
                .greaterThan("startDate", new Date())
                .findAllSorted("startDate", Sort.ASCENDING);

        return courseList;
    }

    public void requestCourse(String courseId, JobCallback callback) {
        jobManager.addJobInBackground(new GetCourseJob(courseId, callback));
    }

    public void requestCourseList(JobCallback callback) {
        jobManager.addJobInBackground(new ListCoursesJob(callback));
    }

    public void requestEnrollmentList(JobCallback callback) {
        jobManager.addJobInBackground(new ListEnrollmentsJob(callback));
    }

    public void createEnrollment(String courseId, JobCallback callback) {
        jobManager.addJobInBackground(new CreateEnrollmentJob(courseId, callback));
    }

    public void deleteEnrollment(String id, JobCallback callback) {
        jobManager.addJobInBackground(new DeleteEnrollmentJob(id, callback));
    }

}
