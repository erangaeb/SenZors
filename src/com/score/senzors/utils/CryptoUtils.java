package com.score.senzors.utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import com.score.senzors.exceptions.RsaKeyException;
import com.score.senzors.exceptions.NoPhoneNoException;

import javax.crypto.Cipher;
import java.security.*;

/**
 * Created by eranga on 6/4/14.
 */
public class CryptoUtils {

    private static final String TAG = CryptoUtils.class.getName();

    String temp;

    public static void initKeys(Context context) {
        try {
            System.out.println(Utils.getPhoneNo(context));
        } catch (NoPhoneNoException e) {
            e.printStackTrace();
        }

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.genKeyPair();

            PreferenceUtils.saveRsaKey(context, kp.getPublic(), "public_key");
            PreferenceUtils.saveRsaKey(context, kp.getPrivate(), "private_key");
            PreferenceUtils.setRsaKeySavedStatus(context, true);

            Log.d(TAG, "RSA keys generated and saved in shared preference");
        } catch (Exception e) {
            Log.e(TAG, "RSA key pair error");
        }
    }

    public void encryptMessage(Context context, String message) {
        try {
            // get private key
            PrivateKey key = (PrivateKey)PreferenceUtils.getRasKey(context, "private_key");

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte []encodedBytes = cipher.doFinal(message.getBytes());
            temp = Base64.encodeToString(encodedBytes, Base64.DEFAULT);

            Log.d(TAG, "Encrypted cipher text - " + temp);
        } catch (Exception e) {
            Log.e(TAG, "RSA encryption error");
        }
    }

    public void decryptMessage(Context context) {
        try {
            // get public key
            PublicKey key = (PublicKey)PreferenceUtils.getRasKey(context, "public_key");

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte []decodedBytes = cipher.doFinal(Base64.decode(temp, Base64.DEFAULT));

            Log.d(TAG, "Decrypted text - " + new String(decodedBytes));
        } catch (Exception e) {
            Log.e(TAG, "RSA decryption error");
        }
    }

    /**
     * Encrypt message with private key
     *
     * @param message message to encrypt
     * @return encrypted message
     */
    public static String getEncryptedMessage(Context context, String message) throws RsaKeyException {
        try {
            // get private key
            PrivateKey key = (PrivateKey)PreferenceUtils.getRasKey(context, "private_key");

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte []encodedBytes = cipher.doFinal(message.getBytes());
            String encryptedMessage = Base64.encodeToString(encodedBytes, Base64.DEFAULT);
            Log.d(TAG, "Encrypted cipher text - " + encryptedMessage);

            return encryptedMessage;
        } catch (Exception e) {
            Log.e(TAG, "RSA encryption error " + e.getMessage());
            e.printStackTrace();

            throw new RsaKeyException();
        }
    }
}
