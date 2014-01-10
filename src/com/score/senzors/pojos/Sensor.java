package com.score.senzors.pojos;


/**
 * POJO class to hold sensor data attributes
 */
public class Sensor {

    String user;
    String sensorName;
    String sensorValue;
    boolean isMySensor;
    boolean isAvailable;

    public Sensor(String user, String sensorName, String sensorValue, boolean isMySensor, boolean isAvailable) {
        this.user = user;
        this.sensorName = sensorName;
        this.sensorValue = sensorValue;
        this.isMySensor = isMySensor;
        this.isAvailable = isAvailable;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Sensor) {
            Sensor toCompare = (Sensor) obj;
            return (this.user.equalsIgnoreCase(toCompare.getUser()) && this.sensorName.equalsIgnoreCase(toCompare.getSensorName()));
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (this.user + this.sensorName).hashCode();
    }
}
