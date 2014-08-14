package de.xikolo.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.path.android.jobqueue.JobManager;

import java.util.List;

import de.xikolo.data.preferences.EnrollmentsPreferences;
import de.xikolo.entities.Enrollment;
import de.xikolo.jobs.CreateEnrollmentJob;
import de.xikolo.jobs.DeleteEnrollmentJob;
import de.xikolo.jobs.OnJobResponseListener;
import de.xikolo.jobs.RetrieveEnrollmentsJob;

public class EnrollmentModel extends BaseModel {

    public static final String TAG = EnrollmentModel.class.getSimpleName();

    private EnrollmentsPreferences mEnrollmentPref;

    private OnModelResponseListener<List<Enrollment>> mListener;

    public EnrollmentModel(Context context, JobManager jobManager) {
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

    public void retrieveEnrollments(boolean cache) {
        OnJobResponseListener<List<Enrollment>> callback = new OnJobResponseListener<List<Enrollment>>() {
            @Override
            public void onResponse(final List<Enrollment> response) {
                if (mListener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onResponse(response);
                        }
                    });
                }
                if (response != null)
                    mEnrollmentPref.saveEnrollmentsSize(response.size());
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
        mJobManager.addJobInBackground(new RetrieveEnrollmentsJob(callback, cache, UserModel.readAccessToken(mContext)));
    }

    public void setRetrieveEnrollmentsListener(OnModelResponseListener<List<Enrollment>> listener) {
        mListener = listener;
    }

    public void createEnrollment(String enrollmentId) {
        OnJobResponseListener<Void> callback = new OnJobResponseListener<Void>() {
            @Override
            public void onResponse(Void response) {
                retrieveEnrollments(false);
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
                retrieveEnrollments(false);
            }

            @Override
            public void onCancel() {

            }
        };
        mJobManager.addJobInBackground(new DeleteEnrollmentJob(callback, enrollmentId, UserModel.readAccessToken(mContext)));
    }

}
