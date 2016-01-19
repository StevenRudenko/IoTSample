package io.github.stevenrudenko.iot.sample.sensor;

import com.chimeraiot.android.ble.BleManager;
import com.chimeraiot.android.ble.BleScanCompat;
import com.chimeraiot.android.ble.BleScanner;
import com.chimeraiot.android.ble.BleServiceListener;
import com.chimeraiot.android.ble.BleUtils;
import com.chimeraiot.android.ble.sensor.DeviceDef;
import com.chimeraiot.android.ble.sensor.DeviceDefCollection;
import com.chimeraiot.android.ble.sensor.Sensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.github.stevenrudenko.iot.sample.IoTSample;
import io.github.stevenrudenko.iot.sample.sensor.core.base.IoTSensor;
import io.github.stevenrudenko.iot.sample.sensor.core.ble.BleSensor;
import io.github.stevenrudenko.iot.sample.sensor.core.ble.TiSensorTagDef;
import io.github.stevenrudenko.iot.sample.sensor.core.ble.TiTemperatureSensor;
import io.github.stevenrudenko.iot.sample.sensor.core.inbuilt.LightSensor;
import io.github.stevenrudenko.iot.sample.sensor.core.io.PressySensor;
import io.github.stevenrudenko.iot.sample.sensor.model.SensorProperty;
import io.github.stevenrudenko.iot.sample.sensor.model.SensorsModel;

/** Sensors manager. */
public class ProducerService extends BaseService implements BleServiceListener,
        BleScanner.BleDevicesScannerListener, IoTSensor.OnSensorListener, Runnable {
    /** Log tag. */
    private static final String TAG = ProducerService.class.getSimpleName();

    /** Timestamp formatter. */
    private static final java.text.DateFormat TIMESTAMP_FORMATTER =
            SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM);

    /** BLE device definition collection. */
    private static final DeviceDefCollection DEVICE_DEF_COLLECTION;

    /** Default period. */
    private static final long DEFAULT_PERIOD = 500L;

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

    /** BLE manager. */
    private BleManager bleManager;
    /** BLE scanner. */
    @Nullable
    private BleScanner bleScanner;

    /** Used while formatting. */
    private final Date date = new Date();

    /** Worker thread handler. */
    private Handler threadHandler;
    /** Period. */
    private volatile long period = DEFAULT_PERIOD;

    /** Sensors. */
    private final List<IoTSensor> sensors = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        //noinspection WrongConstant
        bleManager = (BleManager) getApplicationContext().getSystemService(IoTSample.BLE_SERVICE);
        bleManager.initialize(this);
        bleManager.registerListener(this);

        addSensor(new PressySensor());
        addSensor(new LightSensor());
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

    @Override
    public void onConnected(String name, String address) {
        Log.d(TAG, "BLE device connected: " + name + "@" + address);
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
        addSensor(bleSensor);
        if (isRunning()) {
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

        final SensorsModel model = getModel();
        model.setTimestamp(System.currentTimeMillis());
        final SensorProperty property = model.get(sensor.getId());
        if (property != null) {
            property.setValue(data, timestamp);
        }
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }

        super.start();
        scan();

        for (IoTSensor sensor : sensors) {
            sensor.prepare(this);
            sensor.start(this);
        }

        if (threadHandler != null) {
            return;
        }
        final HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        threadHandler = new Handler(thread.getLooper());
        threadHandler.post(this);
    }

    @Override
    public void stop() {
        for (IoTSensor sensor : sensors) {
            sensor.stop();
        }

        if (threadHandler != null) {
            threadHandler.getLooper().quit();
            threadHandler = null;
        }
        super.stop();
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

    private void addSensor(IoTSensor sensor) {
        sensors.add(sensor);
        getModel().add(new SensorProperty(sensor.getId(), sensor.getName(), sensor.getValue()));
    }

    @Override
    public void run() {
        while (isRunning()) {
            publish();
            notifyListeners();
            try {
                Thread.sleep(period);
                //CHECKSTYLE:OFF
            } catch (InterruptedException ignore) {
                //CHECKSTYLE:ON
            }
        }
    }
}
