package com.score.senzors.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import com.score.senzors.pojos.User;

import java.util.ArrayList;

/**
 * Utility class to deal with Phone book
 *      1. Read all contacts
 *      2. Read contact name from phone no
 */
public class PhoneBookUtils {

    /**
     * Read contact name from phone no
     * @param context application context
     * @param phoneNumber phone no
     * @return contact name
     */
    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    /**
     * Read all contacts from contact database, we read
     *      1. name
     *      2. phone no
     * @param context application context
     * @return contact list
     */
    public static ArrayList<User> readContacts(Context context) {
        ArrayList<User> contactList = new ArrayList<User>();

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PHONE_CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
                if (hasPhoneNumber > 0) {
                    // read name nad contact_id
                    String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                    String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));

                    // query and loop for every phone number of the contact
                    String phoneNumber = "";
                    Cursor phoneCursor = contentResolver.query(PHONE_CONTENT_URI, null, PHONE_CONTACT_ID + " = ?", new String[] { contact_id }, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                    }
                    phoneCursor.close();

                    contactList.add(new User(contact_id, PhoneNumberUtils.formatNumber(phoneNumber).replace(" ", ""), name.toLowerCase(), "password"));
                }
            }
        }

        cursor.close();

        return contactList;
    }

}
