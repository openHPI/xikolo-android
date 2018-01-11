package de.xikolo.jobs.base

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import de.xikolo.jobs.UpdateAnnouncementVisitedJob
import de.xikolo.jobs.UpdateItemVisitedJob

class ScheduledJobFactory : JobCreator {

    override fun create(tag: String): Job? = when (tag) {
        UpdateAnnouncementVisitedJob.TAG -> UpdateAnnouncementVisitedJob()
        UpdateItemVisitedJob.TAG         -> UpdateItemVisitedJob()
        else                             -> null
    }

}
