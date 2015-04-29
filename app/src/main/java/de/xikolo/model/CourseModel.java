package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.xikolo.data.database.CourseDataAccess;
import de.xikolo.data.database.DatabaseHelper;
import de.xikolo.data.database.ModuleDataAccess;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Module;
import de.xikolo.model.jobs.CreateEnrollmentJob;
import de.xikolo.model.jobs.DeleteEnrollmentJob;
import de.xikolo.model.jobs.RetrieveCoursesJob;
import de.xikolo.util.DateUtil;

public class CourseModel extends BaseModel {

    public enum CourseFilter {
        ALL, MY
    }

    public static final String TAG = CourseModel.class.getSimpleName();

    private CourseDataAccess courseDataAccess;
    private ModuleDataAccess moduleDataAccess;

    public CourseModel(Context context, JobManager jobManager, DatabaseHelper databaseHelper) {
        super(context, jobManager);

        courseDataAccess = new CourseDataAccess(databaseHelper);
        moduleDataAccess = new ModuleDataAccess(databaseHelper);
    }

    public int getEnrollmentsCount() {
        return courseDataAccess.getEnrollmentsCount();
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

        mJobManager.addJobInBackground(new RetrieveCoursesJob(result, includeProgress, courseDataAccess));
    }

    public void addEnrollment(Result<Course> result, Course course) {
        mJobManager.addJobInBackground(new CreateEnrollmentJob(result, course, courseDataAccess));
    }

    public void deleteEnrollment(Result<Course> result, Course course) {
        mJobManager.addJobInBackground(new DeleteEnrollmentJob(result, course, courseDataAccess, moduleDataAccess));
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
