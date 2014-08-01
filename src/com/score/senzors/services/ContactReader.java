package com.score.senzors.services;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.listeners.ContactReaderListener;
import com.score.senzors.pojos.User;

import java.util.ArrayList;

/**
 * Read contact from contact database
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class ContactReader extends AsyncTask<String, String, String > {

    SenzorApplication application;
    ContactReaderListener listener;
    ArrayList<User> contactList;

    public ContactReader(SenzorApplication application) {
        this.application = application;
        listener = application;
        contactList = new ArrayList<User>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String doInBackground(String... params) {
        readContacts();

        return "READ";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(String status) {
        super.onPostExecute(status);

        listener.onPostReadContacts(this.contactList);
    }

    /**
     * Read contacts from contact database, we read
     *      1. name
     *      2. phone no
     */
    private void readContacts() {
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PHONE_CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = application.getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

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

                    System.out.println(name);
                    System.out.println(phoneNumber);
                    this.contactList.add(new User(contact_id, PhoneNumberUtils.formatNumber(phoneNumber), name, "password"));
                }
            }
        }

        cursor.close();
    }
}
