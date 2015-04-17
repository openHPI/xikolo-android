package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

import java.util.List;

import de.xikolo.data.database.DatabaseHelper;
import de.xikolo.data.database.ItemDataAccess;
import de.xikolo.data.database.ModuleDataAccess;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Module;
import de.xikolo.model.jobs.RetrieveModulesJob;
import de.xikolo.model.jobs.RetrieveModulesWithItemsJob;

public class ModuleModel extends BaseModel {

    public static final String TAG = ModuleModel.class.getSimpleName();

    private ModuleDataAccess moduleDataAccess;

    private ItemDataAccess itemDataAccess;

    public ModuleModel(Context context, JobManager jobManager, DatabaseHelper databaseHelper) {
        super(context, jobManager);

        this.moduleDataAccess = new ModuleDataAccess(databaseHelper);
        this.itemDataAccess = new ItemDataAccess(databaseHelper);
    }

    public void getModules(Result<List<Module>> result, Course course, boolean includeProgress) {
        mJobManager.addJobInBackground(new RetrieveModulesJob(result, course, includeProgress, moduleDataAccess));
    }

    public void getModulesWithItems(Result<List<Module>> result, Course course, boolean includeProgress) {
        mJobManager.addJobInBackground(new RetrieveModulesWithItemsJob(result, course, includeProgress, moduleDataAccess, itemDataAccess));
    }

}
