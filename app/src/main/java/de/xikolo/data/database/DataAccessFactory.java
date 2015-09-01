package de.xikolo.data.database;

public class DataAccessFactory {

    private CourseDataAccess courseDataAccess;

    private ModuleDataAccess moduleDataAccess;

    private ItemDataAccess itemDataAccess;

    private OverallProgressDataAccess overallProgressDataAccess;

    private VideoDataAccess videoDataAccess;

    private DatabaseHelper databaseHelper;

    public DataAccessFactory(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public CourseDataAccess getCourseDataAccess() {
        if (courseDataAccess == null) {
            courseDataAccess = new CourseDataAccess(databaseHelper);
        }
        return courseDataAccess;
    }

    public ModuleDataAccess getModuleDataAccess() {
        if (moduleDataAccess == null) {
            moduleDataAccess = new ModuleDataAccess(databaseHelper);
        }
        return moduleDataAccess;
    }

    public ItemDataAccess getItemDataAccess() {
        if (itemDataAccess == null) {
            itemDataAccess = new ItemDataAccess(databaseHelper);
        }
        return itemDataAccess;
    }

    public OverallProgressDataAccess getOverallProgressDataAccess() {
        if (overallProgressDataAccess == null) {
            overallProgressDataAccess = new OverallProgressDataAccess(databaseHelper);
        }
        return overallProgressDataAccess;
    }

    public VideoDataAccess getVideoDataAccess() {
        if (videoDataAccess == null) {
            videoDataAccess = new VideoDataAccess(databaseHelper);
        }
        return videoDataAccess;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
}
