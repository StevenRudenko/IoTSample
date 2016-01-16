package io.github.stevenrudenko.iot.sample.sensor.base;

import android.content.Context;

import java.util.HashMap;

/** Sensor definition. */
public interface IoTSensor {

    int getId();

    String getName();

    float[] getValue();

    boolean prepare(Context context);

    void start(OnSensorListener listener);

    void stop();

    void release();

    boolean isRunning();

    /** Sensor data listener. */
    interface OnSensorListener {
        /**
         * Returns updates sensor data.
         * @param sensor sensor.
         * @param data data.
         */
        void onSensorDate(IoTSensor sensor, float[] data, long timestamp);
    }

}
