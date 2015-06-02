package de.xikolo.data.database;

import android.database.sqlite.SQLiteDatabase;

class OverallProgressTable implements Table {

    public static final String TABLE_NAME = "overall_progress";

    public static final String COLUMN_ITEM_COUNT_AVAILABLE = "item_count_available";
    public static final String COLUMN_ITEM_COUNT_VISITED = "item_count_visited";
    public static final String COLUMN_ITEM_COUNT_COMPLETED = "item_count_completed";

    public static final String COLUMN_SELF_TESTS_COUNT_AVAILABLE = "self_tests_count_available";
    public static final String COLUMN_SELF_TESTS_COUNT_TAKEN = "self_tests_count_taken";
    public static final String COLUMN_SELF_TESTS_POINTS_POSSIBLE = "self_tests_points_possible";
    public static final String COLUMN_SELF_TESTS_POINTS_SCORED = "self_tests_points_scored";

    public static final String COLUMN_ASSIGNMENTS_COUNT_AVAILABLE = "assignments_count_available";
    public static final String COLUMN_ASSIGNMENTS_COUNT_TAKEN = "assignments_count_taken";
    public static final String COLUMN_ASSIGNMENTS_POINTS_POSSIBLE = "assignments_points_possible";
    public static final String COLUMN_ASSIGNMENTS_POINTS_SCORED = "assignments_points_scored";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " text primary key, " +
                    COLUMN_ITEM_COUNT_AVAILABLE + " integer, " +
                    COLUMN_ITEM_COUNT_VISITED + " integer, " +
                    COLUMN_ITEM_COUNT_COMPLETED + " integer, " +
                    COLUMN_SELF_TESTS_COUNT_AVAILABLE + " real, " +
                    COLUMN_SELF_TESTS_COUNT_TAKEN + " real, " +
                    COLUMN_SELF_TESTS_POINTS_POSSIBLE + " real, " +
                    COLUMN_SELF_TESTS_POINTS_SCORED + " real, " +
                    COLUMN_ASSIGNMENTS_COUNT_AVAILABLE + " real, " +
                    COLUMN_ASSIGNMENTS_COUNT_TAKEN + " real, " +
                    COLUMN_ASSIGNMENTS_POINTS_POSSIBLE + " real, " +
                    COLUMN_ASSIGNMENTS_POINTS_SCORED + " real " +
                    ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

       if(oldVersion == 1) {

           switch(newVersion) {
               case 2:
                   // keep db
                   System.out.println("Keeping " + TABLE_NAME);
                   break;
               default:
                   System.out.println("Deleting " + TABLE_NAME);
                   deleteTable(db);
                   break;
           }

       } else {
           System.out.println("Deleting " + TABLE_NAME);
           deleteTable(db);
       }

    }

    @Override
    public void deleteTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
