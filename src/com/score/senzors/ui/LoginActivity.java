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
import com.score.senzors.exceptions.NoUserException;
import com.score.senzors.pojos.User;
import com.score.senzors.services.WebSocketService;
import com.score.senzors.utils.*;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Activity class for login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class LoginActivity extends Activity implements View.OnClickListener, Handler.Callback {

    private static final String TAG = LoginActivity.class.getName();

    private SenzorApplication application;
    private DataUpdateReceiver dataUpdateReceiver;

    private EditText editTextUsername;
    private EditText editTextPassword;
    private TextView headerText;
    private TextView signUpText;
    private TextView link;
    private RelativeLayout signInButton;
    private RelativeLayout signUpButton;

    // keep user object to use in this activity
    User thiUser;

    /**
     * {@inheritDoc}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        application = (SenzorApplication) this.getApplication();
        initUi();
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();
        application.setCallback(this);
        displayUserCredentials();

        // register broadcast receiver from here
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
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
    }

    /**
     * Initialize UI components
     */
    private void initUi() {
        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        editTextUsername = (EditText) findViewById(R.id.phone_no);
        editTextPassword = (EditText) findViewById(R.id.password);
        signInButton = (RelativeLayout) findViewById(R.id.sign_in_button_panel);
        signUpButton = (RelativeLayout) findViewById(R.id.not_registered);
        headerText = (TextView) findViewById(R.id.header_text);
        signUpText = (TextView) findViewById(R.id.sign_up_text);
        link = (TextView) findViewById(R.id.link);
        signInButton.setOnClickListener(LoginActivity.this);
        signUpButton.setOnClickListener(LoginActivity.this);

        headerText.setTypeface(typefaceThin, Typeface.BOLD);
        signUpText.setTypeface(typefaceThin, Typeface.BOLD);
        editTextUsername.setTypeface(typefaceThin, Typeface.NORMAL);
        editTextPassword.setTypeface(typefaceThin, Typeface.NORMAL);
        link.setTypeface(typefaceThin, Typeface.BOLD);

        displayUserCredentials();
    }

    /**
     * Set user fields in UI
     * actually display username and password in UI
     */
    private void displayUserCredentials() {
        try {
            User user = PreferenceUtils.getUser(LoginActivity.this);
            editTextUsername.setText(user.getUsername());
            editTextPassword.setText(user.getPassword());
        } catch (NoUserException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize this user object
     */
    private void initThisUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        thiUser = new User("0", username, username, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        if (v==signInButton) {
            initThisUser();
            login();
        } else if(v==signUpButton) {
            switchToRegister();
        }
    }

    /**
     * Login action
     * Connect to web socket and send username password to server
     */
    private void login() {
        if(NetworkUtil.isAvailableNetwork(LoginActivity.this)) {
            if(ActivityUtils.isValidLoginFields(thiUser.getUsername(), thiUser.getPassword())) {
                // open web socket and send username password fields
                // we are authenticate with web sockets
                if(!application.getWebSocketConnection().isConnected()) {
                    ActivityUtils.hideSoftKeyboard(this);
                    ActivityUtils.showProgressDialog(LoginActivity.this, "Connecting to senZors...");

                    Intent serviceIntent = new Intent(LoginActivity.this, WebSocketService.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isRegistering", false);
                    serviceIntent.putExtras(bundle);
                    startService(serviceIntent);
                } else {
                    Log.d(TAG, "Login: already connected to web socket");
                }
            } else {
                Log.d(TAG, "Login: empty username/password");
                Toast.makeText(LoginActivity.this, "Invalid input fields", Toast.LENGTH_LONG).show();
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
     * Switch to register activity
     * This method will be call after successful login
     */
    private void switchToRegister() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        this.startActivity(intent);
        LoginActivity.this.overridePendingTransition(R.anim.bottom_in, R.anim.stay_in);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        ActivityUtils.cancelProgressDialog();

        // we handle string messages only from here
        if(message.obj instanceof String) {
            String payLoad = (String)message.obj;
            if (payLoad.equalsIgnoreCase("SERVER_KEY_EXTRACTION_SUCCESS")) {
                Log.d(TAG, "HandleMessage: server key extracted");

                // server key extraction success
                // so send PUT query to create user
                try {
                    if(application.getWebSocketConnection().isConnected()) {
                        String loginQuery = QueryHandler.getLoginQuery(thiUser.getUsername(), thiUser.getPassword(), PreferenceUtils.getSessionKey(this));
                        System.out.println("------login query------");
                        System.out.println(loginQuery);
                        application.getWebSocketConnection().sendTextMessage(loginQuery);
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, e.getMessage());
                }
            } else if(payLoad.equalsIgnoreCase("LoginSUCCESS")) {
                Log.d(TAG, "HandleMessage: login success");
                application.setUpSenzors();
                switchToHome();
            } else {
                Log.d(TAG, "HandleMessage: login fail");
                if(application.getWebSocketConnection().isConnected()) {
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
            if (intent.getAction().equals(WebSocketService.WEB_SOCKET_CONNECTED)) {
                // send login request to server
                Log.d(TAG, "OnReceive: received broadcast message " + WebSocketService.WEB_SOCKET_CONNECTED);
                //QueryHandler.handleLogin(application);
            }
        }
    }

}

