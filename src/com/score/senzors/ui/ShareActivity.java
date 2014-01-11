package com.score.senzors.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.R;
import com.score.senzors.utils.ActivityUtils;
import com.score.senzors.utils.NetworkUtil;

/**
 * Activity class for sharing
 * Implement sharing related functions
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class ShareActivity extends Activity implements Handler.Callback {

    private static final String TAG = ShareActivity.class.getName();

    private SenzorApplication application;

    private EditText emailEditText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_layout);

        application = (SenzorApplication) getApplication();

        initUI();
        Log.d(TAG, "OnCreate: activity created");
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();

        // register handler from here
        Log.d(TAG, "OnResume: set handler callback ShareActivity");
        application.setCallback(this);
    }

    /**
     * {@inheritDoc}
     */
    protected void onPause() {
        super.onPause();

        // un-register handler from here
        Log.d(TAG, "OnPause: reset handler callback ShareActivity");
        application.setCallback(null);
    }

    /**
     * Initialize UI components
     */
    private void initUI() {
        Log.d(TAG, "InitUI: initializing UI components");
        Typeface typefaceThin = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Thin.ttf");

        emailEditText = (EditText) findViewById(R.id.share_layout_email_text);

        // Set up action bar.
        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Share");

        // set custom font for
        //  1. action bar title
        //  2. other ui texts
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitle = (TextView) (this.findViewById(titleId));
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));
        actionBarTitle.setTypeface(typefaceThin, Typeface.BOLD);
        emailEditText.setTypeface(typefaceThin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // navigate to home with effective navigation
                Log.d(TAG, "OnOptionsItemSelected: click home menu");
                NavUtils.navigateUpFromSameTask(this);
                ShareActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
                ActivityUtils.hideSoftKeyboard(this);

                return true;
            case R.id.action_share_done:
                // share sensor data
                Log.d(TAG, "OnOptionsItemSelected: click share menu");
                share();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Share current sensor
     * Need to send share query to server via web socket
     */
    private void share() {
        String email = emailEditText.getText().toString().trim();
        String query = "SHARE" + " " + "#lat #lon" + " " + "@"+emailEditText.getText().toString().trim();
        Log.d(TAG, "Share: sharing query " + query);

        // validate share attribute first
        if(!email.equalsIgnoreCase("")) {
            if(NetworkUtil.isAvailableNetwork(ShareActivity.this)) {
                // construct query and send to server via web socket
                if(application.getWebSocketConnection().isConnected()) {
                    Log.w(TAG, "Login: sending query to server");
                    application.getWebSocketConnection().sendTextMessage(query);
                } else {
                    Log.w(TAG, "Share: not connected to web socket");
                    Toast.makeText(ShareActivity.this, "You are disconnected from senZors service", Toast.LENGTH_LONG).show();
                }

                ActivityUtils.hideSoftKeyboard(this);
            } else {
                Log.w(TAG, "Share: no network connection");
                Toast.makeText(ShareActivity.this, "Cannot connect to server, Please check your network connection", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "Share: empty email");
            Toast.makeText(ShareActivity.this, "Make sure non empty email address", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ShareActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        // we handle string messages only from here
        Log.d(TAG, "HandleMessage: message from server");
        if(message.obj instanceof String) {
            String payLoad = (String)message.obj;
            Log.d(TAG, "HandleMessage: message is a string " + payLoad);

            // successful login returns "ShareDone"
            if(payLoad.equalsIgnoreCase("ShareDone")) {
                Log.d(TAG, "HandleMessage: sharing success");
                Toast.makeText(ShareActivity.this, "Sensor has been shared successfully", Toast.LENGTH_LONG).show();
                ShareActivity.this.finish();
                ShareActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);

                return true;
            } else {
                Log.d(TAG, "HandleMessage: sharing fail");
                Toast.makeText(ShareActivity.this, "Sharing fail", Toast.LENGTH_LONG).show();
            }
        }

        return false;
    }
}
