package de.xikolo.managers;

import com.birbit.android.jobqueue.JobManager;

import de.xikolo.managers.jobs.JobHelper;

public abstract class BaseManager {

    protected JobManager jobManager;

    public BaseManager() {
        super();
        this.jobManager = JobHelper.getJobManager();
    }

}
