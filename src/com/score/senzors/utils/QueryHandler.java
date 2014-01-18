package com.score.senzors.utils;

import android.content.Intent;
import android.os.Message;
import android.util.Log;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.exceptions.InvalidQueryException;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.pojos.Query;
import com.score.senzors.pojos.Sensor;
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
        // add new sensor to friend sensor list that shared in application
        Sensor sensor = new Sensor(query.getUser(), "Location", "Location", false, false);
        if(!application.getFiendSensorList().contains(sensor))
            application.getFiendSensorList().add(sensor);

        // currently we have to launch friend sensor
        // update notification to notify user about incoming query/ share request
        application.setSensorType(SenzorApplication.FRIENDS_SENSORS);
        Log.d(TAG, "HandleShareQuery: received query with type " + application.getSensorType());
        NotificationUtils.updateNotification(application.getApplicationContext(), "Location @" + query.getUser());
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
