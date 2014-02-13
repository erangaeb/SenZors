package com.score.senzors.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

    // db helper
    SenzorsDbHelper senzorsDbHelper;

    /**
     * Init db helper
     * @param context application context
     */
    public SenzorsDbSource(Context context) {
        senzorsDbHelper = SenzorsDbHelper.getInstance(context);
    }

    /**
     * Clean all database connections
     */
    public void close() {
        //close DB helper
        senzorsDbHelper.close();
    }

    /**
     * Insert user to database
     * @param user user
     */
    public void addUser(User user) {
        SQLiteDatabase db = senzorsDbHelper.getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.User.COLUMN_NAME_USERNAME, user.getUsername());
        values.put(SenzorsDbContract.User.COLUMN_NAME_EMAIL, user.getEmail());

        // Insert the new row, if fails throw an error
        db.insert(SenzorsDbContract.User.TABLE_NAME, SenzorsDbContract.User.COLUMN_NAME_EMAIL, values);
        db.close();
    }

    /**
     * Add sensor to the database
     * @param sensor sensor object
     */
    public void addSensor(Sensor sensor) {
        SQLiteDatabase db = senzorsDbHelper.getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Sensor.COLUMN_NAME_NAME, sensor.getSensorName());
        values.put(SenzorsDbContract.Sensor.COLUMN_NAME_IS_MINE, sensor.isMySensor()? 1 : 0);
        values.put(SenzorsDbContract.Sensor.COLUMN_NAME_USER, sensor.getSensorName());

        // Insert the new row, if fails throw an error
        db.insert(SenzorsDbContract.User.TABLE_NAME, SenzorsDbContract.Sensor.COLUMN_NAME_VALUE, values);
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
        List<Sensor> sensorList = new ArrayList<Sensor>();

        // Select All Query
        // we execute row query here
        String selectQuery = "SELECT  * FROM " + SenzorsDbContract.Sensor.TABLE_NAME;
        SQLiteDatabase db = senzorsDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // sensor attributes
        String sensorName;
        String sensorValue;
        boolean isMySensor;
        String user;

        // extract attributes
        while (cursor.moveToNext()) {
            sensorName = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Sensor.COLUMN_NAME_NAME));
            sensorValue = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Sensor.COLUMN_NAME_VALUE));
            isMySensor = cursor.getInt(cursor.getColumnIndex(SenzorsDbContract.Sensor.COLUMN_NAME_IS_MINE)) == 1;
            user = "user";
            sensorList.add(new Sensor(user, sensorName, sensorValue, isMySensor, true));
        }

        return sensorList;
    }
}
