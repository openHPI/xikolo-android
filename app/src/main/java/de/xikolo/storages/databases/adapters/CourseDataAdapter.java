package de.xikolo.storages.databases.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

import de.xikolo.models.Course;
import de.xikolo.storages.databases.DatabaseHelper;
import de.xikolo.storages.databases.tables.CourseTable;
import de.xikolo.storages.databases.tables.Table;

public class CourseDataAdapter extends DataAdapter<Course> {

    private ProgressDataAdapter progressDataAccess;

    public CourseDataAdapter(DatabaseHelper databaseHelper, Table table, Table progressTable) {
        super(databaseHelper, table);

        this.progressDataAccess = new ProgressDataAdapter(databaseHelper, progressTable);
    }

    @Override
    protected Course buildEntity(Cursor cursor) {
        Course course = new Course();

        course.id = cursor.getString(0);
        course.name = cursor.getString(1);
        course.description = cursor.getString(2);
        course.course_code = cursor.getString(3);
        course.lecturer = cursor.getString(4);
        course.language = cursor.getString(5);
        course.url = cursor.getString(6);
        course.visual_url = cursor.getString(7);
        course.available_from = cursor.getString(8);
        course.available_to = cursor.getString(9);
        course.locked = cursor.getInt(10) != 0;
        course.is_enrolled = cursor.getInt(11) != 0;

        return course;
    }

    @Override
    protected ContentValues buildContentValues(Course course) {
        ContentValues values = new ContentValues();

        values.put(CourseTable.COLUMN_ID, course.id);
        values.put(CourseTable.COLUMN_NAME, course.name);
        values.put(CourseTable.COLUMN_DESCRIPTION, course.description);
        values.put(CourseTable.COLUMN_COURSE_CODE, course.course_code);
        values.put(CourseTable.COLUMN_LECTURER, course.lecturer);
        values.put(CourseTable.COLUMN_LANGUAGE, course.language);
        values.put(CourseTable.COLUMN_URL, course.url);
        values.put(CourseTable.COLUMN_VISUAL_URL, course.visual_url);
        values.put(CourseTable.COLUMN_AVAILABLE_FROM, course.available_from);
        values.put(CourseTable.COLUMN_AVAILABLE_TO, course.available_to);
        values.put(CourseTable.COLUMN_LOCKED, course.locked);
        values.put(CourseTable.COLUMN_IS_ENROLLED, course.is_enrolled);

        return values;
    }

    public void add(Course course, boolean includeProgress) {
        super.add(course);

        if (includeProgress) {
            progressDataAccess.addOrUpdate(course.progress);
        }
    }

    public void addOrUpdate(Course course, boolean includeProgress) {
        if (update(course, includeProgress) < 1) {
            add(course, includeProgress);
        }
    }

    @Override
    public Course get(String id) {
        Course course = super.get(id);
        course.progress = progressDataAccess.get(id);
        return course;
    }

    @Override
    public List<Course> getAll() {
        List<Course> courseList = super.getAll();

        for (Course course : courseList) {
            course.progress = progressDataAccess.get(course.id);
        }

        return courseList;
    }

    public int getEnrollmentsCount() {
        String countQuery = "SELECT * FROM " + CourseTable.TABLE_NAME + " WHERE " + CourseTable.COLUMN_IS_ENROLLED + " != 0 ";
        Cursor cursor = openDatabase().rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();
        closeDatabase();

        return count;
    }

    public int update(Course course, boolean includeProgress) {
        int affected =  super.update(course);

        if (includeProgress) {
            progressDataAccess.addOrUpdate(course.progress);
        }

        return affected;
    }

    @Override
    public void delete(String id) {
        super.delete(id);

        progressDataAccess.delete(id);
    }

}
