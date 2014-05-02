package com.score.senzors.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.utils.ActivityUtils;

/**
 * Activity class for displaying sensor details
 * Implement tabs with swipe view
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SensorDetailsActivity extends FragmentActivity {
    ViewPager Tab;
    TabPagerAdapater TabAdapter;
    ActionBar actionBar;
    Typeface typefaceThin;
    TextView actionBarTitle;
    SenzorApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_details_layout);
        application = (SenzorApplication)getApplication();
        typefaceThin = Typeface.createFromAsset(this.getAssets(), "fonts/vegur_2.otf");

        actionBar = getActionBar();
        //actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#333333")));
        // set custom font for
        //  1. action bar title
        //  2. other ui texts
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        actionBarTitle = (TextView) (this.findViewById(titleId));
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));
        actionBarTitle.setTypeface(typefaceThin);

        TabAdapter = new TabPagerAdapater(getSupportFragmentManager());
        Tab = (ViewPager)findViewById(R.id.pager);
        Tab.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar = getActionBar();
                        actionBar.setSelectedNavigationItem(position);                    }
                });
        Tab.setAdapter(TabAdapter);

        //Enable Tabs on Action Bar
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("#Location @" + application.getCurrentSensor().getUser().getUsername());
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        setUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);

        // show hide share item according to sensor type
        MenuItem share = menu.findItem(R.id.action_share);
        if (application.getCurrentSensor().isMySensor()) {
            share.setVisible(true);
        } else {
            share.setVisible(false);
        }

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
                Log.d("TAG", "OnOptionsItemSelected: click home menu");
                NavUtils.navigateUpFromSameTask(this);
                this.overridePendingTransition(R.anim.stay_in, R.anim.right_out);
                ActivityUtils.hideSoftKeyboard(this);

                return true;
            case R.id.action_share:
                // start share activity
                Log.d("TAG", "OnOptionsItemSelected: click share menu");
                Intent intent = new Intent(this, ShareActivity.class);
                this.startActivity(intent);
                this.overridePendingTransition(R.anim.bottom_in, R.anim.stay_in);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.d("TAG", "OnBackPressed: go back");
        this.overridePendingTransition(R.anim.stay_in, R.anim.right_out);
    }

    private void setUp() {
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                //To change body of implemented methods use File | Settings | File Templates.
                Tab.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        final TextView t1 = new TextView(this);
        t1.setText("Location");
        t1.setTypeface(typefaceThin);
        t1.setTextColor(Color.parseColor("#4a4a4a"));
        t1.setGravity(Gravity.CENTER);
        t1.setTextSize(18);
        t1.setLayoutParams(params);

        final TextView t2 = new TextView(this);
        if (application.getCurrentSensor().isMySensor()) t2.setText("Shared with");
        else t2.setText("Shared by");

        t2.setTypeface(typefaceThin);
        t2.setGravity(Gravity.CENTER);
        t2.setTextColor(Color.parseColor("#4a4a4a"));
        t2.setLayoutParams(params);
        t2.setTextSize(18);

        //Add New Tab
        actionBar.addTab(actionBar.newTab().setCustomView(t1).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setCustomView(t2).setTabListener(tabListener));
    }

}
