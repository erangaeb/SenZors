package com.score.senzors.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.db.SenzorsDbSource;
import com.score.senzors.pojos.User;
import com.score.senzors.services.WebSocketService;
import com.score.senzors.utils.ActivityUtils;
import com.score.senzors.utils.NetworkUtil;
import com.score.senzors.utils.PreferenceUtils;
import com.score.senzors.utils.QueryHandler;

/**
 * Activity class for login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class LoginActivity extends Activity implements View.OnClickListener, Handler.Callback {

    private static final String TAG = LoginActivity.class.getName();

    private SenzorApplication application;
    private DataUpdateReceiver dataUpdateReceiver;

    // UI fields
    private EditText username;
    private EditText password;
    private TextView appName;
    private TextView appDescription;
    private TextView loginText;
    private RelativeLayout loginButton;

    /**
     * {@inheritDoc}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        application = (SenzorApplication) this.getApplication();
        application.setCallback(this);
        Log.d(TAG, "OnCreate: set handler callback LoginActivity");

        initUi();
        Log.d(TAG, "OnCreate: activity created");
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();

        // register broadcast receiver from here
        Log.d(TAG, "OnResume: registering broadcast receiver");
        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter(WebSocketService.WEB_SOCKET_CONNECTED);
        registerReceiver(dataUpdateReceiver, intentFilter);
    }

    /**
     * {@inheritDoc}
     */
    protected void onPause() {
        super.onPause();

        // un-register broadcast receiver from here
        Log.d(TAG, "OnPause: un-registering broadcast receiver");
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
    }

    /**
     * Initialize UI components
     */
    private void initUi() {
        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        Typeface typefaceBlack = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Black.ttf");

        username = (EditText) findViewById(R.id.login_layout_username);
        password = (EditText) findViewById(R.id.login_layout_password);
        loginButton = (RelativeLayout) findViewById(R.id.login_button_panel);
        appName = (TextView) findViewById(R.id.sensor_text);
        appDescription = (TextView) findViewById(R.id.sensor_text1);
        loginText = (TextView) findViewById(R.id.edit_invoice_layout_mark_as_paid_text);
        loginButton.setOnClickListener(LoginActivity.this);

        appName.setTypeface(typefaceBlack);
        appDescription.setTypeface(typefaceThin, Typeface.BOLD);
        loginText.setTypeface(typefaceThin, Typeface.BOLD);
        username.setTypeface(typefaceThin);
        password.setTypeface(typefaceThin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        if (v==loginButton) {
            Log.d(TAG, "OnClick: click login");
            login();
        }
    }

    /**
     * Login action
     * Connect to web socket and send username password to server
     */
    private void login() {
        if(NetworkUtil.isAvailableNetwork(LoginActivity.this)) {
            if(!username.getText().toString().trim().equals("") && !password.getText().toString().trim().equals("")) {
                // create user and share in application
                application.setUser(new User("0", username.getText().toString().trim(), username.getText().toString().trim(),password.getText().toString().trim()));
                Log.d(TAG, "Login: user shared in application");

                // open web socket and send username password fields
                // we are authenticate with web sockets
                if(!application.getWebSocketConnection().isConnected()) {
                    Log.d(TAG, "Login: not connected to web socket");
                    Log.d(TAG, "Login: connecting to web socket via service");
                    Log.d(TAG, "Login: force to disconnect web socket");
                    ActivityUtils.showProgressDialog(LoginActivity.this, "Connecting to senZors...");
                    Intent serviceIntent = new Intent(LoginActivity.this, WebSocketService.class);
                    startService(serviceIntent);
                    application.setForceToDisconnect(true);
                } else {
                    Log.d(TAG, "Login: already connected to web socket");
                }
            } else {
                Log.d(TAG, "Login: empty username/password");
            }
        } else {
            Log.w(TAG, "Login: no network connection");
            Toast.makeText(LoginActivity.this, "Cannot connect to server, Please check your network connection", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Switch to home activity
     * This method will be call after successful login
     */
    private void switchToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        this.startActivity(intent);
        LoginActivity.this.overridePendingTransition(R.anim.right_in, R.anim.left_out);

        LoginActivity.this.finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        Log.d(TAG, "HandleMessage: message from server");
        ActivityUtils.cancelProgressDialog();

        // we handle string messages only from here
        if(message.obj instanceof String) {
            String payLoad = (String)message.obj;
            Log.d(TAG, "HandleMessage: message is a string - " + payLoad);

            // successful login returns "LoginSUCCESS"
            if(payLoad.equalsIgnoreCase("LoginSUCCESS")) {
                Log.d(TAG, "HandleMessage: login success");
                Log.d(TAG, "HandleMessage: NOT force to disconnect web socket");
                User user = new SenzorsDbSource(LoginActivity.this).getOrCreateUser(application.getUser().getUsername(), application.getUser().getEmail());
                PreferenceUtils.saveUser(LoginActivity.this, user);
                application.setUser(user);
                application.setUpSenzors();
                application.setForceToDisconnect(false);
                switchToHome();
                return true;
            } else {
                Log.d(TAG, "HandleMessage: login fail");
                if(application.getWebSocketConnection().isConnected()) {
                    Log.d(TAG, "HandleMessage: disconnect from web socket");
                    Log.d(TAG, "HandleMessage: force to disconnect web socket");
                    application.setForceToDisconnect(true);
                    application.getWebSocketConnection().disconnect();
                }

                Toast.makeText(LoginActivity.this, "Login fail", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "HandleMessage: message is NOT a string(may be location object)");
        }

        return false;
    }

    /**
     * Register this receiver to get connect/ disconnect messages from web socket
     * Need to do relevant action according to the message, actions as below
     *  1. connect - send login query to server via web socket connections
     *  2. disconnect - logout user
     */
    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "OnReceive: received broadcast message");
            if (intent.getAction().equals(WebSocketService.WEB_SOCKET_CONNECTED)) {
                // send login request to server
                Log.d(TAG, "OnReceive: received broadcast message " + WebSocketService.WEB_SOCKET_CONNECTED);
                QueryHandler.handleLogin(application);
            }
        }
    }

}

