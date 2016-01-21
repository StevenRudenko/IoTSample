package io.github.stevenrudenko.iot.sample.sensor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import io.github.stevenrudenko.iot.sample.mqtt.MqttUtils;

/** Sensors model. Hold all active sensors data. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorsModel {
    /** Device ID. */
    private String deviceId;
    /** Last message timestamp. */
    private long timestamp;
    /** Accelerometer values. */
    @JsonIgnore
    private HashMap<Integer, SensorProperty> sensors = new HashMap<>();

    public SensorsModel() {
        // for Jackson
    }

    public SensorsModel(Context context) {
        deviceId = MqttUtils.getClientId(context);
    }

    public SensorProperty get(int id) {
        return sensors.get(id);
    }

    public int size() {
        return sensors.size();
    }

    public void add(SensorProperty property) {
        sensors.put(property.getId(), property);
    }

    public void remove(SensorProperty property) {
        sensors.remove(property.getId());
    }

    @JsonProperty("deviceId")
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty("deviceId")
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @JsonProperty("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("sensors")
    public ArrayList<SensorProperty> getSensors() {
        return new ArrayList<>(sensors.values());
    }

    @JsonProperty("sensors")
    public void setSensors(ArrayList<SensorProperty> sensors) {
        for (SensorProperty property : sensors) {
            add(property);
        }
    }

    public void reset() {
        for (SensorProperty property : sensors.values()) {
            final long timestamp = property.getTimestamp();
            final long delay = System.currentTimeMillis() - timestamp;
            property.reset(delay);
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (SensorProperty property : sensors.values()) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(property.getId())
                    .append(": ")
                    .append(Arrays.toString(property.getValues()));
        }
        return builder.toString();
    }
}
