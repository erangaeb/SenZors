package com.score.senzors.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.score.senzors.R;
import com.score.senzors.exceptions.NoUserException;
import com.score.senzors.pojos.User;

/**
 * Utility class to deal with Share Preferences
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class PreferenceUtils {

    /**
     * Save user credentials in shared preference
     * @param context application context
     * @param user logged-in user
     */
    public static void saveUser(Context context, User user) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  preferences.edit();

        //keys should be constants as well, or derived from a constant prefix in a loop.
        editor.putString("username", user.getUsername());
        editor.putString("password", user.getPassword());
        editor.putString("email", user.getEmail());
        editor.commit();
    }

    /**
     * Get user details from shared preference
     * @param context application context
     * @return user object
     */
    public static User getUser(Context context) throws NoUserException {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        String email = preferences.getString("email", "");

        if(username.isEmpty() || password.isEmpty())
            throw new NoUserException();

        return new User(username, password, email);
    }

}