package io.github.stevenrudenko.iot.sample.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.stevenrudenko.iot.sample.R;
import io.github.stevenrudenko.iot.sample.sensor.BaseService;
import io.github.stevenrudenko.iot.sample.sensor.SubscriberService;
import io.github.stevenrudenko.iot.sample.sensor.model.SensorsModel;
import io.github.stevenrudenko.iot.sample.ui.adapter.MqttModelAdapter;

/** MQTT result fragment. */
public class MqttFragment extends BaseSensorsFragment {
    /** Log. tag. */
    private static final String TAG = MqttFragment.class.getSimpleName();

    /** Adapter. */
    private final MqttModelAdapter adapter = new MqttModelAdapter();

    @Override
    protected Class<? extends BaseService> getServiceClass() {
        return SubscriberService.class;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mqtt, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView list = (RecyclerView) view;
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(adapter);
    }

    @Override
    public void onSensorUpdated(final SensorsModel model) {
        Log.d(TAG, "model=" + model);
        adapter.add(model);
    }

}
