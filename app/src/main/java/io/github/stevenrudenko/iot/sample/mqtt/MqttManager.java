package io.github.stevenrudenko.iot.sample.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** MQTT service. */
public class MqttManager implements MqttCallback {

    /** Log tag. */
    private static final String TAG = MqttManager.class.getSimpleName();

    /** Default topic. */
    private static final String DEFAULT_TOPIC = "iot/test";
    /** Default QoS. */
    private static final int DEFAULT_QOS = 2;
    /** Disconnect timeout. */
    private static final long QUIESCE_TIMEOUT = 30000;

    /** Disconnected state. */
    public static final int STATE_DISCONNECTED = 0;
    /** Disconnecing in progress state. */
    public static final int STATE_DISCONNECTING = 1;
    /** Connecting to server. */
    public static final int STATE_CONNECTING = 2;
    /** Connected state. */
    public static final int STATE_CONNECTED = 3;
    /** Connection state. */
    @IntDef(value={
            STATE_DISCONNECTED,
            STATE_DISCONNECTING,
            STATE_CONNECTING,
            STATE_CONNECTED,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectionState {
    }

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

    /** Connection state. */
    @ConnectionState
    private int state;

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

    public void connect() {
        if (client.isConnected()) {
            return;
        }
        final MqttConnectOptions options = MqttUtils.getOptions(context, prefs);
        try {
        if (client.isConnected() || state == STATE_CONNECTING || state == STATE_DISCONNECTING) {
            return;
        }
        state = STATE_CONNECTING;
        client.connect(options, null, new IMqttActionListener() {
            @Override
            public void onSuccess(final IMqttToken asyncActionToken) {
                state = STATE_CONNECTED;
            }

            @Override
            public void onFailure(final IMqttToken asyncActionToken,
                    final Throwable exception) {
                state = STATE_DISCONNECTED;
            }
        });
        } catch (MqttException e) {
            Log.e(TAG, "Fail to connect MQTT client", e);
        }
    }

    @ConnectionState
    public int getState() {
        return state;
    }

    public void release() {
        if (client != null) {
            try {
                if (client.isConnected() && state != STATE_DISCONNECTING) {
                    state = STATE_DISCONNECTING;
                    client.disconnect(QUIESCE_TIMEOUT, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(final IMqttToken asyncActionToken) {
                            state = STATE_DISCONNECTED;
                        }

                        @Override
                        public void onFailure(final IMqttToken asyncActionToken,
                                final Throwable exception) {
                            state = STATE_DISCONNECTED;
                        }
                    });
                }
                client.close();
            } catch (MqttException e) {
                Log.e(TAG, "Fail to stop MQTT client", e);
            }
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
            if (!client.isConnected()) {
                return false;
            }
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
        state = STATE_DISCONNECTED;
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
