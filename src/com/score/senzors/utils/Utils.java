package com.score.senzors.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.score.senzors.exceptions.NoPhoneNoException;

/**
 * Class deal with generic helper function
 *
 * @author eranga.herath@pagero.com (eranga herath)
 */
public class Utils {

    /**
     * Get phone no of the device, we use this device to identify the user
     *
     * @param context application context
     * @return device phone no
     */
    public static String getPhoneNo(Context context) throws NoPhoneNoException {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNo = telephonyManager.getLine1Number();

        if (phoneNo.isEmpty())
            throw new NoPhoneNoException();

        return phoneNo;
    }
}
