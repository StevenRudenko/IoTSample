package io.github.stevenrudenko.iot.sample.sensor.core.io;

import android.os.Handler;

import io.github.stevenrudenko.iot.sample.sensor.core.base.Sensors;

/** Pressy sensor. */
public class PressySensor extends MicSensor {
    /** Log tag. */
    @SuppressWarnings("unused")
    private static final String TAG = PressySensor.class.getSimpleName();

    /** Reset timeout. */
    private static final long RESET_TIMEOUT = 500;

    /** Reset action handler. */
    private final Handler resetHandler = new Handler();
    /** Reset action. */
    private final Runnable resetAction = new Runnable() {
        @Override
        public void run() {
            post(new float[]{0}, System.currentTimeMillis());
        }
    };

    public PressySensor() {
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
    protected boolean analyze(short data[]) {
        long sum = 0;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < data.length; i++) {
            sum += Math.abs(data[i]);
        }
        if (Math.abs(sum) > 1000000) {
            // button pressed
            post(new float[]{1}, System.currentTimeMillis());
            // reset button press state within delay
            resetHandler.removeCallbacks(resetAction);
            resetHandler.postDelayed(resetAction, RESET_TIMEOUT);
            return true;
        }
        return false;
    }

}
