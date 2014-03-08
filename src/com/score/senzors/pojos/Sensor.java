package com.score.senzors.pojos;


import java.util.ArrayList;

/**
 * POJO class to hold sensor data attributes
 */
public class Sensor {
    String id;
    String sensorName;
    String sensorValue;
    boolean isMySensor;
    boolean isAvailable;
    User user;
    ArrayList<User> sharedUsers;

    public Sensor(String id, String sensorName, String sensorValue, boolean isMySensor, boolean isAvailable, User user, ArrayList<User> sharedUsers) {
        this.id = id;
        this.sensorName = sensorName;
        this.sensorValue = sensorValue;
        this.isMySensor = isMySensor;
        this.isAvailable = isAvailable;
        this.user = user;
        this.sharedUsers = sharedUsers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(String sensorValue) {
        this.sensorValue = sensorValue;
    }

    public boolean isMySensor() {
        return isMySensor;
    }

    public void setMySensor(boolean mySensor) {
        isMySensor = mySensor;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<User> getSharedUsers() {
        return sharedUsers;
    }

    public void setSharedUsers(ArrayList<User> sharedUsers) {
        this.sharedUsers = sharedUsers;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Sensor) {
            Sensor toCompare = (Sensor) obj;
            return (this.getUser().getUsername().equalsIgnoreCase(toCompare.getUser().getUsername()) && this.sensorName.equalsIgnoreCase(toCompare.getSensorName()));
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (this.user + this.sensorName).hashCode();
    }
}
