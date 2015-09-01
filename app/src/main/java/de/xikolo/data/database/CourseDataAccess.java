package de.xikolo.data.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.data.entities.Course;

public class CourseDataAccess extends DataAccess {

    private OverallProgressDataAccess progressDataAccess;

    public CourseDataAccess(DatabaseHelper databaseHelper) {
        super(databaseHelper);

        this.progressDataAccess = new OverallProgressDataAccess(databaseHelper);
    }

    public void addCourse(Course course, boolean includeProgress) {
        openDatabase().insert(CourseTable.TABLE_NAME, null, buildContentValues(course));

        if (includeProgress) {
            progressDataAccess.addOrUpdateProgress(course.id, course.progress);
        }

        closeDatabase();
    }

    public void addOrUpdateCourse(Course course, boolean includeProgress) {
        if (updateCourse(course, includeProgress) < 1) {
            addCourse(course, includeProgress);
        }
    }

    public Course getCourse(String id) {
        Cursor cursor = openDatabase().query(
                CourseTable.TABLE_NAME,
                new String[]{
                        CourseTable.COLUMN_ID,
                        CourseTable.COLUMN_NAME,
                        CourseTable.COLUMN_DESCRIPTION,
                        CourseTable.COLUMN_COURSE_CODE,
                        CourseTable.COLUMN_LECTURER,
                        CourseTable.COLUMN_LANGUAGE,
                        CourseTable.COLUMN_URL,
                        CourseTable.COLUMN_VISUAL_URL,
                        CourseTable.COLUMN_AVAILABLE_FROM,
                        CourseTable.COLUMN_AVAILABLE_TO,
                        CourseTable.COLUMN_LOCKED,
                        CourseTable.COLUMN_IS_ENROLLED,
                },
                CourseTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(id)}, null, null, null, null);

        Course course = null;
        if (cursor.moveToFirst()) {
            course = buildCourse(cursor);
            course.progress = progressDataAccess.getProgress(id);
        }
        cursor.close();
        closeDatabase();

        return course;
    }

    public List<Course> getAllCourses() {
        List<Course> courseList = new ArrayList<Course>();

        String selectQuery = "SELECT * FROM " + CourseTable.TABLE_NAME;

        Cursor cursor = openDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Course course = buildCourse(cursor);
                course.progress = progressDataAccess.getProgress(course.id);
                courseList.add(course);
            } while (cursor.moveToNext());
        }

        cursor.close();
        closeDatabase();

        return courseList;
    }

    private Course buildCourse(Cursor cursor) {
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

    private ContentValues buildContentValues(Course course) {
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

    public int getCoursesCount() {
        String countQuery = "SELECT * FROM " + CourseTable.TABLE_NAME;
        Cursor cursor = openDatabase().rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();
        closeDatabase();

        return count;
    }

    public int getEnrollmentsCount() {
        String countQuery = "SELECT * FROM " + CourseTable.TABLE_NAME + " WHERE " + CourseTable.COLUMN_IS_ENROLLED + " != 0 ";
        Cursor cursor = openDatabase().rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();
        closeDatabase();

        return count;
    }

    public int updateCourse(Course course, boolean includeProgress) {
        int affected = openDatabase().update(
                CourseTable.TABLE_NAME,
                buildContentValues(course),
                CourseTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(course.id)});

        if (includeProgress) {
            progressDataAccess.addOrUpdateProgress(course.id, course.progress);
        }

        closeDatabase();

        return affected;
    }

    public void deleteCourse(Course course) {
        openDatabase().delete(
                CourseTable.TABLE_NAME,
                CourseTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(course.id)});

        progressDataAccess.deleteProgress(course.id);

        closeDatabase();
    }

}
