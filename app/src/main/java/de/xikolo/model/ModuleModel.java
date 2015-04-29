package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

import java.util.Collections;
import java.util.Comparator;
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
        result.setResultFilter(result.new ResultFilter() {
            @Override
            public List<Module> onFilter(List<Module> result, Result.DataSource dataSource) {
                sortModules(result);
                return result;
            }
        });

        mJobManager.addJobInBackground(new RetrieveModulesJob(result, course, includeProgress, moduleDataAccess));
    }

    public void getModulesWithItems(Result<List<Module>> result, Course course, boolean includeProgress) {
        result.setResultFilter(result.new ResultFilter() {
            @Override
            public List<Module> onFilter(List<Module> result, Result.DataSource dataSource) {
                sortModules(result);
                for (Module module : result) {
                    ItemModel.sortItems(module.items);
                }
                return result;
            }
        });

        mJobManager.addJobInBackground(new RetrieveModulesWithItemsJob(result, course, includeProgress, moduleDataAccess, itemDataAccess));
    }

    public static void sortModules(List<Module> modules) {
        Collections.sort(modules, new Comparator<Module>() {
            @Override
            public int compare(Module lhs, Module rhs) {
                return lhs.position - rhs.position;
            }
        });
    }

}
