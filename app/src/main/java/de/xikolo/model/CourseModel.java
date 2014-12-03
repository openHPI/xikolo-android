package de.xikolo.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.path.android.jobqueue.JobManager;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.data.preferences.EnrollmentsPreferences;
import de.xikolo.data.entities.Course;
import de.xikolo.model.jobs.CreateEnrollmentJob;
import de.xikolo.model.jobs.DeleteEnrollmentJob;
import de.xikolo.model.jobs.OnJobResponseListener;
import de.xikolo.model.jobs.RetrieveCoursesJob;

public class CourseModel extends BaseModel {

    public static final String TAG = CourseModel.class.getSimpleName();

    public static final String FILTER_ALL = "filter_all";
    public static final String FILTER_MY = "filter_my";

    private OnModelResponseListener<List<Course>> mListener;

    private EnrollmentsPreferences mEnrollmentPref;

    public CourseModel(Context context, JobManager jobManager) {
        super(context, jobManager);

        this.mEnrollmentPref = new EnrollmentsPreferences(context);
    }

    public static int readEnrollmentsSize(Context context) {
        EnrollmentsPreferences prefs = new EnrollmentsPreferences(context);
        return prefs.getEnrollmentsSize();
    }

    public static void deleteEnrollmentsSize(Context context) {
        EnrollmentsPreferences prefs = new EnrollmentsPreferences(context);
        prefs.deleteEnrollmentsSize();
    }

    public void retrieveCourses(boolean cache, boolean includeProgress) {
        retrieveCourses(FILTER_ALL, cache, includeProgress);
    }

    public void retrieveCourses(final String filter, boolean cache, boolean includeProgress) {
        OnJobResponseListener<List<Course>> callback = new OnJobResponseListener<List<Course>>() {
            @Override
            public void onResponse(final List<Course> response) {
                final List<Course> myCourses = new ArrayList<Course>();
                if (response != null) {
                    for (Course course : response) {
                        if (course.is_enrolled) {
                            myCourses.add(course);
                        }
                    }
                    mEnrollmentPref.saveEnrollmentsSize(myCourses.size());
                }
                if (mListener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (FILTER_MY.equals(filter)) {
                                mListener.onResponse(myCourses);
                            } else {
                                mListener.onResponse(response);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancel() {
                if (mListener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onResponse(null);
                        }
                    });
                }
            }
        };
        mJobManager.addJobInBackground(new RetrieveCoursesJob(callback, cache, includeProgress, UserModel.readAccessToken(mContext)));
    }

    public void setRetrieveCoursesListener(OnModelResponseListener<List<Course>> listener) {
        mListener = listener;
    }

    public void createEnrollment(String enrollmentId) {
        OnJobResponseListener<Void> callback = new OnJobResponseListener<Void>() {
            @Override
            public void onResponse(Void response) {
                retrieveCourses(false, false);
            }

            @Override
            public void onCancel() {

            }
        };
        mJobManager.addJobInBackground(new CreateEnrollmentJob(callback, enrollmentId, UserModel.readAccessToken(mContext)));
    }

    public void deleteEnrollment(String enrollmentId) {
        OnJobResponseListener<Void> callback = new OnJobResponseListener<Void>() {
            @Override
            public void onResponse(Void response) {
                retrieveCourses(false, false);
            }

            @Override
            public void onCancel() {

            }
        };
        mJobManager.addJobInBackground(new DeleteEnrollmentJob(callback, enrollmentId, UserModel.readAccessToken(mContext)));
    }

}
