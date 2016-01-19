package io.github.stevenrudenko.iot.sample.sensor.core.ble;

import com.chimeraiot.android.ble.BleManager;
import com.chimeraiot.android.ble.sensor.Sensor;

import android.content.Context;
import android.support.annotation.CallSuper;

import io.github.stevenrudenko.iot.sample.IoTSample;
import io.github.stevenrudenko.iot.sample.sensor.ProducerService;
import io.github.stevenrudenko.iot.sample.sensor.core.base.IoTSensor;

/**
 * BLE enabled sensor.
 * @param <M> model.
 */
public abstract class BleSensor<M> extends Sensor<M> implements IoTSensor {

    /** Device address. */
    private final String address;

    /** Listener. */
    private OnSensorListener listener;
    /** Indicates whether sensor is working. */
    private boolean isRunning = false;

    /** BLE manager. */
    private BleManager bleManager;

    public BleSensor(String address, M model) {
        super(model);
        this.address = address;
    }

    public BleManager getBleManager() {
        return bleManager;
    }

    public String getAddress() {
        return address;
    }

    @Override
    @CallSuper
    public boolean prepare(Context context) {
        //noinspection WrongConstant
        bleManager = (BleManager) context.getApplicationContext().getSystemService(
                IoTSample.BLE_SERVICE);
        return true;
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
    @CallSuper
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
