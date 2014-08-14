package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

import java.util.List;

import de.xikolo.entities.Module;
import de.xikolo.jobs.RetrieveModulesJob;
import de.xikolo.jobs.OnJobResponseListener;

public class ModuleModel extends BaseModel {

    public static final String TAG = ModuleModel.class.getSimpleName();

    private OnModelResponseListener<List<Module>> mListener;

    public ModuleModel(Context context, JobManager jobManager) {
        super(context, jobManager);
    }

    public void retrieveModules(String courseId, boolean cache, boolean includeProgress) {
        OnJobResponseListener<List<Module>> callback = new OnJobResponseListener<List<Module>>() {
            @Override
            public void onResponse(List<Module> response) {
                if (mListener != null)
                    mListener.onResponse(response);
            }

            @Override
            public void onCancel() {
                if (mListener != null)
                    mListener.onResponse(null);
            }
        };
        mJobManager.addJobInBackground(new RetrieveModulesJob(callback, courseId, cache, includeProgress, UserModel.readAccessToken(mContext)));
    }

    public void setRetrieveModulesListener(OnModelResponseListener<List<Module>> listener) {
        mListener = listener;
    }
}
