package com.apptech.smsdispatcher.smsradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.apptech.smsdispatcher.signalr.SignalRService;

public class ReceiverCall extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Service Stops", "Restart Service From Signal");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, SignalRService.class));
        }
        else
            context.startService(new Intent(context, SignalRService.class));
    }

}
