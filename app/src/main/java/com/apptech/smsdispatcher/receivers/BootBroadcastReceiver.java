package com.apptech.smsdispatcher.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.apptech.smsdispatcher.signalr.SignalRService;

public class BootBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch the specified service when this message is received
        Intent startServiceIntent = new Intent(context, SignalRService.class);
        startWakefulService(context, startServiceIntent);
    }
}
