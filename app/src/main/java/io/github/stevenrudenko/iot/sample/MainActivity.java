package io.github.stevenrudenko.iot.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import io.github.stevenrudenko.iot.sample.common.ServiceConnector;
import io.github.stevenrudenko.iot.sample.sensor.SensorsService;

/** Main activity. Contains navigation logic. */
public class MainActivity extends AppCompatActivity
        implements ServiceConnector.OnServiceConnectionListener<SensorsService> {
    /** Log tag. */
    private static final String TAG = MainActivity.class.getSimpleName();

    /** Sensor service connector. */
    private final ServiceConnector<SensorsService> connector = new ServiceConnector<>(this);

    /** FAB. */
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!connector.isConnected()) {
                    return;
                }
                final SensorsService service = connector.getService();
                if (service.isRunning()) {
                    service.stop();
                    Snackbar.make(view, "Stop", Snackbar.LENGTH_SHORT).show();
                } else {
                    service.start();
                    Snackbar.make(view, "Start", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        final Intent service = new Intent(this, SensorsService.class);
        bindService(service, connector, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connector.isConnected()) {
            unbindService(connector);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceBind(SensorsService service) {
        fab.setEnabled(true);
        Log.d(TAG, "Sensor service connected");
    }

    @Override
    public void onServiceUnbind() {
        fab.setEnabled(false);
        Log.d(TAG, "Sensor service disconnected");
    }
}
