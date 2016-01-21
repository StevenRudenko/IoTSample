package io.github.stevenrudenko.iot.sample.sensor.core.ble;

import com.chimeraiot.android.ble.BleGattExecutor;
import com.chimeraiot.android.ble.BleManager;
import com.chimeraiot.android.ble.BleUtils;
import com.chimeraiot.android.ble.sensor.SensorUtils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Bundle;

import io.github.stevenrudenko.iot.sample.sensor.core.base.Sensors;
import io.github.stevenrudenko.iot.sample.sensor.core.ble.model.TiSensorTag;

import static java.lang.Math.pow;

/** TI BLE device temperature sensor. */
public class TiTemperatureSensor extends BleSensor<TiSensorTag> {
    /** Service UUID. */
    public static final String UUID_SERVICE = "f000aa00-0451-4000-b000-000000000000";
    /** Data UUID. */
    private static final String UUID_DATA = "f000aa01-0451-4000-b000-000000000000";
    /** Configuration UUID. */
    private static final String UUID_CONFIG = "f000aa02-0451-4000-b000-000000000000";

    /** Max value. */
    private static final float MAX_VALUE = 60;
    /** Min value. */
    private static final float MIN_VALUE = 0;

    /** Update period. */
    private static final long REFRESH_PERIOD = 1050;

    public TiTemperatureSensor(String address, TiSensorTag model) {
        super(address, model);
    }

    @Override
    public int getId() {
        return Sensors.BLE_TI_TEMP_SENSOR;
    }

    @Override
    public String getName() {
        return "TI temperature";
    }

    @Override
    public boolean available(Context context) {
        return BleUtils.getBleStatus(context) == BleUtils.STATUS_BLE_ENABLED;
    }

    @Override
    public long getRefreshTimeout() {
        return REFRESH_PERIOD;
    }

    @Override
    public float[] getValue() {
        return getData().getTemp();
    }

    @Override
    public String getServiceUUID() {
        return UUID_SERVICE;
    }

    @Override
    public void start(OnSensorListener listener) {
        super.start(listener);
        final BleManager manager = getBleManager();
        final String address = getAddress();
        manager.update(address, this, UUID_CONFIG, null);
        manager.listen(address, this, UUID_DATA);
    }

    @Override
    protected void post(final float[] data, final long timestamp) {
        // normalize
        for (int i = 0; i < data.length; i++) {
            data[i] = (data[i] - MIN_VALUE) / MAX_VALUE;
        }
        super.post(data, timestamp);
    }

    @Override
    public BleGattExecutor.ServiceAction[] update(String uuid, Bundle data) {
        switch (uuid) {
            case UUID_CONFIG:
                return new BleGattExecutor.ServiceAction[]{
                        write(uuid, new byte[] {
                                (byte)(isRunning() ? 1 : 0)
                        })
                };
            default:
                return super.update(uuid, data);
        }
    }

    @Override
    protected boolean apply(BluetoothGattCharacteristic c, TiSensorTag data) {
        final String uuid = c.getUuid().toString();
        switch (uuid) {
            case UUID_DATA:
                /* The IR Temperature sensor produces two measurements;
                 * Object (AKA target or IR) Temperature,
                 * and Ambient (AKA die) temperature.
                 *
                 * Both need some conversion, and Object temperature is dependent on Ambient temperature.
                 *
                 * They are stored as [ObjLSB, ObjMSB, AmbLSB, AmbMSB] (4 bytes)
                 * Which means we need to shift the bytes around to get the correct values.
                 */
                double ambient = extractAmbientTemperature(c);
                double target = extractTargetTemperature(c, ambient);

                final float[] values = data.getTemp();
                values[0] = (float) ambient;
                values[1] = (float) target;

                post(values, System.currentTimeMillis());
                return true;
            default:
                return false;
        }
    }

    private static double extractAmbientTemperature(BluetoothGattCharacteristic c) {
        int offset = 2;
        return SensorUtils.uint16(c, offset) / 128.0;
    }

    private static double extractTargetTemperature(BluetoothGattCharacteristic c, double ambient) {
        Integer twoByteValue = SensorUtils.int16(c, 0);

        double Vobj2 = twoByteValue.doubleValue();
        Vobj2 *= 0.00000015625;

        double Tdie = ambient + 273.15;

        double S0 = 5.593E-14;  // Calibration factor
        double a1 = 1.75E-3;
        double a2 = -1.678E-5;
        double b0 = -2.94E-5;
        double b1 = -5.7E-7;
        double b2 = 4.63E-9;
        double c2 = 13.4;
        double Tref = 298.15;
        double S = S0*(1+a1*(Tdie - Tref)+a2*pow((Tdie - Tref),2));
        double Vos = b0 + b1*(Tdie - Tref) + b2*pow((Tdie - Tref),2);
        double fObj = (Vobj2 - Vos) + c2*pow((Vobj2 - Vos),2);
        double tObj = pow(pow(Tdie,4) + (fObj/S),.25);

        return tObj - 273.15;
    }

}
