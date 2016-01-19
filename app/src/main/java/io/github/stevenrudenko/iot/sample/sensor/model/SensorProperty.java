package io.github.stevenrudenko.iot.sample.sensor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;

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

    public SensorProperty() {
        // for Jackson
    }

    public SensorProperty(int id, String name, float[] values) {
        this.id = id;
        this.name = name;
        this.values = Arrays.copyOf(values, values.length);
    }

    @JsonIgnore
    public void setValue(float[] values, long timestamp) {
        this.timestamp = timestamp;
        System.arraycopy(values, 0, this.values, 0, values.length);
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
}
