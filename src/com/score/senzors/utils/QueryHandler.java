package com.score.senzors.utils;

import android.content.Intent;
import android.os.Message;
import android.util.Log;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.db.SenzorsDbSource;
import com.score.senzors.exceptions.InvalidQueryException;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.pojos.Query;
import com.score.senzors.pojos.Sensor;
import com.score.senzors.pojos.User;
import com.score.senzors.services.GpsReadingService;

import java.util.HashMap;

/**
 * Handler class for incoming queries
 * Handle following queries
 *  1. STATUS
 *  2. SHARE
 *  3. GET
 *  4. LOGIN
 *  5. DATA
 *
 *  @author Eranga Herath(erangaeb@gmail.com)
 */
public class QueryHandler {

    private static final String TAG = QueryHandler.class.getName();

    /**
     * Generate login query and send to server
     * @param application application object instance
     */
    public static void handleLogin(SenzorApplication application) {
        // generate login query with user credentials
        // sample query - LOGIN #name era #skey 123 @mysensors
        String command = "LOGIN";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", application.getUser().getUsername());
        params.put("skey", application.getUser().getPassword());
        String message = QueryParser.getMessage(new Query(command, "mysensors", params));

        System.out.println("login message " + message);
        application.getWebSocketConnection().sendTextMessage(message);
    }

    /**
     * Handle query message from web socket
     *
     * @param application application object
     * @param payload payload from server
     */
    public static void handleQuery(SenzorApplication application, String payload) {
        System.out.println(payload);
        try {
            // need to parse query in order to further processing
            Query query = QueryParser.parse(payload);

            if(query.getCommand().equalsIgnoreCase("STATUS")) {
                // STATUS query
                // handle LOGIN and SHARE status from handleStatus
                handleStatusQuery(application, query);
            } else if(query.getCommand().equalsIgnoreCase("SHARE")) {
                // SHARE query
                // handle SHARE query from handleShare
                handleShareQuery(application, query);
            } else if(query.getCommand().equalsIgnoreCase(":SHARE")) {
                // UN-SHARE query
                // handle Un-SHARE query from handleUnShare
                handleUnShareQuery(application, query);
            } else if (query.getCommand().equalsIgnoreCase("GET")) {
                // GET query
                // handle via handleGet
                handleGetQuery(application, query);
            } else if(query.getCommand().equalsIgnoreCase("DATA")) {
                // DATA query
                // handle via handleData
                handleDataQuery(application, query);
            } else {
                // invalid query or not supporting query
                System.out.println("INVALID/UN-SUPPORTING query");
            }
        } catch (InvalidQueryException e) {
            System.out.println(e);
        }
    }

    /**
     * Handle STATUS query from server
     * @param application application
     * @param query parsed query
     */
    private static void handleStatusQuery(SenzorApplication application, Query query) {
        // get status from query
        String status = "success";

        if (query.getParams().containsKey("login")) {
            // login status
            status = query.getParams().get("login");
        } else if (query.getParams().containsKey("share")){
            // share status
            status = query.getParams().get("share");
        }

        // just send status to available handler
        sendMessage(application, status);
    }

    /**
     * Handle SHARE query from server
     * @param application application
     * @param query parsed query
     */
    private static void handleShareQuery(SenzorApplication application, Query query) {
        // get or create match user
        // create/save new sensor in db
        User user = new SenzorsDbSource(application.getApplicationContext()).getOrCreateUser(query.getUser(), "email");
        Sensor sensor = new Sensor("0", "Location", "Location", false, false, user, null);

        try {
            // save sensor in db and refresh friend sensor list
            new SenzorsDbSource(application.getApplicationContext()).addSensor(sensor);
            application.initFriendsSensors();
            Log.d(TAG, "HandleShareQuery: saved sensor from - " + user.getUsername());

            // currently we have to launch friend sensor
            // update notification to notify user about incoming query/ share request
            application.setSensorType(SenzorApplication.FRIENDS_SENSORS);
            Log.d(TAG, "HandleShareQuery: received query with type " + application.getSensorType());
            NotificationUtils.updateNotification(application.getApplicationContext(), "Location @" + user.getUsername());
        } catch (Exception e) {
            // Db exception here
            Log.e(TAG, "HandleShareQuery: db error " + e.toString());
        }
    }

    /**
     * Handle UNSHARE query from server
     * @param application application
     * @param query parsed query
     */
    private static void handleUnShareQuery(SenzorApplication application, Query query) {
        // get match user and sensor
        User user = new SenzorsDbSource(application.getApplicationContext()).getOrCreateUser(query.getUser(), "email");
        Sensor sensor = new Sensor("0", "Location", "Location", false, false, user, null);
        try {
            // delete sensor  from db
            // new SenzorsDbSource(application.getApplicationContext()).deleteSharedUser(user);
            new SenzorsDbSource(application.getApplicationContext()).deleteSensorOfUser(sensor);
            application.initFriendsSensors();
            Log.d(TAG, "HandleUnShareQuery: deleted sensor from - " + user.getUsername());

            // currently we have to launch friend sensor
            // update notification to notify user about incoming query/ share request
            application.setSensorType(SenzorApplication.FRIENDS_SENSORS);
            Log.d(TAG, "HandleUnShareQuery: received query with type " + application.getSensorType());
            NotificationUtils.updateNotification(application.getApplicationContext(), "Unshared Location @" + user.getUsername());
        } catch (Exception e) {
            // Db exception here
            Log.e(TAG, "HandleUnShareQuery: db error " + e.toString());
        }
    }

    /**
     * Handle GET query from server
     * @param application application
     * @param query parsed query
     */
    private static void handleGetQuery(SenzorApplication application, Query query) {
        // get location by starting location service
        if(application.getWebSocketConnection().isConnected()) {
            // current location request is from web socket service
            // start location service
            application.setRequestFromFriend(true);
            application.setRequestQuery(query);
            Intent serviceIntent = new Intent(application.getApplicationContext(), GpsReadingService.class);
            application.getApplicationContext().startService(serviceIntent);
        }
    }

    /**
     * Handle DATA query from server
     * @param application application
     * @param query parsed query
     */
    private static void handleDataQuery(SenzorApplication application, Query query) {
        if(query.getUser().equalsIgnoreCase("mysensors")) {
            // this is a status query
            // @mysensors DATA #msg LoginSuccess
            // just send status to available handler
            String status = query.getParams().get("msg");
            sendMessage(application, status);
        } else {
            // from a specific user
            // create LatLon object from query params
            // we assume incoming query contains lat lon values
            LatLon latLon = new LatLon(query.getParams().get("lat"), query.getParams().get("lon"));
            application.setDataQuery(query);

            // send message to available handler to notify incoming sensor value
            sendMessage(application, latLon);
        }
    }

    /**
     * Send message to appropriate handler
     * @param application application
     * @param obj payload from server
     */
    private static void sendMessage(SenzorApplication application, Object obj) {
        Message message = Message.obtain();
        message.obj = obj;
        if (application.getHandler()!=null) {
            application.getHandler().sendMessage(message);
        }
    }

}
