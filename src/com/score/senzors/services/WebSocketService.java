package com.score.senzors.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.utils.NotificationUtils;
import com.score.senzors.utils.QueryHandler;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

/**
 * Service for listen to a web socket
 * On login to application this service need tobe start
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class WebSocketService extends Service {

    private static final String TAG = WebSocketService.class.getName();
    private SenzorApplication application;

    public static final String WEB_SOCKET_CONNECTED = "WEB_SOCKET_CONNECTED";
    public static final String WEB_SOCKET_DISCONNECTED = "WEB_SOCKET_DISCONNECTED";

    // Keep track with how many times we tried to connect to web socket
    // maximum try 5 times with 5 seconds break
    private static int REQUEST_COUNT = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        application = (SenzorApplication) getApplication();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // connect to web socket from here
        Log.d(TAG, "OnStartCommand: starting service");
        connectToWebSocket(application);

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        // here we
        //  1. cancel all notifications
        //  2. delete all sensors in my sensor list
        //  3. send broadcast message about service disconnecting
        stopForeground(true);
        NotificationUtils.cancelNotification();
        application.emptyMySensors();
        Intent disconnectMessage = new Intent(WebSocketService.WEB_SOCKET_DISCONNECTED);
        sendBroadcast(disconnectMessage);
        Log.d(TAG, "OnDestroy: service destroyed");
    }

    /**
     * Connect to web socket
     * when connecting we need to send username and password of current user
     * in order to continue communication
     *
     * @param application application object
     */
    public void connectToWebSocket(final SenzorApplication application) {
        try {
            application.getWebSocketConnection().connect(SenzorApplication.WEB_SOCKET_URI, new WebSocketConnectionHandler() {
                @Override
                public void onOpen() {
                    // connected to web socket so notify it to activity
                    Log.d(TAG, "ConnectToWebSocket: open web socket");
                    WebSocketService.REQUEST_COUNT = 0;
                    QueryHandler.handleLogin(application);
                    NotificationUtils.initNotification(WebSocketService.this);
                    //Intent connectMessage = new Intent(WebSocketService.WEB_SOCKET_CONNECTED);
                    //sendBroadcast(connectMessage);
                }

                @Override
                public void onTextMessage(String payload) {
                    // delegate to handleMessage
                    Log.d(TAG, "ConnectToWebSocket: receive message from server");
                    QueryHandler.handleQuery(application, payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "ConnectToWebSocket: web socket closed");
                    Log.d(TAG, "ConnectToWebSocket: code - " + code);
                    Log.d(TAG, "ConnectToWebSocket: reason - " + reason);
                    if(application.isForceToDisconnect()) {
                        Log.d(TAG, "ConnectToWebSocket: forced to disconnect, so stop the service");
                        stopService(new Intent(getApplicationContext(), WebSocketService.class));
                    } else {
                        Log.d(TAG, "ConnectToWebSocket: NOT forced to disconnect, so reconnect again");
                        if(code<4000) reconnectToWebSocket();
                    }
                }
            });
        } catch (WebSocketException e) {
            Log.e(TAG, "ConnectToWebSocket: error connecting to web socket", e);
        }
    }

    /**
     * Reconnect to web socket when connection drops
     * We maximum try 5 times, after that ignore connecting
     */
    private void reconnectToWebSocket() {
        if(WebSocketService.REQUEST_COUNT <=5) {
            try {
                if(application.getWebSocketConnection().isConnected()) {
                    Log.e(TAG, "ReconnectToWebSocket: web socket already connected");
                } else {
                    // sleep for while and connect again
                    Thread.sleep(5000);
                    Log.e(TAG, "ReconnectToWebSocket: trying to re-connect " + (WebSocketService.REQUEST_COUNT+1) + " times");
                    Log.d(TAG, "ReconnectToWebSocket: NOT force to close web socket");
                    application.setForceToDisconnect(false);
                    connectToWebSocket(application);
                    WebSocketService.REQUEST_COUNT++;
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "ReconnectToWebSocket: error sleeping thread", e);
            }
        } else {
            stopService(new Intent(getApplicationContext(), WebSocketService.class));
            Log.d(TAG, "ReconnectToWebSocket: maximum re-connect count exceed");
        }
    }

}
