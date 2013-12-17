package com.score.senzors.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.User;
import com.score.senzors.utils.NetworkUtil;

/**
 * Activity class for login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class LoginActivity extends Activity implements View.OnClickListener, Handler.Callback {

    // form fields
    private EditText username;
    private EditText password;
    private RelativeLayout loginButton;

    SenzorApplication application;

    /**
     * {@inheritDoc}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        application = (SenzorApplication) this.getApplication();
        application.setCallback(this);

        initUI();
    }

    /**
     * Initialize layout components
     */
    private void initUI() {
        username = (EditText) findViewById(R.id.login_layout_username);
        password = (EditText) findViewById(R.id.login_layout_password);
        loginButton = (RelativeLayout) findViewById(R.id.login_button_panel);
        loginButton.setOnClickListener(LoginActivity.this);

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");
        Typeface tf1 = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Black.ttf");
        TextView tv = (TextView) findViewById(R.id.sensor_text);
        TextView tv1 = (TextView) findViewById(R.id.sensor_text1);
        TextView loginText = (TextView) findViewById(R.id.edit_invoice_layout_mark_as_paid_text);
        TextView username = (TextView) findViewById(R.id.login_layout_username);
        TextView password = (TextView) findViewById(R.id.login_layout_password);
        tv.setTypeface(tf1);
        tv1.setTypeface(tf, Typeface.BOLD);
        loginText.setTypeface(tf, Typeface.BOLD);
        username.setTypeface(tf);
        password.setTypeface(tf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        if (v==loginButton) {
            System.out.println("click login");
            login();
        }
    }

    /**
     * Login action
     */
    private void login() {
        if(NetworkUtil.isAvailableNetwork(LoginActivity.this)) {
            if(!username.getText().toString().trim().equals("") && !password.getText().toString().trim().equals("")) {
                // create user and share in application
                application.setUser(new User(username.getText().toString().trim(), username.getText().toString().trim(),
                        password.getText().toString().trim()));

                // open web socket and send username password fields
                // we are authenticate with web sockets
                if(!application.getWebSocketConnection().isConnected()) {
                    //ActivityUtils.showProgressDialog(LoginActivity.this, "Connecting to server...");
                    //Intent serviceIntent = new Intent(LoginActivity.this, WebSocketService.class);
                    //startService(serviceIntent);
                    switchToHome();
                }
            }
        } else {
            Toast.makeText(LoginActivity.this, "Cannot connect to server, Please check your network connection", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Switch to home activity
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
        // we handle string messages only from here
        if(message.obj instanceof String) {
            String payLoad = (String)message.obj;

            // successful login returns "Hello"
            if(payLoad.equalsIgnoreCase("success")) {
                // un-register login activity from callback
                switchToHome();
                return true;
            } else {
                Toast.makeText(LoginActivity.this, "Login fail", Toast.LENGTH_LONG).show();
            }
        }

        return false;
    }

}

