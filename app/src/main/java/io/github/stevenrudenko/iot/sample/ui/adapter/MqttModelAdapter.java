package io.github.stevenrudenko.iot.sample.ui.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.stevenrudenko.iot.sample.R;
import io.github.stevenrudenko.iot.sample.sensor.core.base.Sensors;
import io.github.stevenrudenko.iot.sample.sensor.model.SensorProperty;
import io.github.stevenrudenko.iot.sample.sensor.model.SensorsModel;

/** MQTT sensor model adapter. */
public class MqttModelAdapter extends RecyclerView.Adapter<MqttModelAdapter.ViewHolder> {
    /** List to save order. */
    private final List<SensorsModel> list = new ArrayList<>();
    /** Model map. Used to fast assign. */
    private final Map<String, SensorsModel> map = new HashMap<>();
    /** Used to process sensors data while view fill out. */
    private final float[] values = new float[3];

    public void add(SensorsModel model) {
        final String deviceId = model.getDeviceId();
        if (map.containsKey(deviceId)) {
            final SensorsModel cache = map.get(deviceId);
            // update value
            cache.setTimestamp(model.getTimestamp());
            for (SensorProperty property : model.getSensors()) {
                final SensorProperty cachedProp = cache.get(property.getId());
                if (cachedProp == null) {
                    cache.add(property);
                } else {
                    cachedProp.setValue(property.getValues(), property.getTimestamp());
                }
            }
        } else {
            map.put(deviceId, model);
            list.add(model);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.li_mqtt_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final SensorsModel model = list.get(position);
        holder.title.setText(model.getDeviceId());

        for (SensorProperty property : model.getSensors()) {
            final int id = property.getId();
            switch (id) {
                case Sensors.BLE_TI_TEMP_SENSOR:
                    values[0] = property.getValues()[1];
                    break;
                case Sensors.ANDROID_IO_PRESSY_SENSOR:
                    values[1] = property.getValues()[0];
                    break;
                case Sensors.ANDROID_LIGHT_SENSOR:
                    values[2] = property.getValues()[0];
                    break;
            }
        }

        final String text = holder.values.getContext().getString(
                R.string.li_mqttt_values_template,
                values[0], values[1], values[2]);
        holder.values.setText(text);

        final int color = Color.rgb(
                (int)(values[0] * 255),
                (int)(values[1] * 255),
                (int)(values[2] * 255));
        holder.color.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /** View holder. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        /** Device name. */
        private final TextView title;
        /** Values. */
        private final TextView values;
        /** Color view. */
        private final View color;

        private ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.li_mqtt_title);
            values = (TextView) v.findViewById(R.id.li_mqtt_value);
            color = v.findViewById(R.id.li_mqtt_color);
        }
    }

}
