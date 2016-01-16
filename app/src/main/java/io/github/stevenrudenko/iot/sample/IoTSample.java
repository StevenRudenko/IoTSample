package io.github.stevenrudenko.iot.sample;

import com.chimeraiot.android.ble.BleConfig;

import android.app.Application;

/** IoT sample application. */
public class IoTSample extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BleConfig.setDebugEnabled(BuildConfig.DEBUG_BLE);
    }
}
