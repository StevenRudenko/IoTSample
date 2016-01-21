package io.github.stevenrudenko.iot.sample.sensor.core.base;

import android.content.Context;

/** Sensor definition. */
public interface IoTSensor {

    int getId();

    String getName();

    boolean available(Context context);

    /**
     * Indicates how ofter values is updated.
     * @return milliseconds. Data refresh period.
     */
    long getRefreshTimeout();

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
