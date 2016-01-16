package io.github.stevenrudenko.iot.sample.sensor.io;

/** Pressy sensor. */
public class PressySensor extends MicSensor {
    /** Log tag. */
    @SuppressWarnings("unused")
    private static final String TAG = PressySensor.class.getSimpleName();

    public PressySensor() {
        super(new float[1]);
    }

    @Override
    public int getId() {
        return 0;
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
            post(new float[]{1}, System.currentTimeMillis());
            return true;
        }
        return false;
    }

}
