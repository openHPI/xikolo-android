package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

import de.xikolo.jobs.OnJobResponseListener;
import de.xikolo.jobs.UpdateProgressionJob;

public class ProgressionModel extends BaseModel {

    public static final String TAG = ProgressionModel.class.getSimpleName();

    public ProgressionModel(Context context, JobManager jobManager) {
        super(context, jobManager);
    }

    public void updateProgression(String itemId) {
        OnJobResponseListener<Void> callback = new OnJobResponseListener<Void>() {
            @Override
            public void onResponse(Void response) {
            }

            @Override
            public void onCancel() {
            }
        };
        mJobManager.addJobInBackground(new UpdateProgressionJob(callback, itemId, UserModel.readAccessToken(mContext)));
    }

}
