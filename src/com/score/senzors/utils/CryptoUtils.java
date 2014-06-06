package com.score.senzors.utils;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

/**
 * Created by eranga on 6/4/14.
 */
public class CryptoUtils {

    private static final String TAG = CryptoUtils.class.getName();

    String message = "test";
    Key publicKey;
    Key privateKey;

    byte[] encodedBytes = null;
    byte[] decodedBytes = null;
    String temp;

    public void initKeys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
        } catch (Exception e) {
            Log.e(TAG, "RSA key pair error");
        }
    }

    public void encryptMessage() {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            encodedBytes = cipher.doFinal(message.getBytes());
            temp = Base64.encodeToString(encodedBytes, Base64.DEFAULT);
            System.out.println(temp);
        } catch (Exception e) {
            Log.e(TAG, "RSA encryption error");
        }
    }

    public void decryptMessage() {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            decodedBytes = cipher.doFinal(Base64.decode(temp, Base64.DEFAULT));
            System.out.println("Decrypted Data: " + new String(decodedBytes));
        } catch (Exception e) {
            Log.e(TAG, "RSA decryption error");
        }
    }
}
