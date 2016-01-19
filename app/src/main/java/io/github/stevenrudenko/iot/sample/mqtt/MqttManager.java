package io.github.stevenrudenko.iot.sample.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

/** MQTT service. */
public class MqttManager implements MqttCallback {
    /** Log tag. */
    private static final String TAG = MqttManager.class.getSimpleName();

    /** Default topic. */
    private static final String DEFAULT_TOPIC = "iot/test";
    /** Default QoS. */
    private static final int DEFAULT_QOS = 2;

    /** Context. */
    private final Context context;
    /** Messages topic. */
    private final String topic;

    /** Preferences. */
    private SharedPreferences prefs;

    /** MQTT client. */
    private MqttAsyncClient client;

    /** Listener. */
    private MqttListener listener;

    public MqttManager(Context context) {
        this(context, DEFAULT_TOPIC);
    }

    public MqttManager(Context context, String topic) {
        this.context = context;
        this.topic = topic;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String clientId = MqttUtils.getClientId(context);
        final String host = MqttUtils.getBrokerHost(context, prefs);
        try {
            final MqttDefaultFilePersistence persistence = new MqttDefaultFilePersistence(
                    context.getFilesDir().getAbsolutePath());
            client = new MqttAsyncClient(host, clientId, persistence);
            client.setCallback(this);
        } catch (MqttException e) {
            Log.e(TAG, "Fail to create MQTT client", e);
        }
    }

    public MqttListener getListener() {
        return listener;
    }

    public void setListener(final MqttListener listener) {
        this.listener = listener;
    }

    public void release() {
        if (client != null) {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                }
                client.close();
            } catch (MqttException e) {
                Log.e(TAG, "Fail to stop MQTT client", e);
            }
        }
    }

    public void connect() {
        if (client.isConnected()) {
            return;
        }
        final MqttConnectOptions options = MqttUtils.getOptions(context, prefs);
        try {
            if (client.isConnected()) {
                return;
            }
            client.connect(options);
        } catch (MqttException e) {
            Log.e(TAG, "Fail to connect MQTT client", e);
        }
    }

    public boolean publish(@NonNull byte[] message) {
        try {
            if (!client.isConnected()) {
                return false;
            }
            client.publish(topic, message, DEFAULT_QOS, true);
            return true;
        } catch (MqttException e) {
            Log.e(TAG, "Fail to send message: " + new String(message), e);
        }
        return false;
    }

    public boolean subscribe() {
        try {
            if (!client.isConnected()) {
                return false;
            }
            client.subscribe(topic, DEFAULT_QOS);
            return true;
        } catch (MqttException e) {
            Log.e(TAG, "Fail to subscribe", e);
        }
        return false;
    }

    public boolean unsubscribe() {
        try {
            client.unsubscribe(topic);
            return true;
        } catch (MqttException e) {
            Log.e(TAG, "Fail to unsubscribe", e);
        }
        return false;
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.w(TAG, "Connection with MQTT server lost", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (listener != null) {
            listener.onMessageReceived(message.getPayload());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    /** MQTT events listener. */
    public interface MqttListener {
        /**
         * Notifies that new message received from subscribed topic.
         * @param message byte representation of message.
         */
        void onMessageReceived(byte[] message);
    }

}
