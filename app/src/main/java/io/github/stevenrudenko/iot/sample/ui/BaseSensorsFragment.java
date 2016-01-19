package io.github.stevenrudenko.iot.sample.ui;

import android.app.Fragment;
import android.os.Bundle;

import io.github.stevenrudenko.iot.sample.common.ServiceConnector;
import io.github.stevenrudenko.iot.sample.sensor.BaseService;

/** Base sensors fragment. */
public abstract class BaseSensorsFragment extends Fragment
        implements ServiceConnector.OnServiceConnectionListener<BaseService>,
        BaseService.OnSensorUpdateListener {

    /** Service connection. */
    private final ServiceConnector<BaseService> conn = new ServiceConnector<>(this);

    protected abstract Class<? extends BaseService> getServiceClass();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conn.bindService(getActivity(), getServiceClass());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (conn.isConnected()) {
            conn.getService().removeListener(this);
        }
        conn.unbindService(getActivity(), true);
    }

    public BaseService getService() {
        return conn.getService();
    }

    @Override
    public void onServiceBind(BaseService service) {
        service.addListener(this);
    }

    @Override
    public void onServiceUnbind() {
    }

}
