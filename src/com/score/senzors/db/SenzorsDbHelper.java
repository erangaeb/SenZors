package com.score.senzors.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Perform creating tables here
 *
 * @author erangaeb@gmail.com(eranga herath)
 */
public class SenzorsDbHelper extends SQLiteOpenHelper {

    // we use singleton database
    private static SenzorsDbHelper senzorsDbHelper;

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Senzors.db";

    // data types, keywords and queries
    private static final String TEXT_TYPE = " TEXT";
    private static final String SMALLINT_TYPE = " SMALLINT";
    private static final String SQL_CREATE_SENSOR =
            "CREATE TABLE " + SenzorsDbContract.Sensor.TABLE_NAME + " (" +
                    SenzorsDbContract.Sensor._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                    SenzorsDbContract.Sensor.COLUMN_NAME_NAME + TEXT_TYPE + "," +
                    SenzorsDbContract.Sensor.COLUMN_NAME_VALUE + TEXT_TYPE + "," +
                    SenzorsDbContract.Sensor.COLUMN_NAME_IS_MINE + SMALLINT_TYPE + "," +
                    SenzorsDbContract.Sensor.COLUMN_NAME_USER + " INTEGER NOT NULL" + "," +
                    "FOREIGN KEY" + "(" + SenzorsDbContract.Sensor.COLUMN_NAME_USER + ") " +
                    "REFERENCES "+ SenzorsDbContract.User.TABLE_NAME + "(" + SenzorsDbContract.User._ID + ")" +
            " )";
    private static final String SQL_CREATE_USER =
            "CREATE TABLE " + SenzorsDbContract.User.TABLE_NAME + " (" +
                    SenzorsDbContract.Sensor._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                    SenzorsDbContract.User.COLUMN_NAME_USERNAME + TEXT_TYPE + "UNIQUE NOT NULL" + "," +
                    SenzorsDbContract.User.COLUMN_NAME_EMAIL + TEXT_TYPE +
                    " )";
    private static final String SQL_DELETE_SENSOR =
            "DROP TABLE IF EXISTS " + SenzorsDbContract.Sensor.TABLE_NAME;
    private static final String SQL_DELETE_USER =
            "DROP TABLE IF EXISTS " + SenzorsDbContract.User.TABLE_NAME;

    /**
     * Init context
     * Init database
     * @param context application context
     */
    public SenzorsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * We are reusing one database instance in all over the app for better memory usage
     * @param context application context
     * @return db helper instance
     */
    synchronized static SenzorsDbHelper getInstance(Context context) {
        if (senzorsDbHelper == null) {
            senzorsDbHelper = new SenzorsDbHelper(context.getApplicationContext());
        }
        return (senzorsDbHelper);
    }

    /**
     * {@inheritDoc}
     */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SENSOR);
        db.execSQL(SQL_CREATE_USER);
    }

    /**
     * {@inheritDoc}
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_SENSOR);
        db.execSQL(SQL_DELETE_USER);
        onCreate(db);
    }

    /**
     * {@inheritDoc}
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
