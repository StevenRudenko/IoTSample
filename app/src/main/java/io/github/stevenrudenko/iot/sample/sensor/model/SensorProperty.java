package io.github.stevenrudenko.iot.sample.sensor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;

import io.github.stevenrudenko.iot.sample.sensor.core.base.IoTSensor;

/** Sensor property.  */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorProperty {
    /** Property ID. */
    private int id;
    /** Property name. */
    private String name;
    /** Accelerometer values. */
    private float[] values;
    /** Timestamp. */
    private long timestamp;

    /** Refresh timeout. */
    @JsonIgnore
    private long refreshTimeout;

    @SuppressWarnings("unused")
    public SensorProperty() {
        // for Jackson
    }

    public SensorProperty(IoTSensor sensor) {
        this.id = sensor.getId();
        this.name = sensor.getName();
        final float[] values = sensor.getValue();
        this.values = Arrays.copyOf(values, values.length);
        this.refreshTimeout = sensor.getRefreshTimeout();
    }

    @JsonIgnore
    public void setValue(float[] values, long timestamp) {
        synchronized (this) {
            this.timestamp = timestamp;
            System.arraycopy(values, 0, this.values, 0, values.length);
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float[] getValues() {
        return values;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void reset(long delay) {
        if (delay < refreshTimeout) {
            return;
        }
        synchronized (this) {
            for (int i = 0; i < values.length; i++) {
                values[i] = 0f;
            }
        }
    }

}
