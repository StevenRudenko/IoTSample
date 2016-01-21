package io.github.stevenrudenko.iot.sample.sensor.core.io;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Used to intercept audio jack button clicks. */
public class RemoteControlReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // intercept action
        abortBroadcast();
    }

}