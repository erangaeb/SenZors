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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.db.SenzorsDbSource;
import com.score.senzors.exceptions.InvalidQueryException;
import com.score.senzors.exceptions.NoUserException;
import com.score.senzors.exceptions.RsaKeyException;
import com.score.senzors.pojos.Query;
import com.score.senzors.pojos.User;
import com.score.senzors.services.WebSocketService;
import com.score.senzors.utils.*;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

/**
 * Activity class for login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class LoginActivity extends Activity implements View.OnClickListener, Handler.Callback {

    private static final String TAG = LoginActivity.class.getName();

    private final WebSocketConnection mConnection = new WebSocketConnection();

    private SenzorApplication application;
    private DataUpdateReceiver dataUpdateReceiver;

    // UI fields
    private EditText phoneNo;
    private EditText username;
    private EditText password;
    private EditText confirmPassword;
    private TextView headerText;
    private TextView signUpText;
    private RelativeLayout signUpButton;

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

        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");
        Typeface typefaceBlack = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Black.ttf");

        phoneNo = (EditText) findViewById(R.id.phone_no);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirm_password);
        signUpButton = (RelativeLayout) findViewById(R.id.sign_up_button_panel);
        headerText = (TextView) findViewById(R.id.header_text);
        signUpText = (TextView) findViewById(R.id.sign_up_text);
        signUpButton.setOnClickListener(LoginActivity.this);

        headerText.setTypeface(typefaceThin, Typeface.BOLD);
        signUpText.setTypeface(typefaceThin, Typeface.BOLD);
        phoneNo.setTypeface(typefaceThin, Typeface.NORMAL);
        username.setTypeface(typefaceThin, Typeface.NORMAL);
        password.setTypeface(typefaceThin, Typeface.NORMAL);
        confirmPassword.setTypeface(typefaceThin, Typeface.NORMAL);

        // get saved used and display credentials
        try {
            User user = PreferenceUtils.getUser(LoginActivity.this);
            username.setText(user.getUsername());
            password.setText(user.getPassword());
        } catch (NoUserException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        if (v==signUpButton) {
            Log.d(TAG, "OnClick: click login");
            signUp();
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
     * Sign up action
     * Connect to web socket and send PUT request
     */
    private void signUp() {
        if(!phoneNo.getText().toString().trim().equals("") && !password.getText().toString().trim().equals("") && !confirmPassword.getText().toString().trim().equals("")) {
            if(NetworkUtil.isAvailableNetwork(LoginActivity.this)) {
                if (!PreferenceUtils.isRasKeysSaved(this)) {
                    CryptoUtils.initKeys(LoginActivity.this);
                } else {
                    Log.d(TAG, "Already saved RSA keys");
                }

                registerUser();
            }
        }
    }

    /**
     * Register user in server,
     * basically upload public key and phone no to server
     * Send PUT query hear
     */
    private void registerUser() {
        try {
            // construct put message
            final HashMap<String, String> params = new HashMap<String, String>();
            String command = "PUT";
            String name = phoneNo.getText().toString().trim();
            String pubKey = PreferenceUtils. getEncodedRsaKey(application, "public_key");
            String signature = CryptoUtils.getEncryptedMessage(application, name);
            params.put("name", name);
            params.put("pubkey", pubKey);
            params.put("signature", signature);
            final String message = QueryParser.getMessage(new Query(command, "mysensors", params));

            mConnection.connect(SenzorApplication.WEB_SOCKET_URI, new WebSocketConnectionHandler() {
                @Override
                public void onOpen() {
                    // send put query
                    Log.d(TAG, "sending message " + message);
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.d(TAG, "Got message: " + payload);
                    if (payload.contains("pubkey")) {
                        // receive server public key
                        try {
                            Query query = QueryParser.parse(payload);
                            byte[] serverKey = Base64.decode(query.getParams().get("pubkey"), Base64.DEFAULT);
                            String sKey = new String(serverKey, "UTF-8");

                            // remove BEGIN and END from key
                            String publicPEM = sKey.replace("-----BEGIN PUBLIC KEY-----\n", "");
                            publicPEM = publicPEM.replace("-----END PUBLIC KEY-----", "");
                            System.out.println(publicPEM);

                            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(publicPEM, Base64.DEFAULT));
                            KeyFactory kf = KeyFactory.getInstance("RSA");
                            PublicKey key = kf.generatePublic(spec);
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (InvalidKeySpecException e) {
                            e.printStackTrace();
                        } catch (InvalidQueryException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    //mConnection.sendTextMessage("LOGIN #name eranga #skey 1234");
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "Connection lost");
                }
            });
        } catch (WebSocketException e) {
            Log.d(TAG, e.toString());
        } catch (RsaKeyException e) {
            Log.d(TAG, e.toString());
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

                // get user
                // set password of the user since we not storing password in database
                User user = new SenzorsDbSource(LoginActivity.this).getOrCreateUser(application.getUser().getUsername(), application.getUser().getEmail());
                user.setPassword(application.getUser().getPassword());

                PreferenceUtils.saveUser(LoginActivity.this, user);
                application.setUser(user);
                Log.d(TAG, "HandleMessage: user saved " + application.getUser().getId());
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

