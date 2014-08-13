package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

import java.util.List;

import de.xikolo.entities.Course;
import de.xikolo.jobs.RetrieveCoursesJob;
import de.xikolo.jobs.OnJobResponseListener;

public class CourseModel extends BaseModel {

    public static final String TAG = CourseModel.class.getSimpleName();

    private OnModelResponseListener<List<Course>> mListener;

    public CourseModel(Context context, JobManager jobManager) {
        super(context, jobManager);
    }

    public void retrieveCourses(boolean cache, boolean includeProgress) {
        OnJobResponseListener<List<Course>> callback = new OnJobResponseListener<List<Course>>() {
            @Override
            public void onResponse(List<Course> response) {
                if (mListener != null)
                    mListener.onResponse(response);
            }

            @Override
            public void onCancel() {
                if (mListener != null)
                    mListener.onResponse(null);
            }
        };
        mJobManager.addJobInBackground(new RetrieveCoursesJob(callback, cache, includeProgress, UserModel.readAccessToken(mContext)));
    }

    public void setRetrieveCoursesListener(OnModelResponseListener<List<Course>> listener) {
        mListener = listener;
    }

}
