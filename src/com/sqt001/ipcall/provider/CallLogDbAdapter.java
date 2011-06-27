package com.sqt001.ipcall.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple logs database access helper class.
 */
public class CallLogDbAdapter {
    public static final String KEY_NAME = "name";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_TIME = "time";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "logDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table logs (_id integer primary key autoincrement, "
        + "name text not null, number text not null,  time text not null);";

    private static final String DATABASE_NAME = "call_log_db";
    private static final String DATABASE_TABLE = "logs";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS logs");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public CallLogDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the logs database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public CallLogDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new log using the title and body provided. If the log is
     * successfully created return the new rowId for that log, otherwise return
     * a -1 to indicate failure.
     *
     * @param title the title of the log
     * @param body the body of the log
     * @return rowId or -1 if failed
     */
    public long createlog(CallLogEntry entry) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, entry.getName());
        initialValues.put(KEY_NUMBER, entry.getNumber());
        initialValues.put(KEY_TIME, entry.getTime());

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the log with the given rowId
     *
     * @param rowId id of log to delete
     * @return true if deleted, false otherwise
     */
    public boolean deletelog(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean deleteAllLogs() {
        return mDb.delete(DATABASE_TABLE, "1", null) > 0;
    }

    /**
     * Return a Cursor over the list of all logs in the database
     *
     * @return Cursor over all logs
     */
    public Cursor fetchAlllogs() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME, KEY_NUMBER,
                KEY_TIME}, null, null, null, null, KEY_ROWID + " DESC");
    }

    /**
     * Return a Cursor positioned at the log that matches the given rowId
     *
     * @param rowId id of log to retrieve
     * @return Cursor positioned to matching log, if found
     * @throws SQLException if log could not be found/retrieved
     */
    public Cursor fetchlog(long rowId) throws SQLException {
        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_NAME, KEY_NUMBER, KEY_TIME}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        return mCursor;
    }

    /**
     * Update the log using the details provided. The log to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of log to update
     * @param title value to set log title to
     * @param body value to set log body to
     * @return true if the log was successfully updated, false otherwise
     */
    public boolean updatelog(long rowId, CallLogEntry entry) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, entry.getName());
        args.put(KEY_NUMBER, entry.getNumber());
        args.put(KEY_TIME, entry.getTime());
       
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}