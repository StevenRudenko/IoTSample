package io.github.stevenrudenko.iot.sample.sensor.core.inbuilt;

import android.content.Context;
import android.hardware.Sensor;
import android.util.Log;

import io.github.stevenrudenko.iot.sample.sensor.core.base.Sensors;

/** Light sensor. */
public class LightSensor extends AndroidSensor {
    /** Log tag. */
    private static final String TAG = LightSensor.class.getSimpleName();

    /** Used to send correct value array. */
    private final float[] proxyValue = new float[1];

    /** Sensor value range. */
    private float max;

    public LightSensor() {
        super(new float[3]);
    }

    @Override
    public int getId() {
        return Sensors.ANDROID_LIGHT_SENSOR;
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
    public boolean prepare(final Context context) {
        final boolean result = super.prepare(context);
        if (result) {
            max = getSensor().getMaximumRange();
            Log.d(TAG, getName() + ": max value=" + max);
        }
        return result;
    }

    @Override
    protected void post(final float[] data, final long timestamp) {
        proxyValue[0] = data[0] / max;
        super.post(proxyValue, timestamp);
    }
}
