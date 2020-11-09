package com.apptech.smsdispatcher.signalr;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.ServiceState;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.apptech.smsdispatcher.Defines;
import com.apptech.smsdispatcher.R;
import com.apptech.smsdispatcher.smsradar.OreoNotification;
import com.apptech.smsdispatcher.smsradar.TimeProvider;
import com.apptech.smsdispatcher.ui.MainActivity;
import com.apptech.smsdispatcher.ui.MainSignalActivity;
import com.apptech.smsdispatcher.ui.MsSignalrActivity;
import com.apptech.smsdispatcher.utils.Utils;
import com.microsoft.signalr.Action2;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;


import es.dmoral.prefs.Prefs;

import static com.apptech.smsdispatcher.rest.SmsApi.GetParts;

public class SignalRService extends Service {
    private HubConnection mHubConnection;
    private Handler mHandler; // to display Toast message

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";
    private AlarmManager alarmManager;
    private TimeProvider timeProvider;
    private boolean initialized;

    private Timer timer;
    private TimerTask timerTask;

   boolean isServiceStarted = false;
    Context context;

    public SignalRService(Context applicationContext) {
        super();
        context = applicationContext;
        Log.i("HERE", "here service created!");
    }
    public SignalRService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mHandler = new Handler(Looper.getMainLooper());
        } catch (Exception e) {
            e.printStackTrace();
        }
        startForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      super.onStartCommand(intent, flags, startId);

        try {
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        startSignalR();
        startTimer();
        return START_STICKY;

    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 30000, 30000); //
    }
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
          //     log("SmsNotifier is running");
               startSignalR();
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public HubConnectionState getStatus(){
        if(mHubConnection!=null)
        return  mHubConnection.getConnectionState();

        return HubConnectionState.DISCONNECTED;
    }
    public void sendMessage(String message) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mHubConnection.send("Send", message);
                } catch (Exception e) {
                    log(e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    /**
     * method for clients (activities)
     */
    public void sendMessage_To(String receiverName, String message) {
        String SERVER_METHOD_SEND_TO = "Send";
        mHubConnection.invoke(SERVER_METHOD_SEND_TO, receiverName, message);
    }

    public void startSignalR() {
        try {

            if(mHubConnection!=null && mHubConnection.getConnectionState()==HubConnectionState.CONNECTED){
                return;
            }
            if (!Utils.isOnline(this)) {
                log("no internet connection ");
                return;
            }
            mHubConnection = HubConnectionBuilder.create("http://207.180.210.165:6663/chatHub?username="+ Defines.TargetName).build();
            try {
                mHubConnection.setServerTimeout(60000);
                mHubConnection.setKeepAliveInterval(15000*10);
                mHubConnection.on("ReceiveMessage",
                        (message, phoneNumber) -> sendSms(message, phoneNumber), String.class, String.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mHubConnection.onClosed( x->
                            {
                                log("connection closed");
                            }
                    );

            new HubConnectionTask().execute(mHubConnection);

            log("Hub Initilaized");
        } catch (Exception e) {
            log(e.getMessage());
        }



    }
    private void sendSms(String message, String phoneNumber) {
        try {
            log("ReceiveMessage" );

            if(message==null || phoneNumber==null)
                return;
            SmsManager sms = SmsManager.getDefault();
            if (message.length() < 70)
            sms.sendTextMessage(phoneNumber, null, message, null, null);
           else{

               ArrayList<String> messages=GetParts(message);
                for (String s : messages) {
                    sms.sendTextMessage(phoneNumber, null, s, null, null);
                }
               // sms.sendMultipartTextMessage(phoneNumber, null, GetParts(message), null, null);
            }
            log("Sms Sent Successfully" );
        } catch (Exception e) {
            log("couldnt send sms ,details: " + e.getMessage());
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("EXIT", "ondestroy!");
        stoptimertask();

        try {
            Intent broadcastIntent = new Intent("com.android.techtrainner");
            sendBroadcast(broadcastIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void log(String message) {
        try {
           // if(mHandler!=null)
//            mHandler.post(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void finishService() {
        initialized = false;
        //	unregisterSmsContentObserver();
    }

    private void restartService() {
        Intent intent = new Intent(this, SignalRService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        long now = getTimeProvider().getDate().getTime();
        getAlarmManager().set(AlarmManager.RTC_WAKEUP, now + 1000, pendingIntent);

    }
    private TimeProvider getTimeProvider() {
        return timeProvider != null ? timeProvider : new TimeProvider();
    }

    private AlarmManager getAlarmManager() {
        return alarmManager != null ? alarmManager : (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try {
            //restartService();
            Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
            restartServiceIntent.setPackage(getPackageName());
            startService(restartServiceIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
            super.onTaskRemoved(rootIntent);
    }

    private void startForeground() {
        try {
            Intent notificationIntent = new Intent(this, MainSignalActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                OreoNotification oreoNotification = new OreoNotification(this);
                Notification.Builder builder = oreoNotification.getOreoNotification("AppTech SmsNotifier", "Listening for incoming message", pendingIntent);
                startForeground(NOTIF_ID,builder.build());
            }
            else{

                startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                        NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                        .setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("SMS Host is running")
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    class HubConnectionTask extends AsyncTask<HubConnection, Void, HubConnection>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected HubConnection doInBackground(HubConnection... hubConnections) {
            HubConnection hubConnection = null;
            try {
                hubConnection = hubConnections[0];
                hubConnection.start().blockingAwait();
            } catch (Exception e) {
                return null;
            }

        //    log("Hub Started ConnectionId"+hubConnection.getConnectionId());
            return hubConnection;
        }

        @Override
        protected void onPostExecute(HubConnection hubConnection) {

            try {
                if(hubConnection!=null && hubConnection.getConnectionState()==HubConnectionState.CONNECTED)
                {
                    hubConnection.invoke("GetConnectionId");

                log("Hub Connected");
                }
            } catch (Exception e) {
               log( e.getMessage());
            }

            //long tl=hubConnection.getKeepAliveInterval();
            //long timeout=hubConnection.getServerTimeout();
        }
    }
}
