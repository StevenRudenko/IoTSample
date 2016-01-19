package io.github.stevenrudenko.iot.sample;

import com.chimeraiot.android.ble.BleConfig;
import com.chimeraiot.android.ble.BleManager;
import com.chimeraiot.android.ble.sensor.DeviceDef;
import com.chimeraiot.android.ble.sensor.DeviceDefCollection;
import com.github.mikephil.charting.utils.Utils;

import android.app.Application;

import io.github.stevenrudenko.iot.sample.mqtt.MqttManager;
import io.github.stevenrudenko.iot.sample.sensor.core.ble.TiSensorTagDef;

/** IoT sample application. */
public class IoTSample extends Application {

    /** BLE service. */
    public static final String BLE_SERVICE = "ble";
    /** MQTT service. */
    public static final String MQTT_SERVICE = "mqtt";

    /** BLE device definition collection. */
    private static final DeviceDefCollection DEVICE_DEF_COLLECTION;
    static {
        DEVICE_DEF_COLLECTION = new DeviceDefCollection() {
            @Override
            public DeviceDef create(String name, String address) {
                if (name == null) {
                    return null;
                }
                switch (name) {
                    case "SensorTag":
                    case "CC2650 SensorTag":
                        return new TiSensorTagDef(address);
                    default:
                        return null;
                }
            }
        };
        DEVICE_DEF_COLLECTION.register("");
    }

    /** MQTT manager. */
    private MqttManager mqttManager;
    /** BLE manager. */
    private BleManager bleManager;

    @Override
    public void onCreate() {
        super.onCreate();
        BleConfig.setDebugEnabled(BuildConfig.DEBUG_BLE);
        // init charts utils
        Utils.init(this);

        // shared MQTT manager
        mqttManager = new MqttManager(this);
        // shared BLE manager
        bleManager = new BleManager(DEVICE_DEF_COLLECTION);
    }

    @Override
    public Object getSystemService(final String name) {
        switch (name) {
            case BLE_SERVICE:
                return bleManager;
            case MQTT_SERVICE:
                return mqttManager;
            default:
                return super.getSystemService(name);
        }
    }

    @Override
    public String getSystemServiceName(final Class<?> serviceClass) {
        if (serviceClass.equals(BleManager.class)) {
            return BLE_SERVICE;
        } else if (serviceClass.equals(MqttManager.class)) {
            return MQTT_SERVICE;
        }
        return super.getSystemServiceName(serviceClass);
    }

}
