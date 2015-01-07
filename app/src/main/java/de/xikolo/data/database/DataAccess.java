package de.xikolo.data.database;

import android.database.sqlite.SQLiteDatabase;

public abstract class DataAccess {

    protected DatabaseHelper databaseHelper;

    protected SQLiteDatabase database;

    public DataAccess(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.database = databaseHelper.getDatabase();
    }

}
