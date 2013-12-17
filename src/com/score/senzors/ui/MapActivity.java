package com.score.senzors.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.utils.ActivityUtils;

public class MapActivity extends FragmentActivity {
    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;

    SenzorApplication application;

    Typeface tf ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        application = (SenzorApplication) this.getApplication();
        tf = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Thin.ttf");

        // Set up action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Location");

        // set action bar font type
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView yourTextView = (TextView) (this.findViewById(titleId));
        yourTextView.setTextColor(getResources().getColor(R.color.white));
        yourTextView.setTypeface(tf, Typeface.BOLD);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
                NavUtils.navigateUpFromSameTask(this);
                MapActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.right_out);

                ActivityUtils.hideSoftKeyboard(this);
                return true;
            case R.id.action_share:
                // start share activity
                // start share activity
                Intent intent = new Intent(this, ShareActivity.class);
                this.startActivity(intent);
                this.overridePendingTransition(R.anim.bottom_in, R.anim.stay_in);
                //this.finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
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
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.setMyLocationEnabled(true);
        // display current location
        LatLon latLon = application.getLatLon();
        LatLng currentCoordinates = new LatLng(Double.parseDouble(latLon.getLat()), Double.parseDouble(latLon.getLon()));
        mMap.addMarker(new MarkerOptions().position(currentCoordinates).title("Marker"));
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 15));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 15));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MapActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.right_out);
    }
}
