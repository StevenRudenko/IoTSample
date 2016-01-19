package io.github.stevenrudenko.iot.sample.ui;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import io.github.stevenrudenko.iot.sample.R;
import io.github.stevenrudenko.iot.sample.sensor.BaseService;

/** Main activity. */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /** Log tag. */
    private static final String TAG = MainActivity.class.getSimpleName();

    /** Settings dialog tag. */
    private static final String DIALOG_SETTINGS = "dialog:settings";

    /** Request code for permission. */
    private static final int PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 1;

    /** Local sensors fragment tag. */
    private static final String LOCAL_SENSORS_TAG = "LOCAL";
    /** MQTT result fragment tag. */
    private static final String MQTT_RESULT_TAG = "MQTT";

    /** FAB. */
    private FloatingActionButton fab;

    /** Indicates whether permission has been granted. {@code null} if permission has not been requested yet. */
    private Boolean isPermissionGranted = null;

    /** Screen fragments. */
    private final BaseSensorsFragment[] screens = new BaseSensorsFragment[] {
            new ChartsFragment(),
            new MqttFragment()
    };
    /** Active screen. */
    private BaseSensorsFragment active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.main_fab_service);
        fab.setOnClickListener(this);

        getFragmentManager().beginTransaction()
                .add(R.id.container, screens[0], LOCAL_SENSORS_TAG)
                .add(R.id.container, screens[1], MQTT_RESULT_TAG)
                .commit();

        final SpinnerAdapter adapter = new SpinnerAdapter(toolbar.getContext(),
                getResources().getStringArray(R.array.app_spinner));
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final Fragment hide = active;
                active = screens[position];
                if (hide == active) {
                    return;
                }
                final FragmentTransaction transaction = getFragmentManager()
                        .beginTransaction();
                if (hide != null) {
                    transaction.hide(hide);
                }
                transaction.show(active).commit();

                final BaseService service = active.getService();
                if (service != null && service.isRunning()) {
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    fab.setImageResource(android.R.drawable.ic_media_play);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                //new SettingsDialog().show(getSupportFragmentManager(), DIALOG_SETTINGS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_BLUETOOTH_ADMIN:
                isPermissionGranted = grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                Log.d(TAG, "Permission " + (isPermissionGranted ? "GRANTED" : "DENIED"));
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View view) {
        if (active == null) {
            return;
        }

        final BaseService service = active.getService();
        if (service == null) {
            Log.d(TAG, "unknown service for " + active.getTag());
            return;
        }

        final int snackText;
        if (service.isRunning()) {
            Log.d(TAG, "start service: " + service.getClass().getSimpleName());
            service.stop();
            snackText = R.string.main_service_stop;
            fab.setImageResource(android.R.drawable.ic_media_play);
        } else {
            final int audioPermission = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_ADMIN);
            if (isPermissionGranted == null && audioPermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                        PERMISSIONS_REQUEST_BLUETOOTH_ADMIN);
                return;
            }
            Log.d(TAG, "stop service: " + service.getClass().getSimpleName());
            service.start();
            snackText = R.string.main_service_start;
            fab.setImageResource(android.R.drawable.ic_media_pause);
        }

        fab.setEnabled(false);
        Snackbar.make(view, snackText, Snackbar.LENGTH_SHORT)
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(final Snackbar snackbar, final int event) {
                        super.onDismissed(snackbar, event);
                        fab.setEnabled(true);
                    }
                })
                .show();
    }

    /** Spinner adapter. */
    private static class SpinnerAdapter extends ArrayAdapter<String>
            implements ThemedSpinnerAdapter {
        /** Drop down helper. */
        private final ThemedSpinnerAdapter.Helper dropDownHelper;

        public SpinnerAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            dropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            final View view;
            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = dropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            final TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));
            return view;
        }

        @Override
        public Resources.Theme getDropDownViewTheme() {
            return dropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Resources.Theme theme) {
            dropDownHelper.setDropDownViewTheme(theme);
        }
    }

}
