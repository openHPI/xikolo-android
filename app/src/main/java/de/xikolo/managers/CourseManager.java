package de.xikolo.managers;

import java.util.Date;
import java.util.List;

import de.xikolo.jobs.CreateEnrollmentJob;
import de.xikolo.jobs.DeleteEnrollmentJob;
import de.xikolo.jobs.GetCourseJob;
import de.xikolo.jobs.GetCourseProgressWithSectionsJob;
import de.xikolo.jobs.GetCourseWithSectionsJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.jobs.ListCoursesJob;
import de.xikolo.jobs.ListEnrollmentsJob;
import de.xikolo.managers.base.BaseManager;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.models.SectionProgress;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
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

    public RealmResults searchCourses(String query, boolean withEnrollment, Realm realm, RealmChangeListener<RealmResults<Course>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        RealmQuery<Course> dbQuery = realm
                .where(Course.class)
                .equalTo("external", false)
                .beginGroup()
                    .like("title", "*" + query + "*", Case.INSENSITIVE)
                    .or()
                    .like("shortAbstract", "*" + query + "*", Case.INSENSITIVE)
                    .or()
                    .like("description", "*" + query + "*", Case.INSENSITIVE)
                    .or()
                    .like("teachers", "*" + query + "*", Case.INSENSITIVE)
                .endGroup();

        if (withEnrollment) {
            dbQuery = dbQuery.isNotNull("enrollmentId");
        }

        RealmResults<Course> courseListPromise = dbQuery.findAllSortedAsync("startDate", Sort.DESCENDING);

        courseListPromise.addChangeListener(listener);

        return courseListPromise;
    }

    public Course getCourse(String id, Realm realm, RealmChangeListener<Course> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        Course coursePromise = realm
                .where(Course.class)
                .beginGroup()
                    .equalTo("id", id)
                    .or()
                    .equalTo("slug", id)
                .endGroup()
                .findFirstAsync();

        coursePromise.addChangeListener(listener);

        return coursePromise;
    }

    public List<Course> listCurrentAndFutureCourses(Realm realm) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .greaterThanOrEqualTo("endDate", new Date())
                .findAllSorted("startDate", Sort.ASCENDING);

        return courseList;
    }

    public List<Course> listCurrentAndPastCourses(Realm realm) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .lessThanOrEqualTo("startDate", new Date())
                .findAllSorted("startDate", Sort.DESCENDING);

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

    public List<Course> listFutureCourses(Realm realm) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .greaterThan("startDate", new Date())
                .findAllSorted("startDate", Sort.ASCENDING);

        return courseList;
    }

    public List<Course> listCurrentAndPastCoursesWithEnrollment(Realm realm) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .isNotNull("enrollmentId")
                .lessThanOrEqualTo("startDate", new Date())
                .findAllSorted("startDate", Sort.DESCENDING);

        return courseList;
    }

    public List<Course> listFutureCoursesWithEnrollment(Realm realm) {
        RealmResults<Course> courseList = realm
                .where(Course.class)
                .equalTo("external", false)
                .isNotNull("enrollmentId")
                .greaterThan("startDate", new Date())
                .findAllSorted("startDate", Sort.ASCENDING);

        return courseList;
    }

    public RealmResults listSectionProgressesForCourse(String courseId, Realm realm, RealmChangeListener<RealmResults<SectionProgress>> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RealmChangeListener should not be null for async queries.");
        }

        RealmResults<SectionProgress> spListPromise = realm
                .where(SectionProgress.class)
                .equalTo("courseProgressId", courseId)
                .findAllSortedAsync("position");

        spListPromise.addChangeListener(listener);

        return spListPromise;
    }

    public long countEnrollments(Realm realm) {
        return realm.where(Enrollment.class).count();
    }

    public void requestCourse(String courseId, JobCallback callback) {
        jobManager.addJobInBackground(new GetCourseJob(courseId, callback));
    }

    public void requestCourseWithSections(String courseId, JobCallback callback) {
        jobManager.addJobInBackground(new GetCourseWithSectionsJob(courseId, callback));
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

    public void requestCourseProgressWithSections(String courseId, JobCallback callback) {
        jobManager.addJobInBackground(new GetCourseProgressWithSectionsJob(courseId, callback));
    }

}
