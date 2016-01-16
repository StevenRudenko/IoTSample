package io.github.stevenrudenko.iot.sample.sensor;

import com.chimeraiot.android.ble.BleManager;
import com.chimeraiot.android.ble.BleScanCompat;
import com.chimeraiot.android.ble.BleScanner;
import com.chimeraiot.android.ble.BleServiceListener;
import com.chimeraiot.android.ble.BleUtils;
import com.chimeraiot.android.ble.sensor.DeviceDef;
import com.chimeraiot.android.ble.sensor.DeviceDefCollection;
import com.chimeraiot.android.ble.sensor.Sensor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.github.stevenrudenko.iot.sample.common.LocalBinder;
import io.github.stevenrudenko.iot.sample.sensor.base.IoTSensor;
import io.github.stevenrudenko.iot.sample.sensor.ble.BleSensor;
import io.github.stevenrudenko.iot.sample.sensor.ble.TiSensorTagDef;
import io.github.stevenrudenko.iot.sample.sensor.ble.TiTemperatureSensor;
import io.github.stevenrudenko.iot.sample.sensor.inbuilt.LightSensor;
import io.github.stevenrudenko.iot.sample.sensor.io.PressySensor;

/** Sensors manager. */
public class SensorsService extends Service implements SensorManager, BleServiceListener,
        BleScanner.BleDevicesScannerListener, IoTSensor.OnSensorListener {
    /** Log tag. */
    private static final String TAG = SensorsService.class.getSimpleName();

    /** BLE service. */
    public static final String BLE_SERVICE = "ble";

    /** Timestamp formatter. */
    private static final java.text.DateFormat TIMESTAMP_FORMATTER =
            SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM);

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

    /** Binder. */
    private LocalBinder<SensorsService> binder = new LocalBinder<>(this);

    /** BLE manager. */
    private BleManager bleManager;
    /** BLE scanner. */
    @Nullable
    private BleScanner bleScanner;

    /** Used while formatting. */
    private final Date date = new Date();

    /** Sensors. */
    private final List<IoTSensor> sensors = new ArrayList<>();
    /** Indicates where sensors is running. */
    private boolean isRunning;

    /** Connected BLE device address. */
    private String bleDeviceAddress = null;

    @Override
    public void onCreate() {
        super.onCreate();
        bleManager = new BleManager(DEVICE_DEF_COLLECTION);
        bleManager.initialize(this);
        bleManager.registerListener(this);

        sensors.add(new PressySensor());
        sensors.add(new LightSensor());
    }

    @Override
    public Object getSystemService(final String name) {
        if (BLE_SERVICE.equals(name)) {
            return bleManager;
        }
        return super.getSystemService(name);
    }

    @Override
    public String getSystemServiceName(final Class<?> serviceClass) {
        if (serviceClass.equals(BleManager.class)) {
            return BLE_SERVICE;
        }
        return super.getSystemServiceName(serviceClass);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stop();

        if (bleScanner != null) {
            bleScanner.stop();
        }

        for (IoTSensor sensor : sensors) {
            sensor.release();
        }

        bleManager.disconnect();
        bleManager.close();
        bleManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onConnected(String name, String address) {
        Log.d(TAG, "BLE device connected: " + name + "@" + address);
        bleDeviceAddress = address;
    }

    @Override
    public void onConnectionFailed(String name, String address, int status, int state) {
        Log.w(TAG, "BLE device connection failed: " + name + "@" + address);
        scan();
    }

    @Override
    public void onDisconnected(String name, String address) {
        Log.d(TAG, "BLE device disconnected: " + name + "@" + address);
        if (bleScanner != null) {
            bleScanner.start();
        }
    }

    @Override
    public void onServiceDiscovered(String name, String address) {
        Log.d(TAG, "BLE device services discovered: " + name + "@" + address);
        final DeviceDefCollection collection = bleManager.getDeviceDefCollection();
        final DeviceDef def = collection.get(name, address);
        final Sensor sensor = def.getSensor(TiTemperatureSensor.UUID_SERVICE);
        if (sensor == null) {
            return;
        }
        if (!(sensor instanceof BleSensor)) {
            return;
        }
        final BleSensor bleSensor = (BleSensor) sensor;
        sensors.add(bleSensor);
        if (isRunning) {
            bleSensor.prepare(this);
            bleSensor.start(this);
        }
    }

    @Override
    public void onCharacteristicChanged(String name, String address,
            String serviceUuid, String characteristicUuid) {
    }

    @Override
    public void onScanStarted() {
    }

    @Override
    public void onScanRepeat() {
    }

    @Override
    public void onScanStopped() {
    }

    @Override
    public void onLeScan(BluetoothDevice device, int i, byte[] bytes) {
        final String address = device.getAddress();
        final String name = device.getName();
        final DeviceDefCollection collection = bleManager.getDeviceDefCollection();
        final DeviceDef def = collection.get(name, address);
        if (def == null) {
            return;
        }

        if (bleScanner != null) {
            bleScanner.stop();
        }
        bleManager.connect(this, address);
        Log.d(TAG, "connect to device: " + name + "@" + address);
    }

    @Override
    public void onSensorDate(IoTSensor sensor, float[] data, long timestamp) {
        date.setTime(timestamp);
        final String message = TIMESTAMP_FORMATTER.format(date)
                + " " + sensor.getName()
                + ": " + Arrays.toString(data);
        Log.d(TAG, message);
    }

    @Override
    public void start() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        scan();

        for (IoTSensor sensor : sensors) {
            sensor.prepare(this);
            sensor.start(this);
        }
    }

    @Override
    public void stop() {
        for (IoTSensor sensor : sensors) {
            sensor.stop();
        }
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    private void scan() {
        final BluetoothAdapter adapter = BleUtils.getBluetoothAdapter(this);
        if (adapter != null && adapter.isEnabled()) {
            if (bleScanner == null) {
                bleScanner = new BleScanCompat(adapter, this);
            }
            bleScanner.start();
        }
    }

}
