package com.score.senzors.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import com.score.senzors.R;
import com.score.senzors.exceptions.NoUserException;
import com.score.senzors.pojos.User;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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
     * Save public/private keys in shared preference
     * Actually convert key to string and save it in shared preference
     *
     * @param context application context
     * @param key public/private keys
     * @param keyType public_key or private_key
     */
    public static void saveRsaKey(Context context, Key key, String keyType) {
        // get bytes from key
        String keyString =  Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);

        System.out.println("---------------------------------------");
        System.out.println(keyType);
        System.out.println(keyString);
        System.out.println("---------------------------------------");

        // save key string
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  preferences.edit();
        editor.putString(keyType, keyString);
        editor.commit();
    }

    /**
     * Get RSA keys from shared preference
     * @param context application context
     * @param keyType public_key or private_key
     * @return public key/ private key
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static Key getRasKey(Context context, String keyType) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // get key from shared preference
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String keyString = preferences.getString(keyType, "");

        if (keyType.equalsIgnoreCase("public_key")) {
            // public key
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(keyString, Base64.DEFAULT));
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(spec);
        } else {
            // private key
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.decode(keyString, Base64.DEFAULT));
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePrivate(spec);
        }
    }

    /**
     * Public private keys stored in shared preference,
     * Its a Base64 encoded string, so just read it and return
     * @param context application context
     * @param keyType public_key or private_key
     * @return encoded key string
     */
    public static String getEncodedRsaKey(Context context, String keyType) {
        // get key from shared preference
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String keyString = preferences.getString(keyType, "");

        return keyString;
    }

    /**
     * Get status of RSA key saved
     * @param context application context
     * @return true/false
     */
    public static boolean isRasKeysSaved(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getBoolean("is_saved", true);
    }

    /**
     * Save rsa key saved status
     * @param context application context
     * @param isSaved first time status
     */
    public static void setRsaKeySavedStatus(Context context, boolean isSaved) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  preferences.edit();
        editor.putBoolean("is_saved", isSaved);
        editor.commit();
    }
}
