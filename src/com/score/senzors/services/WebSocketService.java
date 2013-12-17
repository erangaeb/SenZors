package com.score.senzors.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
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

    SenzorApplication application;

    @Override
    public void onCreate() {
        application = (SenzorApplication) getApplication();

        System.out.println("///////////////////////////////////");
        System.out.println("//////////SERVICE_CREATED//////////");
        System.out.println("///////////////////////////////////");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("///////////////////////////////////");
        System.out.println("//////////SERVICE_STARTED//////////");
        System.out.println("///////////////////////////////////");

        // connect to web socket from here
        connectToWebSocket(application);
        NotificationUtils.initNotification(this);

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
        System.out.println("///////////////////////////////////");
        System.out.println("/////////////DESTROYED/////////////");
        System.out.println("///////////////////////////////////");

        // close web socket connection here
        NotificationUtils.cancelNotification();
        if(application.getWebSocketConnection().isConnected())
            application.getWebSocketConnection().disconnect();
    }

    /**
     * connect to web socket
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
                    // send login query to user
                    QueryHandler.handleLogin(application);
                }

                @Override
                public void onTextMessage(String payload) {
                    // delegate to handleMessage
                    QueryHandler.handleQuery(application, payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    System.out.println("///////////////////////////////////");
                    System.out.println("//////////////STOPPED//////////////");
                    System.out.println("///////////////////////////////////");

                    // TODO start service again
                    // TODO no need to stop service instead connect to web socket again
                    stopForeground(true);
                }
            });
        } catch (WebSocketException e) {
            System.out.println(e);
        }
    }

}
