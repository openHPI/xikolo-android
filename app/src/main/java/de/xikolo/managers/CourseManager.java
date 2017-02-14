package de.xikolo.managers;

import com.birbit.android.jobqueue.JobManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.xikolo.managers.jobs.CreateEnrollmentJob;
import de.xikolo.managers.jobs.DeleteEnrollmentJob;
import de.xikolo.managers.jobs.ListCoursesJob;
import de.xikolo.managers.jobs.RetrieveCourseJob;
import de.xikolo.models.Course;
import de.xikolo.utils.DateUtil;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CourseManager extends BaseManager {

    public enum CourseFilter {
        ALL, MY
    }

    public static final String TAG = CourseManager.class.getSimpleName();

    public CourseManager(JobManager jobManager) {
        super(jobManager);
    }

    public int getEnrollmentsCount() {
//        CourseDataAdapter courseDataAdapter = (CourseDataAdapter) GlobalApplication.getDataAdapter(DataType.COURSE);
//        return courseDataAdapter.getEnrollmentsCount();
        return 0;
    }

    public void getCourse(Result<Course> result, String courseId) {
        jobManager.addJobInBackground(new RetrieveCourseJob(result, courseId));
    }

    public RealmResults listCourses(Realm realm, RealmChangeListener<RealmResults<Course>> listener) {
        RealmResults<Course> courseListPromise = realm.where(Course.class).findAllSortedAsync("startDate");
        courseListPromise.addChangeListener(listener);
        return courseListPromise;
    }

    public void requestCourses() {
        jobManager.addJobInBackground(new ListCoursesJob());
    }

    public void addEnrollment(Result<Course> result, Course course) {
        jobManager.addJobInBackground(new CreateEnrollmentJob(result, course));
    }

    public void deleteEnrollment(Result<Course> result, Course course) {
        jobManager.addJobInBackground(new DeleteEnrollmentJob(result, course));
    }

    public static void sortCoursesAscending(List<Course> courses) {
        Collections.sort(courses, new Comparator<Course>() {
            @Override
            public int compare(Course lhs, Course rhs) {
                return DateUtil.compare(lhs.startDate, rhs.endDate);
            }
        });
    }

    public static void sortCoursesDecending(List<Course> courses) {
        Collections.sort(courses, new Comparator<Course>() {
            @Override
            public int compare(Course rhs, Course lhs) {
                return DateUtil.compare(lhs.startDate, rhs.endDate);
            }
        });
    }

    public static List<Course> getCurrentAndFutureCourses(List<Course> courses) {
        List<Course> currentCourses = new ArrayList<>();

        for (Course course : courses) {
            if (DateUtil.nowIsBetween(course.startDate, course.endDate) ||
                    DateUtil.nowIsBefore(course.endDate)) {
                currentCourses.add(course);
            }
        }

        sortCoursesDecending(currentCourses);

        return currentCourses;
    }

    public static List<Course> getPastCourses(List<Course> courses) {
        List<Course> pastCourses = new ArrayList<>(courses);

        List<Course> currentCourses = getCurrentAndFutureCourses(courses);

        pastCourses.removeAll(currentCourses);

        sortCoursesAscending(pastCourses);

        return pastCourses;
    }

    public static List<Course> getCurrentAndPastCourses(List<Course> courses) {
        List<Course> currentCourses = new ArrayList<>();

        for (Course course : courses) {
            if (DateUtil.nowIsBetween(course.startDate, course.endDate) ||
                    DateUtil.nowIsAfter(course.endDate)) {
                currentCourses.add(course);
            }
        }

        sortCoursesAscending(currentCourses);

        return currentCourses;
    }

    public static List<Course> getFutureCourses(List<Course> courses) {
        List<Course> futureCourses = new ArrayList<>(courses);

        List<Course> currentCourses = getCurrentAndPastCourses(courses);

        futureCourses.removeAll(currentCourses);

        sortCoursesDecending(futureCourses);

        return futureCourses;
    }

}
