package io.github.stevenrudenko.iot.sample.sensor.base;

import android.support.annotation.CallSuper;

/** Base IoT sensor. */
public abstract class BaseIoTSensor implements IoTSensor {
    /** Listener. */
    private OnSensorListener listener;
    /** Values. */
    private final float[] value;
    /** Indicates whether sensor is working. */
    private boolean isRunning = false;

    public BaseIoTSensor(float[] value) {
        this.value = value;
    }

    @Override
    public float[] getValue() {
        return value;
    }

    @Override
    @CallSuper
    public void start(OnSensorListener listener) {
        this.listener = listener;
        isRunning = true;
    }

    @Override
    @CallSuper
    public void stop() {
        isRunning = false;
    }

    @Override
    public void release() {
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    protected void post(float[] data, long timestamp) {
        if (listener == null) {
            throw new IllegalStateException();
        }
        listener.onSensorDate(this, data, timestamp);
    }

}
