package io.github.stevenrudenko.iot.sample.sensor.core.io;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.support.v4.media.session.MediaButtonReceiver;

import io.github.stevenrudenko.iot.sample.sensor.core.base.Sensors;

/** Pressy sensor. Works as switch ON/OFF. */
public class PressySwitchSensor extends MicSensor {
    /** Log tag. */
    @SuppressWarnings("unused")
    private static final String TAG = PressySwitchSensor.class.getSimpleName();

    /** Current value. */
    private float[] values = new float[1];

    /** Audio manager. */
    private AudioManager audioManager;
    /** Force intercept audio button click. */
    private ComponentName receiver;

    public PressySwitchSensor() {
        super(new float[1]);
    }

    @Override
    public int getId() {
        return Sensors.ANDROID_IO_PRESSY_SENSOR;
    }

    @Override
    public String getName() {
        return "Pressy button";
    }

    @Override
    public long getRefreshTimeout() {
        // not dynamic
        return Long.MAX_VALUE;
    }

    @Override
    public boolean prepare(Context context) {
        receiver = new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName());
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return super.prepare(context);
    }

    @Override
    public void start(final OnSensorListener listener) {
        super.start(listener);
        audioManager.registerMediaButtonEventReceiver(receiver);
    }

    @Override
    public void stop() {
        super.stop();
        audioManager.unregisterMediaButtonEventReceiver(receiver);
    }

    @Override
    protected boolean analyze(short data[]) {
        long sum = 0;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < data.length; i++) {
            sum += Math.abs(data[i]);
        }
        if (Math.abs(sum) > 1000000) {
            // button pressed
            if (values[0] > 0) {
                values[0] = 0;
            } else {
                values[0] = 1;
            }
            post(values, System.currentTimeMillis());
            return true;
        }
        return false;
    }

}
