package de.xikolo.data.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.data.entities.OverallProgress;

class OverallProgressDataAccess extends DataAccess {

    public OverallProgressDataAccess(DatabaseHelper databaseHelper) {
        super(databaseHelper);
    }

    public void addProgress(String id, OverallProgress progress) {
        getDatabase().insert(OverallProgressTable.TABLE_NAME, null, buildContentValues(id, progress));
    }

    public void addOrUpdateProgress(String id, OverallProgress progress) {
        if (updateProgress(id, progress) < 1) {
            addProgress(id, progress);
        }
    }

    public OverallProgress getProgress(String id) {
        Cursor cursor = getDatabase().query(
                OverallProgressTable.TABLE_NAME,
                new String[]{
                        OverallProgressTable.COLUMN_ID,
                        OverallProgressTable.COLUMN_ITEM_COUNT_AVAILABLE,
                        OverallProgressTable.COLUMN_ITEM_COUNT_VISITED,
                        OverallProgressTable.COLUMN_ITEM_COUNT_COMPLETED,
                        OverallProgressTable.COLUMN_SELF_TESTS_COUNT_AVAILABLE,
                        OverallProgressTable.COLUMN_SELF_TESTS_COUNT_TAKEN,
                        OverallProgressTable.COLUMN_SELF_TESTS_POINTS_POSSIBLE,
                        OverallProgressTable.COLUMN_SELF_TESTS_POINTS_SCORED,
                        OverallProgressTable.COLUMN_ASSIGNMENTS_COUNT_AVAILABLE,
                        OverallProgressTable.COLUMN_ASSIGNMENTS_COUNT_TAKEN,
                        OverallProgressTable.COLUMN_ASSIGNMENTS_POINTS_POSSIBLE,
                        OverallProgressTable.COLUMN_ASSIGNMENTS_POINTS_SCORED,
                },
                OverallProgressTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(id)}, null, null, null, null);

        cursor.moveToFirst();
        OverallProgress progress = buildProgress(cursor);

        cursor.close();

        return progress;
    }

    public List<OverallProgress> getAllProgress() {
        List<OverallProgress> progressList = new ArrayList<OverallProgress>();

        String selectQuery = "SELECT * FROM " + OverallProgressTable.TABLE_NAME;

        Cursor cursor = getDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                OverallProgress progress = buildProgress(cursor);
                progressList.add(progress);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return progressList;
    }

    private OverallProgress buildProgress(Cursor cursor) {
        OverallProgress progress = new OverallProgress();

        cursor.getString(0);
        progress.items.count_available = cursor.getInt(1);
        progress.items.count_visited = cursor.getInt(2);
        progress.items.count_completed = cursor.getInt(3);
        progress.self_tests.count_available = cursor.getFloat(4);
        progress.self_tests.count_taken = cursor.getFloat(5);
        progress.self_tests.points_possible = cursor.getFloat(6);
        progress.self_tests.points_scored = cursor.getFloat(7);
        progress.assignments.count_available = cursor.getFloat(8);
        progress.assignments.count_taken = cursor.getFloat(9);
        progress.assignments.points_possible = cursor.getFloat(10);
        progress.assignments.points_scored = cursor.getFloat(11);

        return progress;
    }

    private ContentValues buildContentValues(String id, OverallProgress progress) {
        ContentValues values = new ContentValues();
        values.put(OverallProgressTable.COLUMN_ID, id);
        values.put(OverallProgressTable.COLUMN_ITEM_COUNT_AVAILABLE, progress.items.count_available);
        values.put(OverallProgressTable.COLUMN_ITEM_COUNT_VISITED, progress.items.count_visited);
        values.put(OverallProgressTable.COLUMN_ITEM_COUNT_COMPLETED, progress.items.count_completed);
        values.put(OverallProgressTable.COLUMN_SELF_TESTS_COUNT_AVAILABLE, progress.self_tests.count_available);
        values.put(OverallProgressTable.COLUMN_SELF_TESTS_COUNT_TAKEN, progress.self_tests.count_taken);
        values.put(OverallProgressTable.COLUMN_SELF_TESTS_POINTS_POSSIBLE, progress.self_tests.points_possible);
        values.put(OverallProgressTable.COLUMN_SELF_TESTS_POINTS_SCORED, progress.self_tests.points_scored);
        values.put(OverallProgressTable.COLUMN_ASSIGNMENTS_COUNT_AVAILABLE, progress.assignments.count_available);
        values.put(OverallProgressTable.COLUMN_ASSIGNMENTS_COUNT_TAKEN, progress.assignments.count_taken);
        values.put(OverallProgressTable.COLUMN_ASSIGNMENTS_POINTS_POSSIBLE, progress.assignments.points_possible);
        values.put(OverallProgressTable.COLUMN_ASSIGNMENTS_POINTS_SCORED, progress.assignments.points_scored);

        return values;
    }

    public int getProgressCount() {
        String countQuery = "SELECT * FROM " + OverallProgressTable.TABLE_NAME;
        Cursor cursor = getDatabase().rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    public int updateProgress(String id, OverallProgress progress) {
        int affected = getDatabase().update(
                OverallProgressTable.TABLE_NAME,
                buildContentValues(id, progress),
                OverallProgressTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(id)});

        return affected;
    }

    public void deleteProgress(String id) {
        getDatabase().delete(
                OverallProgressTable.TABLE_NAME,
                OverallProgressTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(id)});
    }

}
