package com.score.senzors.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.R;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.pojos.Sensor;
import com.score.senzors.services.GpsReadingService;
import com.score.senzors.utils.ActivityUtils;
import com.score.senzors.utils.NetworkUtil;

import java.util.ArrayList;

/**
 * Display sensor list/ Fragment
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SensorList extends Fragment implements Handler.Callback {

    SenzorApplication application;

    // use to populate list
    private ListView sensorListView;
    private ArrayList<Sensor> sensorList;
    private SensorListAdapter adapter;

    // empty view when display on no sensors available
    ViewStub emptyView;

    // set custom font
    Typeface face;
    Typeface tf ;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        application = (SenzorApplication) getActivity().getApplication();
        application.setCallback(this);

        initEmptyView();

        tf = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Thin.ttf");

        // two sensor types to display
        //  1. My senzors
        //  2. Friends senzors
        if(application.getSensorType().equalsIgnoreCase(SenzorApplication.MY_SENSORS)) {
            // display my sensors
            //  1. initialize my sensors
            //  2. initialize location listener
            //  3. create list view
            initMySensors();
            initSensorListView();

            int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
            TextView yourTextView = (TextView) (this.getActivity().findViewById(titleId));
            yourTextView.setTextColor(getResources().getColor(R.color.white));
            yourTextView.setTypeface(tf, Typeface.BOLD);

            getActivity().getActionBar().setTitle("My.SenZors");
        } else {
            // display friends sensors
            //  1. initialize friends sensor list
            //  2. create list view
            initFriendsSensors();
            initSensorListView();

            int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
            TextView yourTextView = (TextView) (this.getActivity().findViewById(titleId));
            yourTextView.setTextColor(getResources().getColor(R.color.white));
            yourTextView.setTypeface(tf, Typeface.BOLD);

            getActivity().getActionBar().setTitle("Friends.SenZors");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.sensor_list_layout, null);
        initUI(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // reset callback
        application.setCallback(this);
    }

    /**
     * Initialize UI components
     */
    private void initUI(View view) {
        sensorListView = (ListView)view.findViewById(R.id.sensor_list_layout_sensor_list);

        // initialize custom font
        face = Typeface.createFromAsset(getActivity().getAssets(), "fonts/vegur_2.otf");

        // add header and footer for list
        View headerView = View.inflate(this.getActivity(), R.layout.list_header, null);
        View footerView = View.inflate(this.getActivity(), R.layout.list_header, null);
        sensorListView.addHeaderView(headerView);
        sensorListView.addFooterView(footerView);

        sensorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sensor sensor = sensorList.get(position-1);

                if(NetworkUtil.isAvailableNetwork(SensorList.this.getActivity())) {

                    if(sensor.isMySensor()) {
                        // start location service to get my location
                        ActivityUtils.showProgressDialog(SensorList.this.getActivity(), "Accessing location...");
                        application.setRequestFromFriend(false);
                        application.setRequestQuery(null);
                        Intent serviceIntent = new Intent(getActivity(), GpsReadingService.class);
                        getActivity().startService(serviceIntent);
                    } else {
                        // friend sensor
                        // so need to get request to server
                        // send query to get data
                        if(application.getWebSocketConnection().isConnected()) {
                            ActivityUtils.showProgressDialog(SensorList.this.getActivity(), "Accessing location ...");
                            application.getWebSocketConnection().sendTextMessage("GET #gps " + "@" + sensor.getUser());
                        }
                    }
                } else {
                    Toast.makeText(SensorList.this.getActivity(), "Cannot connect to server, Please check your network connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Initialize empty view for list view
     * empty view need to be display when no sensors available
     */
    private void initEmptyView() {
        emptyView = (ViewStub) getActivity().findViewById(R.id.sensor_list_layout_empty_view);
        View inflatedEmptyView = emptyView.inflate();
        TextView emptyText = (TextView) inflatedEmptyView.findViewById(R.id.empty_text);
        emptyText.setText("No friends sensors available");
        emptyText.setTypeface(face);
    }

    /**
     * Initialize sensor managers and sensor list
     * Get available sensors and current sensor data
     */
    private void initMySensors() {
        sensorList = application.getMySensorList();

        // TODO add other important sensors to list
    }

    private void initFriendsSensors() {
        sensorList = application.getFiendSensorList();
    }

    /**
     * Create sensor list
     */
    private void initSensorListView() {
        // construct list adapter
        if(sensorList.size()>0) {
            adapter = new SensorListAdapter(SensorList.this.getActivity(), sensorList);
            sensorListView.setAdapter(adapter);
        } else {
            sensorListView.setEmptyView(emptyView);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        if(message.obj instanceof LatLon) {
            // we handle LatLon messages only, from here
            // get address from location
            LatLon latLon = (LatLon) message.obj;
            //new GetAddressTask(SensorList.this).execute(latLon);

            // start map activity to display location
            ActivityUtils.cancelProgressDialog();
            application.setLatLon(latLon);
            Intent intent = new Intent(SensorList.this.getActivity(), MapActivity.class);
            this.startActivity(intent);
            SensorList.this.getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
        }

        return false;
    }

    /**
     * Execute after finish the GetAddressTask
     * @param address location address
     */
    public void onPostAddressTask(String address) {
        // cancel dialog first
        ActivityUtils.cancelProgressDialog();

        if(application.getSensorType().equalsIgnoreCase(SenzorApplication.MY_SENSORS)) {
            // update my location
            // reload adapter to display new sensor value
            // currently only have one sensor, that's why we update 1st element always
            application.getMySensorList().get(0).setSensorValue(address);
            application.getMySensorList().get(0).setAvailable(true);
            adapter.reloadAdapter(sensorList);
        } else {
            // when data receives update matching sensor(at friend sensor list) with incoming sensor value
            // we assume here incoming query contains gps value of user
            for(Sensor sensor: application.getFiendSensorList()) {
                // find updating sensor
                if(sensor.getUser().equalsIgnoreCase(application.getDataQuery().getUser())) {
                    // query user and sensor user should be match
                    sensor.setSensorValue(address);
                    sensor.setAvailable(true);
                    adapter.reloadAdapter(application.getFiendSensorList());
                }
            }
        }
    }

}
