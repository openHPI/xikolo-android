package de.xikolo.storages.databases.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.storages.databases.tables.Table;
import de.xikolo.storages.databases.DatabaseHelper;
import de.xikolo.storages.databases.DatabaseModel;

@SuppressWarnings("unused")
public abstract class DataAdapter<E extends DatabaseModel> {

    protected DatabaseHelper databaseHelper;

    protected Table table;

    public DataAdapter(DatabaseHelper databaseHelper, Table table) {
        this.databaseHelper = databaseHelper;
        this.table = table;
    }

    protected SQLiteDatabase openDatabase() {
       return databaseHelper.openDatabase();
    }

    protected void closeDatabase() {
        databaseHelper.close();
    }

    public void add(E entity) {
        openDatabase().insert(table.getTableName(), null, buildContentValues(entity));
        closeDatabase();
    }

    public void addOrUpdate(E entity) {
        if (update(entity) < 1) {
            add(entity);
        }
    }

    public E get(String id) {
        Cursor cursor = openDatabase().query(
                table.getTableName(),
                null,
                Table.COLUMN_ID + " =? ",
                new String[]{String.valueOf(id)}, null, null, null, null);

        E entity = null;
        if (cursor.moveToFirst()) {
            entity = buildEntity(cursor);
        }
        cursor.close();
        closeDatabase();

        return entity;
    }

    public List<E> getAll() {
        return getAll("SELECT * FROM " + table.getTableName());
    }

    protected List<E> getAll(String selectQuery) {
        List<E> list = new ArrayList<>();

        Cursor cursor = openDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                E entity = buildEntity(cursor);
                list.add(entity);
            } while (cursor.moveToNext());
        }

        cursor.close();
        closeDatabase();

        return list;
    }

    protected abstract E buildEntity(Cursor cursor);

    protected abstract ContentValues buildContentValues(E entity);

    public int getCount() {
        return getCount("SELECT * FROM " + table.getTableName());
    }

    public int getCount(String countQuery) {
        Cursor cursor = openDatabase().rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();
        closeDatabase();

        return count;
    }

    public int update(E entity) {
        int affected = openDatabase().update(
                table.getTableName(),
                buildContentValues(entity),
                Table.COLUMN_ID + " =? ",
                new String[]{String.valueOf(entity.getId())});

        closeDatabase();

        return affected;
    }

    public void delete(String id) {
        openDatabase().delete(
                table.getTableName(),
                Table.COLUMN_ID + " =? ",
                new String[]{String.valueOf(id)});

        closeDatabase();
    }

}
