package de.xikolo.managers;

import com.path.android.jobqueue.JobManager;

public abstract class BaseManager {

    protected JobManager jobManager;

    public BaseManager(JobManager jobManager) {
        super();

        this.jobManager = jobManager;
    }

}
