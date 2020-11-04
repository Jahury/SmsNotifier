package com.apptech.smsdispatcher.signalr;

import android.os.AsyncTask;
import android.telephony.SmsManager;

import com.microsoft.signalr.HubConnection;

import static com.apptech.smsdispatcher.rest.SmsApi.GetParts;

/*
public class HubConnectionTask extends AsyncTask<HubConnection, Void, HubConnection> {

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

    }

}*/
