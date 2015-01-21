package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

import java.util.List;

import de.xikolo.data.database.DatabaseHelper;
import de.xikolo.data.database.ModuleDataAccess;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Module;
import de.xikolo.model.jobs.RetrieveModulesJob;

public class ModuleModel extends BaseModel {

    public static final String TAG = ModuleModel.class.getSimpleName();

    private ModuleDataAccess moduleDataAccess;

    public ModuleModel(Context context, JobManager jobManager, DatabaseHelper databaseHelper) {
        super(context, jobManager);

        this.moduleDataAccess = new ModuleDataAccess(databaseHelper);
    }

    public void getModules(Result<List<Module>> result, Course course, boolean includeProgress) {
        mJobManager.addJobInBackground(new RetrieveModulesJob(result, course, includeProgress, moduleDataAccess));
    }

}
