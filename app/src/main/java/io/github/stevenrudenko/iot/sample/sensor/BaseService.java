package io.github.stevenrudenko.iot.sample.sensor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.github.stevenrudenko.iot.sample.IoTSample;
import io.github.stevenrudenko.iot.sample.common.LocalBinder;
import io.github.stevenrudenko.iot.sample.mqtt.MqttManager;
import io.github.stevenrudenko.iot.sample.sensor.model.SensorsModel;

/** Base sensor manager. */
public class BaseService extends Service implements MqttManager.MqttListener {
    /** Log tag. */
    private static final String TAG = BaseService.class.getSimpleName();

    /** Binder. */
    private LocalBinder<BaseService> binder = new LocalBinder<>(this);

    /** Listeners. */
    private final Set<OnSensorUpdateListener> listeners = new HashSet<>();
    /** Listeners lock. */
    private final ReadWriteLock listenersSync = new ReentrantReadWriteLock();
    /** UI thread handler. */
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    /** Sensor update notifier. */
    private final SensorUpdateNotifier notifier = new SensorUpdateNotifier();

    /** Indicates where sensors is running. */
    private boolean isRunning;

    /** Json mapper. */
    private final ObjectMapper jsonMapper = new ObjectMapper();
    /** Sensors model. */
    private SensorsModel model;

    /** MQTT manager. */
    private MqttManager mqttManager;

    @Override
    public void onCreate() {
        super.onCreate();
        model = new SensorsModel(this);

        //noinspection WrongConstant
        mqttManager = (MqttManager) getApplicationContext().getSystemService(
                IoTSample.MQTT_SERVICE);
        mqttManager.setListener(this);
        mqttManager.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mqttManager.release();
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return binder;
    }

    public void start() {
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    protected MqttManager getMqttManager() {
        return mqttManager;
    }

    public SensorsModel getModel() {
        return model;
    }

    public void addListener(OnSensorUpdateListener listener) {
        try {
            listenersSync.writeLock().lock();
            listeners.add(listener);
        } finally {
            listenersSync.writeLock().unlock();
        }
    }

    public void removeListener(OnSensorUpdateListener listener) {
        try {
            listenersSync.writeLock().lock();
            listeners.remove(listener);
        } finally {
            listenersSync.writeLock().unlock();
        }
    }

    protected void publish() {
        try {
            final String json = jsonMapper.writeValueAsString(model);
            mqttManager.publish(json.getBytes());
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Fail to create JSON string from model", e);
        }
    }

    protected void notifyListeners() {
        uiThreadHandler.post(notifier);
    }

    @Override
    public void onMessageReceived(byte[] message) {
        try {
            model = jsonMapper.readValue(message, SensorsModel.class);
        } catch (IOException e) {
            Log.e(TAG, "Fail to parse model", e);
        }
    }

    /** Sensor model update listener. */
    public interface OnSensorUpdateListener {

        /**
         * Called when sensor model update posts.
         * @param model sensor model.
         */
        void onSensorUpdated(SensorsModel model);
    }

    /** Used to notify listeners about current model. */
    private class SensorUpdateNotifier implements Runnable {
        @Override
        public void run() {
            try {
                listenersSync.readLock().lock();
                for (OnSensorUpdateListener listener : listeners) {
                    listener.onSensorUpdated(model);
                }
            } finally {
                listenersSync.readLock().unlock();
            }
        }
    }

}
