package io.github.stevenrudenko.iot.sample.common;

import android.app.Service;
import android.os.Binder;

import java.lang.ref.WeakReference;

/**
 * Local binder.
 * @param <T> service type.
 */
public class LocalBinder<T extends Service> extends Binder {

    /** Service weak reference. */
    private final WeakReference<T> service;

    public LocalBinder(T service) {
        this.service = new WeakReference<>(service);
    }

    public T getSerivce() {
        return service.get();
    }

}
