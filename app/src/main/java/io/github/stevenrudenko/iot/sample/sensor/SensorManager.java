package io.github.stevenrudenko.iot.sample.sensor;

/** Sensor manager. */
public interface SensorManager {

    void start();

    void stop();

    boolean isRunning();

}
