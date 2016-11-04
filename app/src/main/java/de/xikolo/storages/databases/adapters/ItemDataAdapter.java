package de.xikolo.storages.databases.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

import de.xikolo.models.Item;
import de.xikolo.storages.databases.DatabaseHelper;
import de.xikolo.storages.databases.tables.ItemTable;
import de.xikolo.storages.databases.tables.Table;

public class ItemDataAdapter extends DataAdapter<Item> {

    public ItemDataAdapter(DatabaseHelper databaseHelper, Table table) {
        super(databaseHelper, table);
    }

    @Override
    protected Item buildEntity(Cursor cursor) {
        Item item = new Item();

        item.id = cursor.getString(0);
        item.position = cursor.getInt(1);
        item.title = cursor.getString(2);
        item.type = cursor.getString(3);
        item.available_from = cursor.getString(4);
        item.available_to = cursor.getString(5);
        item.exercise_type = cursor.getString(6);
        item.locked = cursor.getInt(7) != 0;
        item.progress.visited = cursor.getInt(8) != 0;
        item.progress.completed = cursor.getInt(9) != 0;
        item.courseId = cursor.getString(10);
        item.moduleId = cursor.getString(11);

        return item;
    }

    @Override
    protected ContentValues buildContentValues(Item item) {
        ContentValues values = new ContentValues();

        values.put(ItemTable.COLUMN_ID, item.id);
        values.put(ItemTable.COLUMN_POSITION, item.position);
        values.put(ItemTable.COLUMN_TITLE, item.title);
        values.put(ItemTable.COLUMN_TYPE, item.type);
        values.put(ItemTable.COLUMN_AVAILABLE_FROM, item.available_from);
        values.put(ItemTable.COLUMN_AVAILABLE_TO, item.available_to);
        values.put(ItemTable.COLUMN_EXERCISE_TYPE, item.exercise_type);
        values.put(ItemTable.COLUMN_LOCKED, item.locked);
        values.put(ItemTable.COLUMN_VISITED, item.progress.visited);
        values.put(ItemTable.COLUMN_COMPLETED, item.progress.completed);
        values.put(ItemTable.COLUMN_COURSE_ID, item.courseId);
        values.put(ItemTable.COLUMN_MODULE_ID, item.moduleId);

        return values;
    }

    public List<Item> getAllForModule(String moduleId) {
        return super.getAll("SELECT * FROM " + ItemTable.TABLE_NAME + " WHERE " + ItemTable.COLUMN_MODULE_ID + " = \'" + moduleId + "\'");
    }

}
