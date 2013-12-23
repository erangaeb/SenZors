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
import android.provider.Settings;
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

    public LocationManager locationManager;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private Location currentBestLocation = null;

    LocListener listener;

    @Override
    public void onCreate() {
        super.onCreate();

        application = (SenzorApplication) getApplication();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new LocListener();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent1);
        } else {
            currentBestLocation = getLastBestLocation();

            Criteria criteria = new Criteria();
            //criteria.setAccuracy(Criteria.ACCURACY_FINE);
            //criteria.setCostAllowed(false);

            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);

            locationManager.requestLocationUpdates(provider, 0, 0, listener);
            //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
            //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);

            listener.onLocationChanged(location);

            // Initialize the location fields
            if (location == null) {
                //Location not available
                locationManager.removeUpdates(listener);
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //locationManager.removeUpdates(listener);
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

            makeUseOfNewLocation(location);

            if(currentBestLocation == null){
                currentBestLocation = location;
            }

            //FlowController.isTriggerEventReceived = true;
            //mCobytLocationCallback.onLocationChanged(currentBestLocation);
            if(application.isRequestFromFriend()) {
                // send location to server via web socket
                handleLocationRequestFromSever(currentBestLocation);
            } else {
                // send location result to sensor list via message
                handleLocationRequestFromSensorList(currentBestLocation);
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

    /**
     * This method returns the last know location, between the GPS and the Network one.
     * For this method newer is best :)
     *
     * @return the last know best location
     */
    private Location getLastBestLocation() {
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else{
            return locationNet;
        }

    }

    void makeUseOfNewLocation(Location location) {
        if ( isBetterLocation(location, currentBestLocation) ) {
            currentBestLocation = location;
        }
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
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

