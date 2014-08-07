package com.score.senzors.services;

import android.os.AsyncTask;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.listeners.ContactReaderListener;
import com.score.senzors.pojos.User;
import com.score.senzors.utils.PhoneBookUtils;

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String doInBackground(String... params) {
        contactList = PhoneBookUtils.readContacts(application);

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

}
