package de.xikolo.data.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Module;

public class ModuleDataAccess extends DataAccess {

    private OverallProgressDataAccess progressDataAccess;

    public ModuleDataAccess(DatabaseHelper databaseHelper) {
        super(databaseHelper);

        this.progressDataAccess = new OverallProgressDataAccess(databaseHelper);
    }

    public void addModule(Course course, Module module) {
        getDatabase().insert(ModuleTable.TABLE_NAME, null, buildContentValues(course, module));

        progressDataAccess.addProgress(module.id, module.progress);
    }

    public void addOrUpdateModule(Course course, Module module) {
        if (updateModule(course, module) < 1) {
            addModule(course, module);
        }
    }

    public Module getModule(String id) {
        Cursor cursor = getDatabase().query(
                ModuleTable.TABLE_NAME,
                new String[]{
                        ModuleTable.COLUMN_ID,
                        ModuleTable.COLUMN_POSITION,
                        ModuleTable.COLUMN_NAME,
                        ModuleTable.COLUMN_AVAILABLE_FROM,
                        ModuleTable.COLUMN_AVAILABLE_TO,
                        ModuleTable.COLUMN_LOCKED,
                },
                ModuleTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(id)}, null, null, null, null);

        Module module = null;
        if (cursor.moveToFirst()) {
            module = buildModule(cursor);
            module.progress = progressDataAccess.getProgress(module.id);
        }
        cursor.close();

        return module;
    }

    public List<Module> getAllModules() {
        List<Module> moduleList = new ArrayList<Module>();

        String selectQuery = "SELECT * FROM " + ModuleTable.TABLE_NAME;

        Cursor cursor = getDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Module module = buildModule(cursor);
                module.progress = progressDataAccess.getProgress(module.id);
                moduleList.add(module);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return moduleList;
    }

    public List<Module> getAllModulesForCourse(Course course) {
        List<Module> moduleList = new ArrayList<Module>();

        String selectQuery = "SELECT * FROM " + ModuleTable.TABLE_NAME + " WHERE " + ModuleTable.COLUMN_COURSE_ID + " = \'" + course.id + "\'";

        Cursor cursor = getDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Module module = buildModule(cursor);
                module.progress = progressDataAccess.getProgress(module.id);
                moduleList.add(module);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return moduleList;
    }

    private Module buildModule(Cursor cursor) {
        Module module = new Module();

        module.id = cursor.getString(0);
        module.position = cursor.getInt(1);
        module.name = cursor.getString(2);
        module.available_from = cursor.getString(3);
        module.available_to = cursor.getString(4);
        module.locked = cursor.getInt(5) != 0;

        return module;
    }

    private ContentValues buildContentValues(Course course, Module module) {
        ContentValues values = new ContentValues();
        values.put(ModuleTable.COLUMN_ID, module.id);
        values.put(ModuleTable.COLUMN_POSITION, module.position);
        values.put(ModuleTable.COLUMN_NAME, module.name);
        values.put(ModuleTable.COLUMN_AVAILABLE_FROM, module.available_from);
        values.put(ModuleTable.COLUMN_AVAILABLE_TO, module.available_to);
        values.put(ModuleTable.COLUMN_LOCKED, module.locked);
        values.put(ModuleTable.COLUMN_COURSE_ID, course.id);

        return values;
    }

    public int getModulesCount() {
        String countQuery = "SELECT * FROM " + ModuleTable.TABLE_NAME;
        Cursor cursor = getDatabase().rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    public int updateModule(Course course, Module module) {
        int affected = getDatabase().update(
                ModuleTable.TABLE_NAME,
                buildContentValues(course, module),
                ModuleTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(module.id)});

        progressDataAccess.updateProgress(module.id, module.progress);

        return affected;
    }

    public void deleteModule(Module module) {
        getDatabase().delete(
                ModuleTable.TABLE_NAME,
                ModuleTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(module.id)});

        progressDataAccess.deleteProgress(module.id);
    }

}
