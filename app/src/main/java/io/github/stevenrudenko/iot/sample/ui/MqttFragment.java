package io.github.stevenrudenko.iot.sample.ui;

import android.util.Log;

import io.github.stevenrudenko.iot.sample.sensor.BaseService;
import io.github.stevenrudenko.iot.sample.sensor.SubscriberService;
import io.github.stevenrudenko.iot.sample.sensor.model.SensorsModel;

/** MQTT result fragment. */
public class MqttFragment extends BaseSensorsFragment {
    /** Log. tag. */
    private static final String TAG = MqttFragment.class.getSimpleName();

    @Override
    protected Class<? extends BaseService> getServiceClass() {
        return SubscriberService.class;
    }

    @Override
    public void onSensorUpdated(final SensorsModel model) {
        Log.d(TAG, "model=" + model);
    }

}
