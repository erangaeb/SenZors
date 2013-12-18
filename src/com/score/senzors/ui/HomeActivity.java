package com.score.senzors.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.DrawerItem;
import com.score.senzors.R;
import com.score.senzors.utils.NotificationUtils;

import java.util.ArrayList;

/**
 * Main activity class of MY.sensors
 * Implement navigation drawer here
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class HomeActivity extends FragmentActivity {

    // drawer components
    ArrayList<DrawerItem> drawerItemList;
    DrawerAdapter adapter;
    private ListView drawerListView;
    private DrawerLayout drawerLayout;
    private HomeActionBarDrawerToggle homeActionBarDrawerToggle;

    SenzorApplication application;

    Typeface tf;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        application = (SenzorApplication)this.getApplication();
        tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#985a30")));

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer
        // opens
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        initDrawer();
        homeActionBarDrawerToggle = new HomeActionBarDrawerToggle(this, drawerLayout);
        drawerLayout.setDrawerListener(homeActionBarDrawerToggle);

        loadSensors();
    }

    /**
     * Initialize Drawer menu
     */
    private void initDrawer() {
        // initialize drawer content
        // need to determine selected item according to the currently selected sensor type
        drawerItemList = new ArrayList<DrawerItem>();
        if(application.getSensorType().equalsIgnoreCase(SenzorApplication.MY_SENSORS)) {
            drawerItemList.add(new DrawerItem("My.SenZors", R.drawable.sensors_normal, R.drawable.sensors, true));
            drawerItemList.add(new DrawerItem("Friends.SenZors", R.drawable.share_normal, R.drawable.share, false));
        } else {
            drawerItemList.add(new DrawerItem("My.SenZors", R.drawable.sensors_normal, R.drawable.sensors, false));
            drawerItemList.add(new DrawerItem("Friends.SenZors", R.drawable.share_normal, R.drawable.share, true));
        }

        adapter = new DrawerAdapter(HomeActivity.this, drawerItemList);

        drawerListView = (ListView) findViewById(R.id.drawer);
        if (drawerListView != null)
            drawerListView.setAdapter(adapter);

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
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     *
     * Called whenever we call invalidateOptionsMenu()
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerListView);
        menu.findItem(R.id.action_help).setVisible(!drawerOpen);
        menu.findItem(R.id.action_logout).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		/*
		 * The action bar home/up should open or close the drawer.
		 * ActionBarDrawerToggle will take care of this.
		 */
        if (homeActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_help:
                // TODO display help view
                break;
            case R.id.action_logout:
                if(application.getWebSocketConnection().isConnected())
                    application.getWebSocketConnection().disconnect();
                NotificationUtils.cancelNotification();
                switchToLogin();

                break;
        }

        // Handle your other action bar items.
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle open/close behaviours of Navigation Drawer
     */
    private class HomeActionBarDrawerToggle extends ActionBarDrawerToggle {

        public HomeActionBarDrawerToggle(Activity mActivity, DrawerLayout mDrawerLayout){
            super(mActivity, mDrawerLayout, R.drawable.ic_drawer, R.string.ns_menu_open, R.string.ns_menu_close);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDrawerClosed(View view) {
            //getActionBar().setTitle("My.senzors");
            invalidateOptionsMenu();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDrawerOpened(View drawerView) {
            //getActionBar().setTitle("My.senzors");
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
            //drawerListView.setItemChecked(position, true);
            drawerLayout.closeDrawer(drawerListView);

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

            adapter.notifyDataSetChanged();
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
        //transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Load friends fragment
     */
    private void loadFriends() {
        FriendList friendListFragment = new FriendList();

        // fragment transitions
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, friendListFragment);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     *
     */
    private void switchToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        this.startActivity(intent);
        HomeActivity.this.finish();

        HomeActivity.this.overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
}
