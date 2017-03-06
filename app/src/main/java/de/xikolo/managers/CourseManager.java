package de.xikolo.managers;

import java.util.Date;

import de.xikolo.managers.jobs.CreateEnrollmentJob;
import de.xikolo.managers.jobs.DeleteEnrollmentJob;
import de.xikolo.managers.jobs.GetCourseJob;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.managers.jobs.ListCoursesJob;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class CourseManager extends BaseManager {

    public static final String TAG = CourseManager.class.getSimpleName();

    public RealmResults listEnrollmentsAsync(Realm realm, RealmChangeListener<RealmResults<Enrollment>> listener) {
        if (listener == null) {
            throw new RuntimeException("RealmChangeListener is required for async queries.");
        }

        RealmResults<Enrollment> enrollmentListPromise = realm
                .where(Enrollment.class)
                .findAllAsync();

        enrollmentListPromise.addChangeListener(listener);

        return enrollmentListPromise;
    }

    public RealmResults listCoursesAsync(Realm realm, RealmChangeListener<RealmResults<Course>> listener) {
        if (listener == null) {
            throw new RuntimeException("RealmChangeListener is required for async queries.");
        }

        RealmResults<Course> courseListPromise = realm
                .where(Course.class)
                .equalTo("external", false)
                .findAllSortedAsync("startDate", Sort.DESCENDING);

        courseListPromise.addChangeListener(listener);

        return courseListPromise;
    }

    public RealmResults<Course> listCurrentAndFutureCourses(Realm realm, RealmChangeListener<RealmResults<Course>> listener) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .greaterThanOrEqualTo("endDate", new Date())
                .findAllSorted("startDate", Sort.ASCENDING);

        if (listener != null) {
            courseList.addChangeListener(listener);
        }

        return courseList;
    }

    public RealmResults<Course> listPastCourses(Realm realm, RealmChangeListener<RealmResults<Course>> listener) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .lessThan("endDate", new Date())
                .findAllSorted("startDate", Sort.DESCENDING);

        if (listener != null) {
            courseList.addChangeListener(listener);
        }

        return courseList;
    }

    public RealmResults<Course> listCurrentAndPastCoursesWithEnrollment(Realm realm, RealmChangeListener<RealmResults<Course>> listener) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .isNotNull("enrollment")
                .lessThanOrEqualTo("startDate", new Date())
                .findAllSorted("startDate", Sort.DESCENDING);

        if (listener != null) {
            courseList.addChangeListener(listener);
        }

        return courseList;
    }

    public RealmResults<Course> listFutureCoursesWithEnrollment(Realm realm, RealmChangeListener<RealmResults<Course>> listener) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .isNotNull("enrollment")
                .greaterThan("startDate", new Date())
                .findAllSorted("startDate", Sort.ASCENDING);

        if (listener != null) {
            courseList.addChangeListener(listener);
        }

        return courseList;
    }

    public void requestCourse(String courseId, JobCallback callback) {
        jobManager.addJobInBackground(new GetCourseJob(courseId, callback));
    }

    public void requestCourses(JobCallback callback) {
        jobManager.addJobInBackground(new ListCoursesJob(callback));
    }

    public void createEnrollment(String courseId, JobCallback callback) {
        jobManager.addJobInBackground(new CreateEnrollmentJob(courseId, callback));
    }

    public void deleteEnrollment(String id, JobCallback callback) {
        jobManager.addJobInBackground(new DeleteEnrollmentJob(id, callback));
    }

}
