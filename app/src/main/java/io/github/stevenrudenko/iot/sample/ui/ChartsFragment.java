package io.github.stevenrudenko.iot.sample.ui;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.stevenrudenko.iot.sample.R;
import io.github.stevenrudenko.iot.sample.sensor.BaseService;
import io.github.stevenrudenko.iot.sample.sensor.ProducerService;
import io.github.stevenrudenko.iot.sample.sensor.model.SensorProperty;
import io.github.stevenrudenko.iot.sample.sensor.model.SensorsModel;

/** Charts fragment. */
public class ChartsFragment extends BaseSensorsFragment {
    /** Log tag. */
    @SuppressWarnings("unused")
    private static final String TAG = ChartsFragment.class.getSimpleName();

    /** Max data time length (in seconds). */
    private static final int MAX_DATA_TIME_LENGTH = 15;
    /** Charts colors. */
    private static final int[] COLORS = {Color.RED, Color.GREEN, Color.BLUE};

    /** Time formatter. */
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.US);
    /** Date. */
    private static final Date time = new Date();

    /** Charts container. */
    private LinearLayout root;
    /** Charts. */
    private SparseArrayCompat<LineChart> charts = new SparseArrayCompat<>();

    protected Class<? extends ProducerService> getServiceClass() {
        return ProducerService.class;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_charts, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = (LinearLayout) view.findViewById(R.id.charts_root);
    }

    @Override
    public void onServiceBind(BaseService service) {
        super.onServiceBind(service);

        final List<SensorProperty> sensors = service.getModel().getSensors();
        final Context context = getActivity();
        final int count = sensors.size();
        for (int i = 0; i < count; i++) {
            addChart(context, sensors.get(i));
        }
    }

    @Override
    public void onSensorUpdated(SensorsModel model) {
        final List<SensorProperty> sensors = model.getSensors();
        final Context context = getActivity();
        final int count = sensors.size();
        for (int i = 0; i < count; i++) {
            final SensorProperty property = sensors.get(i);
            LineChart chart = charts.get(property.getId());
            if (chart == null) {
                chart = addChart(context, property);
            }
            addEntry(chart, property);
        }
    }

    private LineChart addChart(Context context, SensorProperty property) {
        final int height = context.getResources().getDimensionPixelSize(R.dimen.chart_height);
        LineChart chart = charts.get(property.getId());
        if (chart != null) {
            return chart;
        }

        final int dataLength = property.getValues().length;
        final LineData data = new LineData();
        data.setDrawValues(false);
        for (int d = 0; d < dataLength; d++) {
            addDataSet(data, "", COLORS[d % COLORS.length]);
        }

        chart = new LineChart(context);
        chart.setData(data);
        chart.setNoDataText("");
        chart.setDescription(property.getName());
        chart.setDrawMarkerViews(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);

        charts.put(property.getId(), chart);
        root.addView(chart, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height));
        return chart;
    }

    private static void addEntry(LineChart chart, SensorProperty property) {
        final LineData data = chart.getLineData();
        synchronized (ChartsFragment.class) {
            time.setTime(property.getTimestamp());
            data.addXValue(TIME_FORMAT.format(time));
        }
        final float[] values = property.getValues();
        for (int i = 0; i < values.length; i++) {
            final LineDataSet set = data.getDataSetByIndex(i);
            data.addEntry(new Entry(values[i], set.getEntryCount()), i);
        }
        chart.notifyDataSetChanged();
        chart.invalidate();

        chart.setVisibleXRange(MAX_DATA_TIME_LENGTH, MAX_DATA_TIME_LENGTH);
        chart.moveViewToX(data.getXValCount() - MAX_DATA_TIME_LENGTH - 1);
    }

    private static void addDataSet(LineData data, String name, int color) {
        final LineDataSet set = new LineDataSet(new ArrayList<Entry>(), name);
        set.setLineWidth(2.5f);
        set.setColor(color);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setDrawCubic(false);
        set.setDrawValues(false);
        data.addDataSet(set);
    }

}
