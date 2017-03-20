package de.xikolo.managers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.xikolo.managers.jobs.RetrieveModuleListJob;
import de.xikolo.managers.jobs.RetrieveModuleListWithItemListJob;
import de.xikolo.managers.jobs.RetrieveModuleWithItemListJob;
import de.xikolo.models.Course;
import de.xikolo.models.Section;

public class ModuleManager extends BaseManager {

    public static final String TAG = ModuleManager.class.getSimpleName();

    public void getModules(Result<List<Section>> result, Course course, boolean includeProgress) {
        result.setResultFilter(result.new ResultFilter() {
            @Override
            public List<Section> onFilter(List<Section> result, Result.DataSource dataSource) {
                sortModules(result);
                return result;
            }
        });

        jobManager.addJobInBackground(new RetrieveModuleListJob(result, course.id, includeProgress));
    }

    public void getModulesWithItems(Result<List<Section>> result, Course course, boolean includeProgress) {
        result.setResultFilter(result.new ResultFilter() {
            @Override
            public List<Section> onFilter(List<Section> result, Result.DataSource dataSource) {
                sortModules(result);
                for (Section module : result) {
                    ItemManager.sortItems(module.items);
                }
                return result;
            }
        });

        jobManager.addJobInBackground(new RetrieveModuleListWithItemListJob(result, course.id, includeProgress));
    }

    public void getModuleWithItems(Result<Section> result, String courseId, String moduleId) {
        result.setResultFilter(result.new ResultFilter() {
            @Override
            public Section onFilter(Section result, Result.DataSource dataSource) {
                ItemManager.sortItems(result.items);
                return result;
            }
        });

        jobManager.addJobInBackground(new RetrieveModuleWithItemListJob(result, courseId, moduleId));
    }

    public static void sortModules(List<Section> modules) {
        Collections.sort(modules, new Comparator<Section>() {
            @Override
            public int compare(Section lhs, Section rhs) {
                return lhs.position - rhs.position;
            }
        });
    }

}
