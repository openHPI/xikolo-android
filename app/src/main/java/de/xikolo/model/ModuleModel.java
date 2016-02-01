package de.xikolo.model;

import com.path.android.jobqueue.JobManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Module;
import de.xikolo.model.jobs.RetrieveModulesJob;
import de.xikolo.model.jobs.RetrieveModulesWithItemsJob;

public class ModuleModel extends BaseModel {

    public static final String TAG = ModuleModel.class.getSimpleName();

    public ModuleModel(JobManager jobManager) {
        super(jobManager);
    }

    public void getModules(Result<List<Module>> result, Course course, boolean includeProgress) {
        result.setResultFilter(result.new ResultFilter() {
            @Override
            public List<Module> onFilter(List<Module> result, Result.DataSource dataSource) {
                sortModules(result);
                return result;
            }
        });

        mJobManager.addJobInBackground(new RetrieveModulesJob(result, course, includeProgress));
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

        mJobManager.addJobInBackground(new RetrieveModulesWithItemsJob(result, course, includeProgress));
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
