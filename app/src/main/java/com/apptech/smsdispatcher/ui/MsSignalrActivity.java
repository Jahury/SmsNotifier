package com.apptech.smsdispatcher.ui;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
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
import com.microsoft.signalr.Action1;
import com.microsoft.signalr.Action2;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import static com.apptech.smsdispatcher.rest.SmsApi.GetParts;

public class MsSignalrActivity extends AppCompatActivity {
    private static final int PERMISSION_SEND_SMS = 123;
    private static final int PERMISSION_READ_STATE = 155;

    private ArrayAdapter<String> arrayAdapter;
    private TextView tvLogger;
    HubConnection hubConnection=null ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ms_signalr);

        initViews();
        initHub();
        hubConnection.on("ReceiveMessage", (message, phoneNumber) -> runOnUiThread(() -> {
            sendSms(message, phoneNumber);
        }),String.class,String.class);

        connect();
    }

    private void sendSms(String message, String phoneNumber) {
        arrayAdapter.add("sno:"+phoneNumber +" | " +message);
        arrayAdapter.notifyDataSetChanged();
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendMultipartTextMessage(phoneNumber, null, GetParts(message), null, null);
        } catch (Exception e) {
            log("couldnt send sms ,details: " + e.getMessage());
        }
    }

    private void initSendButton() {
        Button sendButton = (Button)findViewById(R.id.bSend);
        final EditText editText = (EditText)findViewById(R.id.etMessageText);
        sendButton.setOnClickListener(view -> {
            String message = editText.getText().toString();
            editText.setText("");
            try {
                hubConnection.send("Send", message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        findViewById(R.id.btnConnect).setOnClickListener(view->{
            connect();
        });
    }

    private void initViews() {
        log(Defines.BaseUrl);
        tvLogger = findViewById(R.id.tvLog);
        Switch  switcher = (Switch) findViewById(R.id.switchService);
        switcher.setTextOff("OFF");
        switcher.setTextOn("ON");
        switcher.setChecked(false);
        switcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            initPermissions();
            connect();
        });
        initAdapter();
        initSendButton();
    }

    private void initHub() {
        try {
            hubConnection = HubConnectionBuilder.create("http://207.180.241.132:6662/chatHub?username="+Defines.TargetName).build();
            log("Hub Initilaized");
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    private void initAdapter() {
        ListView listView = (ListView)findViewById(R.id.lvMessages);
        List<String> messageList = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(MsSignalrActivity.this,
               android.R.layout.simple_list_item_1, messageList);
        arrayAdapter.add("First");
        listView.setAdapter(arrayAdapter);
    }

    private void connect() {
        try {
            new HubConnectionTask().execute(hubConnection);
        } catch (Exception e) {
            log(e.getMessage());
        }
    }

    void log(final String msg){
        try {
            runOnUiThread(() -> {
                try {
                    tvLogger.setText(tvLogger.getText() +System.getProperty("line.separator") +msg);
                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
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


    class HubConnectionTask extends AsyncTask<HubConnection, Void, HubConnection>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected HubConnection doInBackground(HubConnection... hubConnections) {
            HubConnection hubConnection = hubConnections[0];
            hubConnection.start().blockingAwait();
            return hubConnection;
        }

        @Override
        protected void onPostExecute(HubConnection hubConnection) {
            hubConnection.invoke("GetConnectionId");
            log("Hub Connected");
        }
    }
}