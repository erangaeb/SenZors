package com.score.senzors.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.pojos.Query;
import com.score.senzors.utils.QueryParser;

import java.util.HashMap;

public class GpsReadingService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
                                                          GooglePlayServicesClient.OnConnectionFailedListener,
                                                          com.google.android.gms.location.LocationListener {

    SenzorApplication application;
    private LocationClient locationClient;

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    // Define an object that holds accuracy and frequency parameters
    LocationRequest locationRequest;

    /*
     Called before service  onStart method is called.All Initialization part goes here
    */
    @Override
    public void onCreate() {
        application = (SenzorApplication) getApplication();
        locationClient = new LocationClient(getApplicationContext(), this,this);

        // Create the LocationRequest object
        locationRequest = LocationRequest.create();
        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        locationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("###################################");
        System.out.println("//////LocationService Started//////");
        System.out.println("###################################");

        if (isServicesConnected())
            locationClient.connect();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("###################################");
        System.out.println("//////LocationConnection Fail//////");
        System.out.println("###################################");
    }

    @Override
    public void onConnected(Bundle arg0) {
        System.out.println("###################################");
        System.out.println("//////////LocationConnected////////");
        System.out.println("###################################");

        Location location = locationClient.getLastLocation();

        if(location!=null) {
            System.out.println("###################################");
            System.out.println("***********************************");
            System.out.println("//////// " + location.getLatitude());
            System.out.println("//////// " + location.getLongitude());
            System.out.println("###################################");

            if(application.isRequestFromFriend()) {
                // send location to server via web socket
                handleLocationRequestFromSever(location);
            } else {
                // send location result to sensor list via message
                handleLocationRequestFromSensorList(location);
            }

            // disconnect location clint
            // stop service
            locationClient.disconnect();
            stopSelf();
        } else {
            // not current location available
            // so need to get location from location listener
            // register listener here
            locationClient.requestLocationUpdates(locationRequest, this);
        }
    }

    @Override
    public void onDisconnected() {
        System.out.println("###################################");
        System.out.println("////////LocationDisconnected///////");
        System.out.println("###################################");
    }

    /**
     * Verify that Google Play services is available before making a request.
     * @return true if Google Play services is available, otherwise false
     */
    private boolean isServicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(GpsReadingService.this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            return false;
        }
    }

    /*
     Called when Service running in background is stopped.
     Remove location update to stop receiving gps reading
    */
    @Override
    public void onDestroy() {
        System.out.println("###################################");
        System.out.println("/////LocationService Destroyed/////");
        System.out.println("###################################");

        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        // get location and send to appropriate handle
        // the close location updates
        // stop service
        if(application.isRequestFromFriend()) {
            // send location to server via web socket
            handleLocationRequestFromSever(location);
        } else {
            // send location result to sensor list via message
            handleLocationRequestFromSensorList(location);
        }

        // If the client is connected
        if (locationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
             locationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        locationClient.disconnect();
        stopSelf();
    }

    /**
     * Handle location request that comes from server as a query
     * need to send location to server via web socket
     * @param location current location
     */
    private void handleLocationRequestFromSever(Location location) {
        String command = "DATA";
        String user = application.getRequestQuery().getUser();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("lat", Double.toString(location.getLatitude()));
        params.put("lon", Double.toString(location.getLongitude()));
        String message = QueryParser.getMessage(new Query(command, user, params));

        // send data to server
        if(application.getWebSocketConnection().isConnected()) {
            application.getWebSocketConnection().sendTextMessage(message);
        }
    }

    /**
     * Location request comes from internal(from sensor list)by clicking my location sensor
     * So need to send update to sensor list
     * @param location current location
     */
    private void handleLocationRequestFromSensorList(Location location) {
        LatLon latLon = new LatLon(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));

        // send message to available handler
        Message message = Message.obtain();
        message.obj = latLon;
        if (application.getHandler()!=null) {
            application.getHandler().sendMessage(message);
        }
    }
}
