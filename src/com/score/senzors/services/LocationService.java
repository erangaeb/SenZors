package com.score.senzors.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.pojos.Query;
import com.score.senzors.utils.QueryParser;

import java.util.HashMap;

/**
 * service class to access location manager
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class LocationService extends Service {

    SenzorApplication application;

    private LocationManager locationManager;
    private LocListener listener;
    private String locationProvider;

    @Override
    public void onCreate() {
        super.onCreate();

        application = (SenzorApplication) getApplication();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new LocListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Criteria criteria = new Criteria();
        locationProvider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(locationProvider);

        System.out.println("Provider " + locationProvider + " has been selected.");

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        //locationManager.requestLocationUpdates(locationProvider, 0, 0, listener);
        // Initialize the location fields
        /*if (location != null) {
            System.out.println("Provider " + locationProvider + " has been selected.");
            //listener.onLocationChanged(location);
            locationManager.requestLocationUpdates(locationProvider, 0, 0, listener);
        } else {
            // start location updates
            locationManager.requestLocationUpdates(locationProvider, 0, 0, listener);
        }*/

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyManager();

        System.out.println("###################################");
        System.out.println("/////LocationService Destroyed/////");
        System.out.println("###################################");
    }

    public class LocListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("###################################");
            System.out.println("//////// " + location.getLongitude());
            System.out.println("//////// " + location.getLatitude());
            System.out.println("###################################");

            if(application.isRequestFromFriend()) {
                // send location to server via web socket
                handleLocationRequestFromSever(location);
            } else {
                // send location result to sensor list via message
                handleLocationRequestFromSensorList(location);
            }

            stopSelf();
        }
        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    public void destroyManager() {
        if(locationManager != null){
            locationManager.removeUpdates(listener);
            locationManager = null;
        }
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

