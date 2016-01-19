package io.github.stevenrudenko.iot.sample.sensor.core.ble.model;

import java.util.Arrays;

/** TI sensor tag model. */
public class TiSensorTag {
    /** Address of device. */
    private String address;

    /** Temperature value. */
    private final float[] temp = new float[2];

    public TiSensorTag(String address) {
        this.address = address;
    }

    public float[] getTemp() {
        return temp;
    }

    @Override
    public String toString() {
        return "SensorTag:\n"
                + "\tTemp: " + Arrays.toString(temp);
    }
}
