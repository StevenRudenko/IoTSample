package io.github.stevenrudenko.iot.sample.sensor.inbuilt;

import android.hardware.Sensor;

/** Light sensor */
public class LightSensor extends AndroidSensor {
    /** Sensor ID. */
    public static final int ID = 1;

    /** Used to send correct value array. */
    private final float[] proxyValue = new float[1];

    public LightSensor() {
        super(new float[3]);
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Light Meter";
    }

    @Override
    protected int getSensorType() {
        return Sensor.TYPE_LIGHT;
    }

    @Override
    public float[] getValue() {
        return proxyValue;
    }

    @Override
    protected void post(final float[] data, final long timestamp) {
        proxyValue[0] = data[0];
        super.post(proxyValue, timestamp);
    }
}
