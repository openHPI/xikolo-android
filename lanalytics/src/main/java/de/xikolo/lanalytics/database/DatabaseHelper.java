package de.xikolo.lanalytics.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.lanalytics.database.access.DataAccess;
import de.xikolo.lanalytics.database.access.EventDataAccess;
import de.xikolo.lanalytics.database.tables.EventTable;
import de.xikolo.lanalytics.database.tables.Table;

@SuppressWarnings("unused")
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "lanalytics";

    private SQLiteDatabase db;

    private List<Table> tables;

    private int openCounter;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        tables = new ArrayList<>();
        tables.add(new EventTable());

        openCounter = 0;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Table table : tables) {
            table.onCreate(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        for (Table table : tables) {
            table.onUpgrade(db, oldVersion, newVersion);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    public synchronized SQLiteDatabase openDatabase() {
        openCounter++;
        if (openCounter == 1) {
            db = getWritableDatabase();
        }
        return this.db;
    }

    @Override
    public synchronized void close() {
        if (openCounter > 0) {
            openCounter--;
        }
        if (openCounter == 0) {
            super.close();
        }
    }

    public void deleteDatabase() {
        for (Table table : tables) {
            table.deleteTable(openDatabase());
        }
    }

    public enum DataAccessType {
        EVENT
    }

    public DataAccess getDataAccess(DataAccessType type) {
        DataAccess dataAccess = null;

        switch (type) {
            case EVENT:
                dataAccess = new EventDataAccess(this, new EventTable());
                break;
        }

        return dataAccess;
    }

}
