package com.score.senzors.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import com.score.senzors.R;
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
    // maximum try 10 times
    private static int RECONNECT_COUNT = 0;
    private static int MAX_RECONNECT_COUNT = 14;

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
        application.setServiceRunning(true);

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
        //  1. cancel/update all notifications
        //  2. delete all sensors in my sensor list
        //  3. send broadcast message about service disconnecting
        stopForeground(true);
        if(application.isForceToDisconnect()) {
            NotificationUtils.cancelNotification(this);
        } else {
            Notification notification = NotificationUtils.getNotification(WebSocketService.this, R.drawable.app_icon_disconnect,
                    getString(R.string.app_name), getString(R.string.disconnected));
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NotificationUtils.SERVICE_NOTIFICATION_ID, notification);
        }

        application.setServiceRunning(false);
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
                    WebSocketService.RECONNECT_COUNT = 0;
                    QueryHandler.handleLogin(application);
                    Notification notification = NotificationUtils.getNotification(WebSocketService.this, R.drawable.app_icon121,
                            getString(R.string.app_name), getString(R.string.launch_senzors));
                    notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
                    startForeground(NotificationUtils.SERVICE_NOTIFICATION_ID, notification);
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
                        if(code<4000) new WebSocketReConnector().execute();
                    }
                }
            });
        } catch (WebSocketException e) {
            Log.e(TAG, "ConnectToWebSocket: error connecting to web socket", e);
        }
    }

    /**
     * Reconnect to web socket when connection drops
     * We maximum try 10 times, after that ignore connecting
     */
    private void reconnectToWebSocket() {
        if(WebSocketService.RECONNECT_COUNT <= WebSocketService.MAX_RECONNECT_COUNT) {
            if(application.getWebSocketConnection().isConnected()) {
                Log.e(TAG, "ReconnectToWebSocket: web socket already connected");
            } else {
                Log.e(TAG, "ReconnectToWebSocket: trying to re-connect " + (WebSocketService.RECONNECT_COUNT+1) + " times");
                Log.d(TAG, "ReconnectToWebSocket: NOT force to close web socket");
                application.setForceToDisconnect(false);
                connectToWebSocket(application);
                WebSocketService.RECONNECT_COUNT++;
            }
        } else {
            stopService(new Intent(getApplicationContext(), WebSocketService.class));
            Log.d(TAG, "ReconnectToWebSocket: maximum re-connect count exceed");
        }
    }

    /**
     * Async task use to reconnect web socket
     * When web socket is disconnected we are trying to reconnect it via this task
     */
    private class WebSocketReConnector extends AsyncTask<Void, Void, Void> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "WebSocketReConnector: reconnecting via async task");
            try {
                // sleep for a while before reconnect
                // sleep for random time interval
                if(WebSocketService.RECONNECT_COUNT <= WebSocketService.MAX_RECONNECT_COUNT/2)
                    Thread.sleep(5000);
                else
                    Thread.sleep(10000);
                reconnectToWebSocket();
            } catch (InterruptedException e) {
                Log.e(TAG, "WebSocketReConnector: error sleeping thread", e);
            }

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
