package com.score.senzors.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.services.GpsReadingService;
import com.score.senzors.utils.ActivityUtils;

public class MapActivity extends FragmentActivity implements View.OnClickListener, Handler.Callback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = MapActivity.class.getName();
    private SenzorApplication application;

    private GoogleMap map;
    private Marker marker;
    private  Circle circle;

    private RelativeLayout mapLocation;
    private RelativeLayout mapActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.d(TAG, "OnCreate: creating map activity");
        application = (SenzorApplication) this.getApplication();
        initUi();
        setUpMapIfNeeded();
    }

    /**
     * Initialize UI components
     */
    private void initUi() {
        Log.d(TAG, "InitUI: initializing UI components");
        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Thin.ttf");

        mapLocation = (RelativeLayout) findViewById(R.id.map_location);
        mapActivity = (RelativeLayout) findViewById(R.id.map_activity);
        mapLocation.setOnClickListener(MapActivity.this);
        mapActivity.setOnClickListener(MapActivity.this);

        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(application.getCurrentSensor().getUser()+" #Location");

        // set action bar font type
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView yourTextView = (TextView) (this.findViewById(titleId));
        yourTextView.setTextColor(getResources().getColor(R.color.white));
        yourTextView.setTypeface(typeface, Typeface.BOLD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "OnResume: setting up map, set handler callback MapActivity");
        application.setCallback(this);
        setUpMapIfNeeded();
    }

    /**
     * {@inheritDoc}
     */
    protected void onPause() {
        super.onPause();

        // un-register handler from here
        Log.d(TAG, "OnPause: reset handler callback MapActivity");
        application.setCallback(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // navigate to home with effective navigation
                Log.d(TAG, "OnOptionsItemSelected: click home menu");
                NavUtils.navigateUpFromSameTask(this);
                MapActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.right_out);
                ActivityUtils.hideSoftKeyboard(this);

                return true;
            case R.id.action_share:
                // start share activity
                Log.d(TAG, "OnOptionsItemSelected: click share menu");
                Intent intent = new Intent(this, ShareActivity.class);
                this.startActivity(intent);
                this.overridePendingTransition(R.anim.bottom_in, R.anim.stay_in);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #map} is not null.
     * <p>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            // disable zoom controller
            Log.d(TAG, "SetUpMapIfNeeded: map is empty, so set up it");
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            map.getUiSettings().setZoomControlsEnabled(false);
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker to available location
     * <p>
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpMap() {
        Log.d(TAG, "SetUpMap: set up map on first time");

        // remove existing markers
        if(marker != null) marker.remove();
        if(circle != null) circle.remove();

        LatLon latLon = application.getLatLon();
        LatLng currentCoordinates = new LatLng(Double.parseDouble(latLon.getLat()), Double.parseDouble(latLon.getLon()));
        marker = map.addMarker(new MarkerOptions().position(currentCoordinates).title("My location").icon(BitmapDescriptorFactory.fromResource(R.drawable.bluedot)));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 15));

        // ... get a map.
        // Add a circle in Sydney
        circle = map.addCircle(new CircleOptions()
                .center(currentCoordinates)
                .radius(500)
                .strokeColor(0xFF0000FF)
                .strokeWidth(0.5f)
                .fillColor(0x110000FF));
    }

    /**
     * Move map to given location
     * @param latLon lat/lon object
     */
    private void moveToLocation(LatLon latLon) {
        Log.d(TAG, "MoveToLocation: move map to given location");

        // remove existing markers
        if(marker != null) marker.remove();
        if(circle != null) circle.remove();

        // add location marker
        LatLng currentCoordinates = new LatLng(Double.parseDouble(latLon.getLat()), Double.parseDouble(latLon.getLon()));
        marker = map.addMarker(new MarkerOptions().position(currentCoordinates).title("My new location").icon(BitmapDescriptorFactory.fromResource(R.drawable.bluedot)));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 15));

        // ... get a map
        // Add a circle
        circle = map.addCircle(new CircleOptions()
                .center(currentCoordinates)
                .radius(500)
                .strokeColor(0xFF0000FF)
                .strokeWidth(0.5f)
                .fillColor(0x110000FF));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.d(TAG, "OnBackPressed: go back");
        MapActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.right_out);
    }

    @Override
    public void onClick(View v) {
        if(v==mapLocation) {
            // get location or send request to server for get friends location
            // currently display my location
            // start location service to get my location
            // TODO if this sensor is from friend get friends location , we currently displaying our location
            Log.d(TAG, "OnClick: click on location, get current location");
            ActivityUtils.showProgressDialog(this, "Accessing location...");
            application.setRequestFromFriend(false);
            application.setRequestQuery(null);
            Intent serviceIntent = new Intent(this, GpsReadingService.class);
            startService(serviceIntent);
        } else if(v==mapActivity) {
            Log.d(TAG, "OnClick: click on activity, get user activity");
            // TODO get user activity
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(marker)) {
            Log.d(TAG, "OnMarkerClick: click on location marker");
            // TODO display user/activity details
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        Log.d(TAG, "HandleMessage: message from server");
        if(message.obj instanceof LatLon) {
            // we handle LatLon messages only, from here
            // get address from location
            Log.d(TAG, "HandleMessage: message is a LatLon object so display it on map");
            LatLon latLon = (LatLon) message.obj;

            // display location
            ActivityUtils.cancelProgressDialog();
            application.setLatLon(latLon);
            moveToLocation(latLon);
        } else {
            Log.e(TAG, "HandleMessage: message not a LatLon object");
        }

        return false;
    }

}
