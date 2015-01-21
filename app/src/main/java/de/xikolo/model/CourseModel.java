package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

import java.util.List;

import de.xikolo.data.database.CourseDataAccess;
import de.xikolo.data.database.DatabaseHelper;
import de.xikolo.data.database.ModuleDataAccess;
import de.xikolo.data.entities.Course;
import de.xikolo.model.jobs.CreateEnrollmentJob;
import de.xikolo.model.jobs.DeleteEnrollmentJob;
import de.xikolo.model.jobs.RetrieveCoursesJob;

public class CourseModel extends BaseModel {

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
        mJobManager.addJobInBackground(new RetrieveCoursesJob(result, includeProgress, courseDataAccess));
    }

    public void addEnrollment(Result<Void> result, Course course) {
        mJobManager.addJobInBackground(new CreateEnrollmentJob(result, course, courseDataAccess));
    }

    public void deleteEnrollment(Result<Void> result, Course course) {
        mJobManager.addJobInBackground(new DeleteEnrollmentJob(result, course, courseDataAccess, moduleDataAccess));


    }

}
