package com.score.senzors.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.score.senzors.R;

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
     *
     * @return progress dialog
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
     * Get custom typeface
     * @param context activity context
     * @return typeface
     */
    public static Typeface getThinTypeFace(Context context) {
        return null;
    }

}
