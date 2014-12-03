package de.xikolo.data.database;

import android.database.sqlite.SQLiteDatabase;

public class CourseTable implements Table {

    private static final String TABLE_NAME = "dictionary";

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_COURSE_CODE = "course_code";
    public static final String COLUMN_LECTURER = "lecturer";
    public static final String COLUMN_LANGUAGE = "language";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_VISUAL_URL = "visual_url";
    public static final String COLUMN_AVAILABLE_FROM = "available_from";
    public static final String COLUMN_AVAILABLE_TO = "available_to";
    public static final String COLUMN_LOCKED = "locked";
    public static final String COLUMN_IS_ENROLLED = "is_enrolled";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " text primary key, " +
                    COLUMN_NAME + " text, " +
                    COLUMN_DESCRIPTION + " text, " +
                    COLUMN_COURSE_CODE + " text, " +
                    COLUMN_LECTURER + " text, " +
                    COLUMN_LANGUAGE + " text, " +
                    COLUMN_URL + " text, " +
                    COLUMN_VISUAL_URL + " text, " +
                    COLUMN_AVAILABLE_FROM + " text, " +
                    COLUMN_AVAILABLE_TO + " text, " +
                    COLUMN_LOCKED + " integer, " +
                    COLUMN_IS_ENROLLED + " integer" +
                     ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
