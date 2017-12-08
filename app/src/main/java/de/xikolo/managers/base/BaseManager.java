package de.xikolo.managers.base;

import com.birbit.android.jobqueue.JobManager;

import de.xikolo.jobs.base.JobHelper;

public abstract class BaseManager {

    protected JobManager jobManager;

    public BaseManager() {
        super();
        this.jobManager = JobHelper.getJobManager();
    }

}
