package com.score.senzors.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.DrawerItem;
import com.score.senzors.R;
import com.score.senzors.services.WebSocketService;
import com.score.senzors.utils.ActivityUtils;

import java.util.ArrayList;

/**
 * Main activity class of MY.sensors
 * Implement navigation drawer here
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class HomeActivity extends FragmentActivity {

    private static final String TAG = HomeActivity.class.getName();

    private SenzorApplication application;
    private DataUpdateReceiver dataUpdateReceiver;

    // Ui components
    private ListView drawerListView;
    private DrawerLayout drawerLayout;
    private RelativeLayout drawerContainer;
    private HomeActionBarDrawerToggle homeActionBarDrawerToggle;

    // drawer components
    private ArrayList<DrawerItem> drawerItemList;
    private DrawerAdapter drawerAdapter;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
        application = (SenzorApplication)this.getApplication();

        initDrawer();
        initDrawerList();
        loadSensors();
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();

        // register broadcast receiver from here
        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter(WebSocketService.WEB_SOCKET_DISCONNECTED);
        registerReceiver(dataUpdateReceiver, intentFilter);

        // check web socket connected, if not redirect to login
        if(!application.isServiceRunning()) {
            Log.d(TAG, "OnResume: disconnected from web socket, so redirect to login");
            Toast.makeText(this, "Disconnected from service", Toast.LENGTH_LONG).show();
            switchToLogin();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void onPause() {
        super.onPause();

        // un-register broadcast receiver from here
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (homeActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize Drawer UI components
     */
    private void initDrawer() {
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerContainer = (RelativeLayout) findViewById(R.id.drawer_container);

        // set custom sign out button
        TextView signOutTextView = (TextView) findViewById(R.id.sign_out_text);
        Typeface face = Typeface.createFromAsset(this.getAssets(), "fonts/vegur_2.otf");
        signOutTextView.setTypeface(face, Typeface.NORMAL);

        // set a custom shadow that overlays the main content when the drawer opens
        // set up drawer listener
        //drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        homeActionBarDrawerToggle = new HomeActionBarDrawerToggle(this, drawerLayout);
        drawerLayout.setDrawerListener(homeActionBarDrawerToggle);
    }

    /**
     * Initialize Drawer list
     */
    private void initDrawerList() {
        // initialize drawer content
        // need to determine selected item according to the currently selected sensor type
        drawerItemList = new ArrayList<DrawerItem>();
        if(application.getSensorType().equalsIgnoreCase(SenzorApplication.MY_SENSORS)) {
            drawerItemList.add(new DrawerItem("My.senZors", R.drawable.my_sensz_normal, R.drawable.my_sensz_selected, true));
            drawerItemList.add(new DrawerItem("Friends.senZors", R.drawable.friends_normal, R.drawable.friends_selected, false));
        } else {
            drawerItemList.add(new DrawerItem("My.senZors", R.drawable.my_sensz_normal, R.drawable.my_sensz_selected, false));
            drawerItemList.add(new DrawerItem("Friends.senZors", R.drawable.friends_normal, R.drawable.friends_selected, true));
        }

        drawerAdapter = new DrawerAdapter(HomeActivity.this, drawerItemList);
        drawerListView = (ListView) findViewById(R.id.drawer);

        if (drawerListView != null)
            drawerListView.setAdapter(drawerAdapter);

        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        homeActionBarDrawerToggle.syncState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        homeActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Handle open/close behaviours of Navigation Drawer
     */
    private class HomeActionBarDrawerToggle extends ActionBarDrawerToggle {

        public HomeActionBarDrawerToggle(Activity mActivity, DrawerLayout mDrawerLayout){
            super(mActivity, mDrawerLayout, R.drawable.ic_navigation_drawer, R.string.ns_menu_open, R.string.ns_menu_close);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDrawerClosed(View view) {
            invalidateOptionsMenu();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDrawerOpened(View drawerView) {
            invalidateOptionsMenu();
        }
    }

    /**
     * Drawer click event handler
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Highlight the selected item, update the title, and close the drawer
            // update selected item and title, then close the drawer
            drawerLayout.closeDrawer(drawerContainer);

            //  reset content in drawer list
            for(DrawerItem drawerItem: drawerItemList) {
                drawerItem.setSelected(false);
            }

            if(position == 0) {
                // set
                //  1. sensor type
                application.setSensorType(SenzorApplication.MY_SENSORS);
                loadSensors();
                drawerItemList.get(0).setSelected(true);
            } else if(position==1) {
                // set
                //  1. sensor type
                application.setSensorType(SenzorApplication.FRIENDS_SENSORS);
                loadSensors();
                drawerItemList.get(1).setSelected(true);
            }

            drawerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Load my sensor list fragment
     */
    private void loadSensors() {
        SensorList sensorListFragment = new SensorList();

        // fragment transitions
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, sensorListFragment);
        transaction.commit();
    }

    /**
     * Navigate to login activity
     * This happens when logout
     */
    private void switchToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        this.startActivity(intent);
        HomeActivity.this.finish();

        HomeActivity.this.overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    /**
     * Register this receiver to get disconnect messages from web socket
     * Need to do relevant action according to the message, actions as below
     *  1. connect - send login query to server via web socket connections
     *  2. disconnect - disconnect from server
     */
    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "OnReceive: received broadcast message");
            ActivityUtils.cancelProgressDialog();
            if (intent.getAction().equals(WebSocketService.WEB_SOCKET_DISCONNECTED)) {
                // cancel existing notifications after disconnect
                Log.d(TAG, "OnReceive: received broadcast message " + WebSocketService.WEB_SOCKET_DISCONNECTED);
                switchToLogin();
            }
        }
    }

}
