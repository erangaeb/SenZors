package com.score.senzors.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.score.senzors.R;
import com.score.senzors.exceptions.InvalidInputFieldsException;
import com.score.senzors.exceptions.MismatchPasswordException;

/**
 * Utility class to handle activity related common functions
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class ActivityUtils {

    // use to create custom progress dialog
    private static Dialog progressDialog;

    /**
     * Hide keyboard
     * Need to hide soft keyboard in following scenarios
     *  1. When starting background task
     *  2. When exit from activity
     *  3. On button submit
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getApplicationContext().getSystemService(activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Create and show custom progress dialog
     * Progress dialogs displaying on background tasks
     *
     * So in here
     *  1. Create custom layout for message dialog
     *  2, Set messages to dialog
     * @param context activity context
     * @param message message to be display
     */
    public static void showProgressDialog(Context context, String message) {
        progressDialog = new Dialog(context);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setContentView(R.layout.progress_dialog_layout);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // set dialog texts
        TextView messageText = (TextView) progressDialog.findViewById(R.id.progress_message);
        messageText.setText(message);

        // set custom font
        Typeface face= Typeface.createFromAsset(context.getAssets(), "fonts/vegur_2.otf");
        messageText.setTypeface(face);

        progressDialog.show();
    }

    /**
     * Cancel progress dialog when background task finish
     */
    public static void cancelProgressDialog() {
        if (progressDialog!=null) {
            progressDialog.cancel();
        }
    }

    /**
     * Validate input fields of registration form,
     * Need to have
     *      1. non empty valid phone no
     *      2. non empty username
     *      3. non empty passwords
     *      4. two passwords should be match
     *
     * @return valid or not
     */
    public static boolean isValidRegistrationFields(String phoneNo, String username, String password, String confirmPassword) throws InvalidInputFieldsException, MismatchPasswordException {
        if (phoneNo.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            throw new InvalidInputFieldsException();
        }

        if (!password.equals(confirmPassword)) {
            throw new MismatchPasswordException();
        }

        return true;
    }

    /**
     * validate input fields of login form
     * @param username username text
     * @param password password text
     * @return valid of not
     */
    public static boolean isValidLoginFields(String username, String password) {
        return !(username.isEmpty() || password.isEmpty());

    }

    /**
     * Create custom text view for tab view
     * Set custom typeface to the text view as well
     * @param context application context
     * @param title tab title
     * @return text view
     */
    public static TextView getCustomTextView(Context context, String title) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Typeface typefaceThin = Typeface.createFromAsset(context.getAssets(), "fonts/vegur_2.otf");

        TextView textView = new TextView(context);
        textView.setText(title);
        textView.setTypeface(typefaceThin);
        textView.setTextColor(Color.parseColor("#4a4a4a"));
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(18);
        textView.setLayoutParams(layoutParams);

        return textView;
    }

}
