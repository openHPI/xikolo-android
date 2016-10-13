package de.xikolo.managers;

import com.path.android.jobqueue.JobManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.models.Course;
import de.xikolo.managers.jobs.CreateEnrollmentJob;
import de.xikolo.managers.jobs.DeleteEnrollmentJob;
import de.xikolo.managers.jobs.RetrieveCourseJob;
import de.xikolo.managers.jobs.RetrieveCourseListJob;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.CourseDataAdapter;
import de.xikolo.utils.DateUtil;

public class CourseManager extends BaseManager {

    public enum CourseFilter {
        ALL, MY
    }

    public static final String TAG = CourseManager.class.getSimpleName();

    public CourseManager(JobManager jobManager) {
        super(jobManager);
    }

    public int getEnrollmentsCount() {
        CourseDataAdapter courseDataAdapter = (CourseDataAdapter) GlobalApplication.getDataAdapter(DataType.COURSE);
        return courseDataAdapter.getEnrollmentsCount();
    }

    public void getCourse(Result<Course> result, String courseId) {
        jobManager.addJobInBackground(new RetrieveCourseJob(result, courseId));
    }

    public void getCourses(Result<List<Course>> result, boolean includeProgress) {
        getCourses(result, includeProgress, CourseFilter.ALL);
    }

    public void getCourses(Result<List<Course>> result, boolean includeProgress, final CourseFilter filter) {
        result.setResultFilter(result.new ResultFilter() {
            @Override
            public List<Course> onFilter(List<Course> result, Result.DataSource dataSource) {
                if (filter == CourseFilter.MY) {
                    ArrayList<Course> removeList = new ArrayList<>();
                    for (Course course : result) {
                        if (!course.is_enrolled) {
                            removeList.add(course);
                        }
                    }
                    result.removeAll(removeList);
                }
                sortCoursesAscending(result);
                return result;
            }
        });

        jobManager.addJobInBackground(new RetrieveCourseListJob(result, includeProgress));
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
                return DateUtil.compare(lhs.available_from, rhs.available_from);
            }
        });
    }

    public static void sortCoursesDecending(List<Course> courses) {
        Collections.sort(courses, new Comparator<Course>() {
            @Override
            public int compare(Course rhs, Course lhs) {
                return DateUtil.compare(lhs.available_from, rhs.available_from);
            }
        });
    }

    public static List<Course> getCurrentAndFutureCourses(List<Course> courses) {
        List<Course> currentCourses = new ArrayList<>();

        for (Course course : courses) {
            if (DateUtil.nowIsBetween(course.available_from, course.available_to) ||
                    DateUtil.nowIsBefore(course.available_from)) {
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
            if (DateUtil.nowIsBetween(course.available_from, course.available_to) ||
                    DateUtil.nowIsAfter(course.available_to)) {
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
