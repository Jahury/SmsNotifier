package com.apptech.smsdispatcher.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.apptech.smsdispatcher.Defines;
import com.apptech.smsdispatcher.R;
import com.apptech.smsdispatcher.signalr.SignalRService;
import com.apptech.smsdispatcher.utils.DeviceInfo;
import com.apptech.smsdispatcher.utils.Utils;
import com.microsoft.signalr.HubConnectionState;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.prefs.Prefs;

public class MainSignalActivity extends AppCompatActivity {

    private static final int PERMISSION_SEND_SMS = 123;
    private static final int PERMISSION_READ_STATE = 155;


    private ArrayAdapter<String> arrayAdapter;
    private TextView tvLogger;


    private final Context mContext = this;
  //  private SignalRService mService;
    private boolean mBound = false;
    private Switch switcher;
    private EditText etKeyname;
         TextView   etKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ms_signalr);
        initViews();
      //  connect();
    }




    private void connect() {
        if (!Utils.isOnline(getBaseContext())) {
            switcher.setChecked(false);
            log("تعذر الأتصال بالأنترنت, يجب ان تكون متصلا بالأنترنت ");
        return;
        }
        if (etKeyname.getText()==null || etKeyname.getText().length()==0) {
            switcher.setChecked(false);
            log("Please enter Target Name ");
            return;
        }

        Prefs.with(this).write("Keyname",etKeyname.getText().toString());
        Defines.TargetName=etKeyname.getText().toString();
        try {
            Intent intent = new Intent();
            intent.setClass(mContext, SignalRService.class);

            SignalRService mSensorService = new SignalRService(getApplicationContext());
            if (isMyServiceRunning(mSensorService.getClass())) {
            //    mSensorService.startSignalR();

                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode");
                startForegroundService(intent);
            }
            else{
            startService(intent);

            }
        } catch (Exception e) {
            log(e.getMessage());
        }
    }
    private void initViews() {

        tvLogger = findViewById(R.id.tvLog);
        switcher = (Switch) findViewById(R.id.switchService);
        switcher.setTextOff("OFF");
        switcher.setTextOn("ON");
        switcher.setChecked(false);
        switcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            initPermissions();
            connect();
        });

        SignalRService mSensorService = new SignalRService(getApplicationContext());
        if (isMyServiceRunning(mSensorService.getClass())) {
            switcher.setChecked(true);
            mSensorService.startSignalR();
            return;
        }
        etKey = findViewById(R.id.etKey);
        etKeyname = findViewById(R.id.etKeyname);

        String keyname=Prefs.with(this).read("Keyname","");
        etKeyname.setText(keyname);
        if(keyname!=null && keyname.length()>1){
            switcher.setChecked(true);
        }
        Defines.TargetName=etKeyname.getText().toString();

        String deviceId=DeviceInfo.getUniqueDeviceId();
        etKey.setText(deviceId);
        etKey.setOnClickListener(view ->{
            Utils.copyToClipboard(getBaseContext(),deviceId);
            log("تم النسخ المفتاح");
        });

        initAdapter();
        initSendButton();
   //     log(Defines.BaseUrl);
    }
    private void initSendButton() {
        Button sendButton = findViewById(R.id.bSend);
        final EditText editText = findViewById(R.id.etMessageText);
        sendButton.setOnClickListener(view -> {
            String message = editText.getText().toString();
            editText.setText("");
            try {
                //mService.sendMessage( message);
            } catch (Exception e) {
                log(e.getMessage());
            }
        });


        sendButton.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);
//        findViewById(R.id.btnConnect).setVisibility(View.GONE);
        findViewById(R.id.btnConnect).setOnClickListener(view->{

            SignalRService mSensorService = new SignalRService(getApplicationContext());
            if (isMyServiceRunning(mSensorService.getClass())) {

                log("Service is running");
        /*    HubConnectionState state=mSensorService.getStatus();
            if(state==HubConnectionState.CONNECTED)
                log("CONNECTED");
            else
            {
                log("NOT CONNECTED");
                mSensorService.startSignalR();
                log("RE-CONNECTED");
            }*/
        }
            else
            {
               // log("قم بتشغل الهوست اولاَ");
connect();
            }
        });
    }

    private void initAdapter() {
        ListView listView = (ListView)findViewById(R.id.lvMessages);
        List<String> messageList = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(MainSignalActivity.this,
                android.R.layout.simple_list_item_1, messageList);
        arrayAdapter.add("TargetName: "+Defines.TargetName);
        listView.setAdapter(arrayAdapter);
    }
    void log(final String msg){
        try {
            runOnUiThread(() -> {
                try {
                    tvLogger.setText(msg);
                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();

                    arrayAdapter.add(msg);
                    arrayAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initPermissions() {
        requestReceiveSmsPermission();
        requestSmsPermission();
        requestPhoneStatePermission();
    }

    private void requestSmsPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_SEND_SMS);
            switcher.setChecked(false);
        }
        else {
            log("SEND_SMS Permission is allowed");
        }
    }
    private void requestPhoneStatePermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSION_READ_STATE);
        }
        else {
            log("PhoneState Permission is allowed");
        }
    }
    private void requestReceiveSmsPermission() {
        String permission = Manifest.permission.RECEIVE_SMS;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if ( grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
            switcher.setChecked(false);
        }
        else {
            log("RECEIVE_SMS Permission is allowed");
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
}
