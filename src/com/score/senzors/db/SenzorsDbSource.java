package com.score.senzors.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import com.score.senzors.pojos.Sensor;
import com.score.senzors.pojos.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Do all database insertions, updated, deletions from here
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzorsDbSource {

    private static final String TAG = SenzorsDbSource.class.getName();
    private static Context context;

    /**
     * Init db helper
     * @param context application context
     */
    public SenzorsDbSource(Context context) {
        Log.d(TAG, "Init: db source");
        this.context = context;
    }

    /**
     * Insert user to database
     * @param user user
     */
    public void addUser(User user) {
        Log.d(TAG, "AddUser: adding user - " + user.getUsername());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.User.COLUMN_NAME_USERNAME, user.getUsername());
        values.put(SenzorsDbContract.User.COLUMN_NAME_EMAIL, user.getEmail());

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.User.TABLE_NAME, SenzorsDbContract.User.COLUMN_NAME_EMAIL, values);
        db.close();
    }

    /**
     * Get user if exists in the database, other wise create user and return
     * @param username username
     * @param email email address
     * @return user
     */
    public User getOrCreateUser(String username, String email) {
        Log.d(TAG, "GetOrCreateUser: " + username);
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // get matching user if exists
        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, SenzorsDbContract.User.COLUMN_NAME_USERNAME + "=?", // constraint
                new String[]{username}, // prams
                null, // order by
                null, // group by
                null); // join

        if(cursor.moveToNext()) {
            // have matching user
            cursor.moveToFirst();

            // so get user data
            // we return id as password since we no storing users password in database
            String id = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            String _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));
            String _email = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_EMAIL));

            // clear
            cursor.close();
            db.close();

            Log.d(TAG, "GetOrCreateUser: no user, so user created:" + _username);
            return new User(id, _username, _email, "password");
        } else {
            // no matching user
            // so create user
            ContentValues values = new ContentValues();
            values.put(SenzorsDbContract.User.COLUMN_NAME_USERNAME, username);
            values.put(SenzorsDbContract.User.COLUMN_NAME_EMAIL, email);

            // inset data
            long id = db.insert(SenzorsDbContract.User.TABLE_NAME, SenzorsDbContract.User.COLUMN_NAME_EMAIL, values);
            db.close();

            Log.d(TAG, "GetOrCreateUser: have user, so return it: " + username);
            return new User(Long.toString(id), username, email, "");
        }
    }

    /**
     * Add sensor to the database
     * @param sensor sensor object
     * @param user sensor user
     */
    public void addSensor(Sensor sensor, User user) {
        Log.d(TAG, "AddSensor: adding sensor - " + sensor.getSensorName());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Sensor.COLUMN_NAME_NAME, sensor.getSensorName());
        values.put(SenzorsDbContract.Sensor.COLUMN_NAME_IS_MINE, sensor.isMySensor()? 1 : 0);
        values.put(SenzorsDbContract.Sensor.COLUMN_NAME_USER, user.getId());

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Sensor.TABLE_NAME, SenzorsDbContract.Sensor.COLUMN_NAME_VALUE, values);
        db.close();
    }

    /**
     * Get all sensors, two types of sensors here
     *  1. my sensors
     *  2. friends sensors
     * @param mySensors sensor type
     * @return sensor list
     */
    public List<Sensor> getSensors(boolean mySensors) {
        Log.d(TAG, "GetSensors: getting all sensor");
        List<Sensor> sensorList = new ArrayList<Sensor>();

        // select query with args
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();

        /*Cursor cursor = db.query(SenzorsDbContract.Sensor.TABLE_NAME, // table
                null, SenzorsDbContract.Sensor.COLUMN_NAME_IS_MINE + "=?", // constraint
                new String[]{mySensors ? "1" : "0"}, // prams
                null, // order by
                null, // group by
                null); // join*/

        // join query via query builder
        /*SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SenzorsDbContract.Sensor.TABLE_NAME+
                " LEFT OUTER JOIN " + SenzorsDbContract.User.TABLE_NAME + " ON " +
                SenzorsDbContract.Sensor.COLUMN_NAME_USER + " = " + SenzorsDbContract.User._ID);*/
        /*Cursor cursor = queryBuilder.query(db, // database
                null,
                SenzorsDbContract.Sensor.COLUMN_NAME_IS_MINE + "=?", // constraint
                new String[]{mySensors ? "1" : "0"}, // prams
                null, // group by
                null, // having
                null); // order by*/

        // get matching data via JOIN query
        String query = "SELECT sensor._id, sensor.name, sensor.value, sensor.is_mine, user.name " +
                "FROM sensor JOIN user " +
                "ON sensor.user = user._id " +
                "WHERE sensor.is_mine=?";
        Cursor cursor = db.rawQuery(query, new String[]{mySensors ? "1" : "0"});

        // sensor attributes
        String id;
        String sensorName;
        String sensorValue;
        boolean isMySensor;
        String user;

        // extract attributes
        while (cursor.moveToNext()) {
            id = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Sensor._ID));
            sensorName = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Sensor.COLUMN_NAME_NAME));
            sensorValue = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Sensor.COLUMN_NAME_VALUE));
            isMySensor = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Sensor.COLUMN_NAME_IS_MINE)) == 1;
            user = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));
            Log.d(TAG, "GetSensors: sensor name - " + sensorName);
            //Log.d(TAG, "GetSensors: sensor value - " + sensorValue);
            Log.d(TAG, "GetSensors: is my sensor - " + isMySensor);
            Log.d(TAG, "GetSensors: user - " + user);
            sensorList.add(new Sensor(id, user, sensorName, sensorValue, isMySensor, false));
        }

        // clean
        cursor.close();
        db.close();

        Log.d(TAG, "GetSensors: sensor count " + sensorList.size());
        return sensorList;
    }

}
