package de.xikolo.data.database;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public interface Table {

    public static final String COLUMN_ID = BaseColumns._ID;

    public void onCreate(SQLiteDatabase db);

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

}
