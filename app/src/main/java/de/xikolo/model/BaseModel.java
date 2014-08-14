package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

public abstract class BaseModel {

    protected Context mContext;

    protected JobManager mJobManager;

    public BaseModel(Context context, JobManager jobManager) {
        super();

        mContext = context;
        mJobManager = jobManager;
    }

}
