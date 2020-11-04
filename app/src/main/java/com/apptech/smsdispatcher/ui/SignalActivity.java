package com.apptech.smsdispatcher.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.apptech.smsdispatcher.R;
import com.smartarmenia.dotnetcoresignalrclientjava.HubConnection;
import com.smartarmenia.dotnetcoresignalrclientjava.HubConnectionListener;
import com.smartarmenia.dotnetcoresignalrclientjava.HubEventListener;
import com.smartarmenia.dotnetcoresignalrclientjava.HubMessage;
import com.smartarmenia.dotnetcoresignalrclientjava.WebSocketHubConnection;

import java.util.Date;

public class SignalActivity extends AppCompatActivity implements HubConnectionListener, HubEventListener {

    TextView tvLog;
    final HubConnection connection = new WebSocketHubConnection("http://207.180.241.132:6662/chatHub", "Bearer your_token");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal);
        tvLog = findViewById(R.id.tvLog);
        tvLog.setText("Logger...");
        connection.addListener(this);
        connection.subscribeToEvent("ReceiveMessage", this);
        //connection.connect();
        connect();
    //    connection.invoke("SendMessage", "Hello");


        findViewById(R.id.btnConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    connect();
                } catch (Exception e) {
                    showMessage("Couldnt sent error :"+e.getMessage());
                }
            }
        });

        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    connection.invoke("SendMessage", "Hello By Button "+new Date().getTime());
                } catch (Exception e) {
      showMessage("Couldnt sent error :"+e.getMessage());
                }
            }
        });
    }

    @Override
    public void onConnected() {
showMessage("onConnected");
    }

    @Override
    public void onDisconnected() {
        showMessage("onDisconnected");
    }

    @Override
    public void onMessage(HubMessage message) {
        showMessage("onMessage "+message.getTarget());
    }

    @Override
    public void onError(Exception exception) {
        showMessage("onError"+exception.getMessage());
    }

    @Override
    public void onEventMessage(HubMessage message) {
        showMessage("onEventMessage: "+message.getTarget());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.removeListener(this);
        connection.unSubscribeFromEvent("Send", this);
        connection.disconnect();
    }
    private void connect() {
        try {

            connection.connect();



        } catch (final Exception ex) {
            String msg=ex.getMessage();
            showMessage("onConnect "+msg);
        }
    }

    private void showMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLog.setText(tvLog.getText() +" | "+ msg);
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();

            }
        });
    }
}
