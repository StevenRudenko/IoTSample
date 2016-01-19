package io.github.stevenrudenko.iot.sample.sensor.core.inbuilt;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import io.github.stevenrudenko.iot.sample.sensor.core.base.BaseIoTSensor;

/** Android sensor. */
public abstract class AndroidSensor extends BaseIoTSensor implements SensorEventListener {

    /** Sensor manager. */
    private SensorManager sensorManager;
    /** Sensor. */
    private Sensor sensor;
    /** Data event handler. */
    private Handler handler;

    public AndroidSensor() {
        this(new float[3]);
    }

    public AndroidSensor(float[] value) {
        super(value);
    }

    protected abstract int getSensorType();

    protected Sensor getSensor() {
        return sensor;
    }

    @Override
    public boolean prepare(final Context context) {
        if (sensorManager != null) {
            return true;
        }
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(getSensorType());

        return true;
    }

    @Override
    public void start(@NonNull OnSensorListener listener) {
        super.start(listener);
        if (handler != null) {
            return;
        }
        if (sensorManager == null) {
            throw new IllegalStateException("Call prepare() before start()");
        }

        final HandlerThread thread = new HandlerThread(getTag());
        thread.start();
        handler = new Handler(thread.getLooper());
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, handler);
    }

    @Override
    public void stop() {
        super.stop();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (handler != null) {
            handler.getLooper().quit();
            handler = null;
        }
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        post(event.values, System.currentTimeMillis());
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
    }

    private String getTag() {
        return "sensor-" + getId();
    }

}
