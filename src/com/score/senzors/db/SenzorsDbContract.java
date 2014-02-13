package com.score.senzors.db;

import android.provider.BaseColumns;

/**
 * Keep database table attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzorsDbContract {

    public SenzorsDbContract() {}

    /* Inner class that defines sensor table contents */
    public static abstract class Sensor implements BaseColumns {
        public static final String TABLE_NAME = "sensor";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_IS_MINE = "is_mine";
        public static final String COLUMN_NAME_USER = "user";
    }

    /* Inner class that defines the user table contents */
    public static abstract class User implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_USERNAME = "name";
        public static final String COLUMN_NAME_EMAIL = "email";
    }
}