package com.score.senzors.application;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.pojos.Query;
import com.score.senzors.pojos.Sensor;
import com.score.senzors.pojos.User;
import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;

import java.util.ArrayList;
import java.util.Random;

/**
 * Application class to hold shared attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzorApplication extends Application {

    // determine sensor type
    //  1. my sensors
    //  2. friends sensors
    public final static String MY_SENSORS = "MY_SENSORS";
    public final static String FRIENDS_SENSORS = "FRIENDS_SENSORS";

    // web socket server up and running in this API
    // need to connect this server when starting the app
    //public final static String WEB_SOCKET_URI = "ws://10.2.4.14:9000";
    //public final static String WEB_SOCKET_URI = "ws://mysensors.ucsc.lk:9000";
    public final static String WEB_SOCKET_URI = "ws://connect.mysensors.mobi:8080";

    // web socket connection share in application
    // we are using one instance of web socket in all over the application
    public final WebSocket webSocketConnection = new WebSocketConnection();

    // currently logged in user
    private User user;

    // determine
    //  1. My sensor
    //  2. Friends sensor
    private String sensorType;

    // keep sensors
    //  1. my sensors(ex: location)
    //  2. friends sensors(sensors shared by friends to me)
    private ArrayList<Sensor> fiendSensorList;
    private ArrayList<Sensor> mySensorList;

    // to types of sensors requests(query requests) can be perform
    //  1. request from friend
    //  2. request from own app(get my sensor value to display on app)
    private boolean isRequestFromFriend;

    // to types of queries need to be shared in application
    //  1. GET query from friend
    //  2. DATA query
    private Query requestQuery;
    private Query dataQuery;

    // keep current location
    // this location display on google map
    LatLon latLon;

    // keep track with current senzor
    // we mainly focus on type of current sensor, all the logic depends on the sensor type
    Sensor currentSensor;

    // We disconnect from web socket two ways
    //  1. when log out - in here we don't need to reconnect to web socket
    //  2. automatic disconnect(because of network drop of server error) - in her we need to re connect to web socket
    boolean forceToDisconnect;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // initialize sensor type
        // initialize sensor lists
        // initially add my location to my sensor list
        setSensorType(MY_SENSORS);
        setFiendSensorList(new ArrayList<Sensor>());
        setMySensorList(new ArrayList<Sensor>());

        // initially we ready to response request from friend
        // we don't want to force to disconnect from web socket
        setRequestFromFriend(true);
        setForceToDisconnect(true);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public WebSocket getWebSocketConnection() {
        return webSocketConnection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    Handler handler = new Handler() {
        public void handleMessage(Message message) {
            if (realCallback!=null) {
                realCallback.handleMessage(message);
            }
        }
    };

    Handler.Callback realCallback=null;

    public Handler getHandler() {
        return handler;
    }

    public void setCallback(Handler.Callback c) {
        realCallback = c;
    }

    public ArrayList<Sensor> getFiendSensorList() {
        return fiendSensorList;
    }

    public void setFiendSensorList(ArrayList<Sensor> fiendSensorList) {
        this.fiendSensorList = fiendSensorList;
    }

    public ArrayList<Sensor> getMySensorList() {
        return mySensorList;
    }

    public void setMySensorList(ArrayList<Sensor> mySensorList) {
        this.mySensorList = mySensorList;
    }

    public boolean isRequestFromFriend() {
        return isRequestFromFriend;
    }

    public void setRequestFromFriend(boolean requestFromFriend) {
        isRequestFromFriend = requestFromFriend;
    }

    public Query getRequestQuery() {
        return requestQuery;
    }

    public void setRequestQuery(Query requestQuery) {
        this.requestQuery = requestQuery;
    }

    public Query getDataQuery() {
        return dataQuery;
    }

    public void setDataQuery(Query dataQuery) {
        this.dataQuery = dataQuery;
    }

    public LatLon getLatLon() {
        return latLon;
    }

    public void setLatLon(LatLon latLon) {
        this.latLon = latLon;
    }

    public Sensor getCurrentSensor() {
        return currentSensor;
    }

    public void setCurrentSensor(Sensor currentSensor) {
        this.currentSensor = currentSensor;
    }

    public boolean isForceToDisconnect() {
        return forceToDisconnect;
    }

    public void setForceToDisconnect(boolean forceToDisconnect) {
        this.forceToDisconnect = forceToDisconnect;
    }

    /**
     * Initialize my sensor list
     * Get all available sensors of me and add to sensor list shared in application
     */
    public void initMySensors() {
        // we only support location sensor
        // TODO add more available sensors
        mySensorList.add(new Sensor(getUser().getUsername(), "Location", "LocationValue", true, false));
    }

    /**
     * Delete all sensors in my sensor list
     */
    public void emptyMySensors() {
        for(Sensor sensor: this.mySensorList) {
            mySensorList.remove(sensor);
        }
    }

    public String getRandomLocation() {
        ArrayList<String> locations = new ArrayList<String>();
        locations.add("Kirulapona");
        locations.add("Thimibirigasyaya");
        locations.add("Nugegoda");
        locations.add("Maharagam");
        Random random = new Random(); //Create random class object
        int randomNumber = random.nextInt(locations.size()); //Generate a random number (index) with the size of the list being the maximum
        return locations.get(randomNumber);
    }
}
