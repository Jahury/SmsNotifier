package com.apptech.smsdispatcher.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.apptech.smsdispatcher.Defines;
import com.apptech.smsdispatcher.R;
import com.apptech.smsdispatcher.entities.SmsMessage;
import com.apptech.smsdispatcher.rest.SmsApi;
import com.apptech.smsdispatcher.smsradar.Sms;
import com.apptech.smsdispatcher.smsradar.SmsListener;
import com.apptech.smsdispatcher.smsradar.SmsRadar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_SEND_SMS = 123;
    private static final int PERMISSION_READ_STATE = 155;
    private static final String TAG = "frenchi_sms";
    private Switch switcher;
TextView tvLogger;
    String SmsPrefix;
    //String HostNumber="777600078";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switcher = (Switch) findViewById(R.id.switchService);
        switcher.setTextOff("OFF");
        switcher.setTextOn("ON");
        switcher.setChecked(false);
        findViewById(R.id.btnInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(Defines.BaseUrl);
            }
        });

        tvLogger = findViewById(R.id.tvLog);
        try {
        log("Url  : "+Defines.BaseUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initPermissions();
        hookListeners();


      //  ArrayList<String> parts= SmsApi.GetParts("قم بتحويل / سحب / أرسال مبالغ مالية قم بتحويل / سحب / أرسال مبالغ ");
      //  ArrayList<String> parts2= SmsApi.GetParts("قم بتحويل / سحب / أرسال مبالغ مالية قم بتحويل / سحب / أرسال مبالغ قم بتحويل / سحب / أرسال مبالغ مالية");
      //  ArrayList<String> parts3= SmsApi.GetParts("قم بتحويل / سحب / أرسال مبالغ مالية قم بتحويل / سحب / أرسال مبالغ قم بتحويل / سحب / أرسال مبالغ قم بتحويل / سحب / أرسال مبالغ ماليةقم بتحويل / سحب / أرسال مبالغ ماليةقم بتحويل / سحب / أرسال مبالغ مالية");

   //    new SmsApi().dispatchSms(null,new SmsMessage("قم بتحويل / سحب / أرسال مبالغ مالية قم بتحويل / سحب / أرسال مبالغ قم بتحويل / سحب / أرسال مبالغ مالية","777600078"));
    }

    private void initPermissions() {
        requestReceiveSmsPermission();
        requestSmsPermission();
        requestPhoneStatePermission();
    }

    void log(final String msg){
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        tvLogger.setText(tvLogger.getText() +System.getProperty("line.separator") +msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void hookListeners() {
        switcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getBaseContext(), "SmsHost has been started", Toast.LENGTH_SHORT);
            if(isChecked){
                log("Host Started");
                initPermissions();
                if(!sendSmsPermittedd()){
                  switcher.setChecked(false);
                  showToast("قم بتفعيل بسماحية ارسال الرسائل النصية");
                  return;
              }
                try{
                    stopSmsRadarService();
                }
                catch (Exception e){}
                try {
                    initializeSmsRadarService();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //  startService();
            }
            else {
                Toast.makeText(getBaseContext(), "SmsHost has been Stopped", Toast.LENGTH_SHORT);
                log("Host Stopped");
                try {
                    stopSmsRadarService();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //  stopService();
            }
        });
    }

    private boolean sendSmsPermittedd() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_SEND_SMS);

            return false;
        }
        else {
            log("SEND_SMS Permission is allowed");
            return true;
        }
    }

    //region sms listener
    private void initializeSmsRadarService() {
        log("Start to Listen for SMS");
        SmsRadar.initializeSmsRadarService(this, new SmsListener() {
            @Override
            public void log(String message) {
                showToast(message);
            }

            @Override
            public void onSmsSent(Sms sms) {
                showToast("SmsReceived: ");
            }

            @Override
            public void onSmsReceived(Sms sms) {
                Log.i(TAG,"SmsReceived: ");
                try {
                showToast("Transaction SmsReceived: ");
                    showToast("sms arrived from : "+sms.getMsg());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void stopSmsRadarService() {
        SmsRadar.stopSmsRadarService(this);
    }
    //endregion

    private void showToast(final String msg) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                        log(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void sendSms(String phoneNumber, String message){
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            showToast(e.getMessage());
            e.printStackTrace();
        }
    }

    //region permissions
    private void requestSmsPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_SEND_SMS);
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
        }
        else {
            log("RECEIVE_SMS Permission is allowed");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_SEND_SMS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                  //  sendSms("dfsd", "sf");
                } else {
                    // permission denied
                }
                return;
            }
        }
    }
    //endregion
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.ab_endpoint:
                showToast(Defines.BaseUrl);
                break;

        }
        return true;
    }

}
