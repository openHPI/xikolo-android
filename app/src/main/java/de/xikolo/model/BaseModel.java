package de.xikolo.model;

import com.path.android.jobqueue.JobManager;

public abstract class BaseModel {

    protected JobManager mJobManager;

    public BaseModel(JobManager jobManager) {
        super();

        mJobManager = jobManager;
    }

}
