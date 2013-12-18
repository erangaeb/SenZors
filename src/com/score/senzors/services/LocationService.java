package com.score.senzors.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import com.score.senzors.application.SenzorApplication;

/**
 * service class to access location manager
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class LocationService extends Service {

    SenzorApplication application;

    public LocationManager locationManager;
    public Location lastLocation = null;
    LocListener listener;

    public static final String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;

    @Override
    public void onCreate() {
        super.onCreate();

        application = (SenzorApplication) getApplication();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        listener = new LocListener();
        lastLocation = locationManager.getLastKnownLocation(LOCATION_PROVIDER);

        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        locationManager.requestLocationUpdates(LOCATION_PROVIDER, 0, 0, listener);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(listener);

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

            stopSelf();
        }
        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

}

