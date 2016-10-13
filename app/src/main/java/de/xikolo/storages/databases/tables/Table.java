package de.xikolo.storages.databases.tables;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public abstract class Table {

    public static String COLUMN_ID = BaseColumns._ID;

    public abstract String getTableName();

    public abstract String getTableCreate();

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getTableCreate());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int upgradeTo = oldVersion + 1;

        while (upgradeTo <= newVersion) {

            switch (upgradeTo) {
                default:
                    System.out.println("Deleting " + getTableName());
                    deleteTable(db);
                    break;
            }
            upgradeTo++;

        }
    }

    /**
     * Keep database and migrate tables in subclasses.
     *
     * @param db The SQLite Database
     * @param version The upgrade version
     */
    protected void upgradeTo(SQLiteDatabase db, int version) {}

    public void deleteTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + getTableName());
        onCreate(db);
    }

}
