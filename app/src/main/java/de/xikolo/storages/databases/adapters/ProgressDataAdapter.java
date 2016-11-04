package de.xikolo.storages.databases.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import de.xikolo.models.Progress;
import de.xikolo.storages.databases.DatabaseHelper;
import de.xikolo.storages.databases.tables.ProgressTable;
import de.xikolo.storages.databases.tables.Table;

class ProgressDataAdapter extends DataAdapter<Progress> {

    public ProgressDataAdapter(DatabaseHelper databaseHelper, Table table) {
        super(databaseHelper, table);
    }

    @Override
    protected Progress buildEntity(Cursor cursor) {
        Progress progress = new Progress();

        progress.id = cursor.getString(0);
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

    @Override
    protected ContentValues buildContentValues(Progress progress) {
        ContentValues values = new ContentValues();

        values.put(ProgressTable.COLUMN_ID, progress.id);
        values.put(ProgressTable.COLUMN_ITEM_COUNT_AVAILABLE, progress.items.count_available);
        values.put(ProgressTable.COLUMN_ITEM_COUNT_VISITED, progress.items.count_visited);
        values.put(ProgressTable.COLUMN_ITEM_COUNT_COMPLETED, progress.items.count_completed);
        values.put(ProgressTable.COLUMN_SELF_TESTS_COUNT_AVAILABLE, progress.self_tests.count_available);
        values.put(ProgressTable.COLUMN_SELF_TESTS_COUNT_TAKEN, progress.self_tests.count_taken);
        values.put(ProgressTable.COLUMN_SELF_TESTS_POINTS_POSSIBLE, progress.self_tests.points_possible);
        values.put(ProgressTable.COLUMN_SELF_TESTS_POINTS_SCORED, progress.self_tests.points_scored);
        values.put(ProgressTable.COLUMN_ASSIGNMENTS_COUNT_AVAILABLE, progress.assignments.count_available);
        values.put(ProgressTable.COLUMN_ASSIGNMENTS_COUNT_TAKEN, progress.assignments.count_taken);
        values.put(ProgressTable.COLUMN_ASSIGNMENTS_POINTS_POSSIBLE, progress.assignments.points_possible);
        values.put(ProgressTable.COLUMN_ASSIGNMENTS_POINTS_SCORED, progress.assignments.points_scored);

        return values;
    }

}
