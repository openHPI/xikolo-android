package de.xikolo.data.database;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

interface Table {

    String COLUMN_ID = BaseColumns._ID;

    void onCreate(SQLiteDatabase db);

    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    void deleteTable(SQLiteDatabase db);

}
