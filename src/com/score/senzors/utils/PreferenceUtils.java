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
        editor.putString("id", user.getId());
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
        String id = preferences.getString("id", "0");
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        String email = preferences.getString("email", "");

        if(username.isEmpty() || password.isEmpty())
            throw new NoUserException();

        return new User(id, username, email, password);
    }

    /**
     * Get first time status from shared preference
     * @param context application context
     * @return true/false
     */
    public static boolean isFirstTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getBoolean("is_first_time", true);
    }

    /**
     * Save first time status on shared preference
     * @param context application context
     * @param isFirstTime first time status
     */
    public static void setFirstTime(Context context, boolean isFirstTime) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  preferences.edit();
        editor.putBoolean("is_first_time", isFirstTime);
        editor.commit();
    }

    /**
     * Save public/private keys in shared preference,
     * @param context application context
     * @param key public/private keys(encoded key string)
     * @param keyType public_key, private_key, server_key
     */
    public static void saveRsaKey(Context context, String key, String keyType) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  preferences.edit();
        editor.putString(keyType, key);
        editor.commit();
    }

    /**
     * Get saved RSA key string from shared preference
     * @param context application context
     * @param keyType public_key, private_key, server_key
     * @return key string
     */
    public static String getRsaKey(Context context, String keyType) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getString(keyType, "");
    }

}
