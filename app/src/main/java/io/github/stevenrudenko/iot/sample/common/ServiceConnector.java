package io.github.stevenrudenko.iot.sample.common;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

/**
 * Service connector.
 * @param <S> service type to connect to.
 */
public class ServiceConnector<S extends Service> implements ServiceConnection {

    /** Service instance. */
    private volatile S service;

    /** Service listener. */
    private final OnServiceConnectionListener<S> listener;

    public ServiceConnector(@NonNull OnServiceConnectionListener<S> listener) {
        this.listener = listener;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //noinspection unchecked
        final LocalBinder<S> binder = (LocalBinder<S>) service;
        this.service = binder.getSerivce();
        listener.onServiceBind(this.service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
        listener.onServiceUnbind();
    }

    public boolean isConnected() {
        return service != null;
    }

    public S getService() {
        return service;
    }

    public void bindService(Context context, Class<? extends S> clazz) {
        final Intent service = getIntent(context, clazz);
        context.startService(service);
        context.bindService(service, this, 0);
    }

    public void unbindService(Context context, boolean forceStop) {
        if (service != null) {
            context.unbindService(this);
            if (forceStop) {
                context.stopService(getIntent(context, service.getClass()));
            }
        }
    }

    private Intent getIntent(Context context, Class<? extends Service> clazz) {
        return new Intent(context, clazz);
    }

    /**
     * Service connection listener.
     * @param <S> service type.
     */
    public interface OnServiceConnectionListener<S> {
        /**
         * Notifies that service has been connected.
         * @param service service instance.
         */
        void onServiceBind(S service);

        /** Notifies that service has been disconnected. */
        void onServiceUnbind();
    }

}
