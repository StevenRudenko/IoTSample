package io.github.stevenrudenko.iot.sample.sensor.ble;

import com.chimeraiot.android.ble.sensor.DeviceDef;
import com.chimeraiot.android.ble.sensor.Sensor;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.github.stevenrudenko.iot.sample.sensor.ble.model.TiSensorTag;

/** TI SensorTag sensor group. */
public class TiSensorTagDef extends DeviceDef<TiSensorTag> {
    /** Collection of sensors. */
    private final Map<String, Sensor<TiSensorTag>> sensors = new HashMap<>();

    /**
     * Constructor.
     * @param address - sensor address.
     */
    public TiSensorTagDef(String address) {
        super(new TiSensorTag(address));
        final TiSensorTag model = getModel();
        final TiTemperatureSensor tiTemperatureSensor = new TiTemperatureSensor(address, model);
        sensors.put(tiTemperatureSensor.getServiceUUID(), tiTemperatureSensor);
    }

    @Nullable
    @Override
    public Sensor<TiSensorTag> getSensor(String uuid) {
        return sensors.get(uuid);
    }

}
