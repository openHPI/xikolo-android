package de.xikolo.model;

import com.path.android.jobqueue.JobManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.data.entities.Course;
import de.xikolo.model.jobs.CreateEnrollmentJob;
import de.xikolo.model.jobs.DeleteEnrollmentJob;
import de.xikolo.model.jobs.RetrieveCoursesJob;
import de.xikolo.util.DateUtil;

public class CourseModel extends BaseModel {

    public enum CourseFilter {
        ALL, MY
    }

    public static final String TAG = CourseModel.class.getSimpleName();

    public CourseModel(JobManager jobManager) {
        super(jobManager);
    }

    public int getEnrollmentsCount() {
        return GlobalApplication.getInstance()
                .getDataAccessFactory().getCourseDataAccess().getEnrollmentsCount();
    }

    public void getCourses(Result<List<Course>> result, boolean includeProgress) {
        getCourses(result, includeProgress, CourseFilter.ALL);
    }

    public void getCourses(Result<List<Course>> result, boolean includeProgress, final CourseFilter filter) {
        result.setResultFilter(result.new ResultFilter() {
            @Override
            public List<Course> onFilter(List<Course> result, Result.DataSource dataSource) {
                if (filter == CourseFilter.MY) {
                    ArrayList<Course> removeList = new ArrayList<Course>();
                    for (Course course : result) {
                        if (!course.is_enrolled) {
                            removeList.add(course);
                        }
                    }
                    result.removeAll(removeList);
                }
                sortCourses(result);
                return result;
            }
        });

        mJobManager.addJobInBackground(new RetrieveCoursesJob(result, includeProgress));
    }

    public void addEnrollment(Result<Course> result, Course course) {
        mJobManager.addJobInBackground(new CreateEnrollmentJob(result, course));
    }

    public void deleteEnrollment(Result<Course> result, Course course) {
        mJobManager.addJobInBackground(new DeleteEnrollmentJob(result, course));
    }

    public static void sortCourses(List<Course> courses) {
        Collections.sort(courses, new Comparator<Course>() {
            @Override
            public int compare(Course lhs, Course rhs) {
                return DateUtil.compare(lhs.available_from, rhs.available_from);
            }
        });
    }

}
