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

    public static final String WEB_SOCKET_CONNECTED = "WEB_SOCKET_CONNECTED";
    public static final String WEB_SOCKET_DISCONNECTED = "WEB_SOCKET_DISCONNECTED";

    private SenzorApplication application;

    @Override
    public void onCreate() {
        application = (SenzorApplication) getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // connect to web socket from here
        Log.d(TAG, "OnStartCommand: starting service");
        connectToWebSocket(application);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy: service destroyed");

        // close web socket connection here
        if(application.getWebSocketConnection().isConnected()) {
            application.getWebSocketConnection().disconnect();
            Log.d(TAG, "OnDestroy: close web socket connection");
        } else {
            Log.d(TAG, "OnDestroy: not connected to web socket");
        }
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
                    Intent connectMessage = new Intent(WebSocketService.WEB_SOCKET_CONNECTED);
                    sendBroadcast(connectMessage);
                    NotificationUtils.initNotification(WebSocketService.this);
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
                    NotificationUtils.cancelNotification();
                    Intent disconnectMessage = new Intent(WebSocketService.WEB_SOCKET_DISCONNECTED);
                    sendBroadcast(disconnectMessage);
                    stopForeground(true);

                    // TODO start service again
                    // TODO no need to stop service instead connect to web socket again
                }
            });
        } catch (WebSocketException e) {
            Log.e(TAG, "ConnectToWebSocket: error connecting to web socket", e);
        }
    }

}
