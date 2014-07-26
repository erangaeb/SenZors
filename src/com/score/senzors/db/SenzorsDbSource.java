package com.score.senzors.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

        // get matching user if exists
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, SenzorsDbContract.User.COLUMN_NAME_USERNAME + "=?", // constraint
                new String[]{username}, // prams
                null, // order by
                null, // group by
                null); // join

        if(cursor.moveToFirst()) {
            // have matching user
            // so get user data
            // we return id as password since we no storing users password in database
            String id = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            String _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));
            String _email = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_EMAIL));

            // clear
            cursor.close();
            db.close();

            Log.d(TAG, "GetOrCreateUser: have user, so return it: " + username);
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

            Log.d(TAG, "GetOrCreateUser: no user, so user created:" + username);
            return new User(Long.toString(id), username, email, "");
        }
    }

    /**
     * Add sensor to the database
     * @param sensor sensor object
     */
    public void addSensor(Sensor sensor) {
        Log.d(TAG, "AddSensor: adding sensor - " + sensor.getSensorName());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Sensor.COLUMN_NAME_NAME, sensor.getSensorName());
        values.put(SenzorsDbContract.Sensor.COLUMN_NAME_IS_MINE, sensor.isMySensor()? 1 : 0);
        values.put(SenzorsDbContract.Sensor.COLUMN_NAME_USER, sensor.getUser().getId());

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Sensor.TABLE_NAME, SenzorsDbContract.Sensor.COLUMN_NAME_VALUE, values);
        db.close();
    }

    /**
     * Delete sensor from database,
     * In here we actually delete all the matching sensors of given user
     * @param sensor sensor
     */
    public void deleteSensorOfUser(Sensor sensor) {
        Log.d(TAG, "deleteSensor: deleting sensor - " + sensor.getSensorName());
        Log.d(TAG, "deleteSensor: deleting sensor - " + sensor.getUser().getUsername());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete sensor matching sensor of given user
        db.delete(SenzorsDbContract.Sensor.TABLE_NAME,
                SenzorsDbContract.Sensor.COLUMN_NAME_USER + "=?" + " AND " +
                SenzorsDbContract.Sensor.COLUMN_NAME_NAME + "=?",
                new String[]{sensor.getUser().getId(), sensor.getSensorName()});
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

        // get matching data via JOIN query
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();
        String query = "SELECT * " +
                "FROM sensor JOIN user " +
                "ON sensor.user = user._id " +
                "WHERE sensor.is_mine=?";
        Cursor cursor = db.rawQuery(query, new String[]{mySensors ? "1" : "0"});

        // sensor/user attributes
        String sensorId;
        String sensorName;
        String sensorValue;
        boolean isMySensor;
        String userId;
        String username;
        String email;
        User user;
        Sensor sensor;

        // extract attributes
        while (cursor.moveToNext()) {
            // get sensor attributes
            sensorId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Sensor._ID));
            sensorName = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Sensor.COLUMN_NAME_NAME));
            sensorValue = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Sensor.COLUMN_NAME_VALUE));
            isMySensor = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Sensor.COLUMN_NAME_IS_MINE)) == 1;

            // get user attributes
            userId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));
            email = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_EMAIL));

            // save to list
            user = new User(userId, username, email, "password");
            ArrayList<User> sharedUsers = getSharedUsers(sensorId, db);
            sensor = new Sensor(sensorId, sensorName, sensorValue, isMySensor, user, sharedUsers);
            sensorList.add(sensor);

            Log.d(TAG, "GetSensors: sensor name - " + sensor.getSensorName());
            Log.d(TAG, "GetSensors: is my sensor - " + sensor.isMySensor());
            Log.d(TAG, "GetSensors: user - " + user.getUsername());
        }

        // clean
        cursor.close();
        db.close();

        Log.d(TAG, "GetSensors: sensor count " + sensorList.size());
        return sensorList;
    }

    /**
     * Get shared users of given sensor
     * @param sensorId sensor id
     * @param db database
     * @return user list
     */
    private ArrayList<User> getSharedUsers(String sensorId, SQLiteDatabase db) {
        Log.d(TAG, "GetSharedUsers: getting shares users");
        ArrayList<User> userList = new ArrayList<User>();

        String query = "SELECT * " +
                "FROM shared_user JOIN user " +
                "ON shared_user.user = user._id " +
                "WHERE shared_user.sensor=?";
        Cursor cursor = db.rawQuery(query, new String[]{sensorId});

        // user attributes
        String userId;
        String username;
        String email;
        User user;

        // extract attributes
        while (cursor.moveToNext()) {
            // get user attributes
            userId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));
            email = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_EMAIL));

            // save to list
            user = new User(userId, username, email, "password");
            userList.add(user);
            Log.d(TAG, "GetSharedUsers: user - " + user.getUsername());
        }

        // clean cursor
        cursor.close();

        Log.d(TAG, "GetSharedUsers: user count - " + userList.size());
        return userList;
    }

    /**
     * Add shared user to db when share sensor with user
     * @param sensor sensor
     * @param user user
     */
    public void addSharedUser(Sensor sensor, User user) {
        Log.d(TAG, "AddSharedUser: add data - " + sensor.getSensorName() + " " + user.getUsername());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.SharedUser.COLUMN_NAME_USER, user.getId());
        values.put(SenzorsDbContract.SharedUser.COLUMN_NAME_SENSOR, sensor.getId());

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.SharedUser.TABLE_NAME, SenzorsDbContract.SharedUser.COLUMN_NAME_USER, values);
        db.close();
    }

    /**
     * Delete shared users from database,
     * Sensor sharing details keeps on SharedUser table, so delete that
     * sharing entries from here
     * @param user user
     */
    public void deleteSharedUser(User user) {
        Log.d(TAG, "DeleteSharedUser: deleting shared user - " + user.getUsername());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // delete shared user
        db.delete(SenzorsDbContract.SharedUser.TABLE_NAME,
                SenzorsDbContract.SharedUser.COLUMN_NAME_USER + "=?",
                new String[]{user.getId()});
        db.close();
    }
}
