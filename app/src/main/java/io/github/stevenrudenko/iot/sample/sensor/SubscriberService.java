package io.github.stevenrudenko.iot.sample.sensor;

/** MQTT subcriber service. */
public class SubscriberService extends BaseService {

    @Override
    public void start() {
        super.start();
        getMqttManager().subscribe();
    }

    @Override
    public void stop() {
        super.stop();
        getMqttManager().unsubscribe();
    }

    @Override
    public void onMessageReceived(final byte[] message) {
        super.onMessageReceived(message);
        notifyListeners();
    }
}
