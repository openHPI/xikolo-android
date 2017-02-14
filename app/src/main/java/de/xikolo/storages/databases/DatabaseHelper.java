package de.xikolo.storages.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.storages.databases.adapters.DataAdapter;
import de.xikolo.storages.databases.adapters.ItemDataAdapter;
import de.xikolo.storages.databases.adapters.ModuleDataAdapter;
import de.xikolo.storages.databases.adapters.VideoDataAdapter;
import de.xikolo.storages.databases.tables.ItemTable;
import de.xikolo.storages.databases.tables.ModuleTable;
import de.xikolo.storages.databases.tables.ProgressTable;
import de.xikolo.storages.databases.tables.Table;
import de.xikolo.storages.databases.tables.VideoTable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_NAME = "xikolo";

    private SQLiteDatabase db;

    private List<Table> tables;

    private int openCounter;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        tables = new ArrayList<>();
        tables.add(new ProgressTable());
        tables.add(new ModuleTable());
        tables.add(new ItemTable());
        tables.add(new VideoTable());

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
        if(openCounter == 1) {
            db = getWritableDatabase();
        }
        return this.db;
    }

    @Override
    public synchronized void close() {
        if (openCounter > 0) {
            openCounter--;
        }
        if(openCounter == 0) {
            super.close();
        }
    }

    public void deleteDatabase() {
        for (Table table : tables) {
            table.deleteTable(openDatabase());
        }
    }

    public DataAdapter getDataAdapter(DataType type) {
        DataAdapter dataAdapter = null;

        switch (type) {
            case MODULE:
                dataAdapter = new ModuleDataAdapter(this, new ModuleTable(), new ProgressTable());
                break;
            case ITEM:
                dataAdapter = new ItemDataAdapter(this, new ItemTable());
                break;
            case VIDEO:
                dataAdapter = new VideoDataAdapter(this, new VideoTable());
                break;
        }

        return dataAdapter;
    }

}
