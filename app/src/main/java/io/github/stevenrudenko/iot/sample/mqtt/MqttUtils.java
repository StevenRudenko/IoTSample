package io.github.stevenrudenko.iot.sample.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.Properties;

import io.github.stevenrudenko.iot.sample.R;

/** MQTT helper methods. */
public final class MqttUtils {

    /** Server certificate asset path. */
    private static final String CERT_FILENAME = "mqtt.crt";

    private MqttUtils() {
        // prevent instantiation
    }

    /**
     * Generates client ID for Android device.
     * @param context used to generate hostname.
     * @return hostname.
     */
    public static String getClientId(Context context) {
        return Build.MODEL.replaceAll(" ", "-") + "-" + Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Generates MQTT client connection options.
     * @param context used to read preference keys.
     * @param prefs to read configuration values from.
     * @return client connection options.
     */
    public static MqttConnectOptions getOptions(Context context, SharedPreferences prefs) {
        final Resources res = context.getResources();
        final String keyUsername = res.getString(R.string.pref_server_username_key);
        final String keyPassword = res.getString(R.string.pref_server_password_key);
        final String defaultUsername = res.getString(R.string.pref_server_username_default);
        final String defaultPassword = res.getString(R.string.pref_server_password_default);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(prefs.getString(keyUsername, defaultUsername));
        options.setPassword(prefs.getString(keyPassword, defaultPassword).toCharArray());
        options.setSocketFactory(CertUtils.getCertificate(context, CERT_FILENAME));

        final Properties ssl = new Properties();
        ssl.setProperty("com.ibm.ssl.protocol", "tlsv1");
        options.setSSLProperties(ssl);
        return options;
    }

    /**
     * Generates MQTT client connection host.
     * @param context used to read preference keys.
     * @param prefs to read configuration values from.
     * @return client connection host.
     */
    public static String getBrokerHost(Context context, SharedPreferences prefs) {
        final Resources res = context.getResources();
        final String keyHost = res.getString(R.string.pref_server_host_key);
        final String keyPort = res.getString(R.string.pref_server_port_key);
        final String defaultHost = res.getString(R.string.pref_server_host_default);
        final String defaultPort = res.getString(R.string.pref_server_port_default);

        final String portString = prefs.getString(keyPort, defaultPort);
        final String host = prefs.getString(keyHost, defaultHost);
        final String port = TextUtils.isEmpty(portString) ? "8883" : portString;
        return "ssl://" + host + ":" + port;
    }


}
