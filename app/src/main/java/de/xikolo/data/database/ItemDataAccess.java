package de.xikolo.data.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.data.entities.Item;

public class ItemDataAccess extends DataAccess {

    public ItemDataAccess(DatabaseHelper databaseHelper) {
        super(databaseHelper);
    }

    public void addItem(String moduleId, Item item) {
        openDatabase().insert(ItemTable.TABLE_NAME, null, buildContentValues(moduleId, item));

        closeDatabase();
    }

    public void addOrUpdateItem(String moduleId, Item item) {
        if (updateItem(moduleId, item) < 1) {
            addItem(moduleId, item);
        }
    }

    public Item getItem(String id) {
        Cursor cursor = openDatabase().query(
                ItemTable.TABLE_NAME,
                new String[]{
                        ItemTable.COLUMN_ID,
                        ItemTable.COLUMN_POSITION,
                        ItemTable.COLUMN_TITLE,
                        ItemTable.COLUMN_TYPE,
                        ItemTable.COLUMN_AVAILABLE_FROM,
                        ItemTable.COLUMN_AVAILABLE_TO,
                        ItemTable.COLUMN_EXERCISE_TYPE,
                        ItemTable.COLUMN_LOCKED,
                        ItemTable.COLUMN_VISITED,
                        ItemTable.COLUMN_COMPLETED,
                },
                ItemTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(id)}, null, null, null, null);

        Item item = null;
        if (cursor.moveToFirst()) {
            item = buildItem(cursor);
        }
        cursor.close();
        closeDatabase();

        return item;
    }

    public List<Item> getAllItems() {
        List<Item> itemList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + ItemTable.TABLE_NAME;

        Cursor cursor = openDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Item item = buildItem(cursor);
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        closeDatabase();

        return itemList;
    }

    public List<Item> getAllItemsForModule(String moduleId) {
        List<Item> itemList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + ItemTable.TABLE_NAME + " WHERE " + ItemTable.COLUMN_MODULE_ID + " = \'" + moduleId + "\'";

        Cursor cursor = openDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Item item = buildItem(cursor);
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        closeDatabase();

        return itemList;
    }

    private Item buildItem(Cursor cursor) {
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

        return item;
    }

    private ContentValues buildContentValues(String moduleId, Item item) {
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
        values.put(ItemTable.COLUMN_MODULE_ID, moduleId);

        return values;
    }

    public int getItemsCount() {
        String countQuery = "SELECT * FROM " + ItemTable.TABLE_NAME;
        Cursor cursor = openDatabase().rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();
        closeDatabase();

        return count;
    }

    public int updateItem(String moduleId, Item item) {
        int affected = openDatabase().update(
                ItemTable.TABLE_NAME,
                buildContentValues(moduleId, item),
                ItemTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(item.id)});

        closeDatabase();

        return affected;
    }

    public void deleteItem(Item item) {
        openDatabase().delete(
                ItemTable.TABLE_NAME,
                ItemTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(item.id)});

        closeDatabase();
    }

}
