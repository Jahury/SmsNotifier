package com.apptech.smsdispatcher.rest;

import android.telephony.SmsManager;
import android.util.Log;

import com.android.volley.Request;
import com.apptech.smsdispatcher.Defines;
import com.apptech.smsdispatcher.entities.QueryResponse;
import com.apptech.smsdispatcher.entities.SmsMessage;
import com.apptech.smsdispatcher.entities.SmsRequest;
import com.apptech.smsdispatcher.entities.SmsResponse;
import com.apptech.smsdispatcher.smsradar.Sms;
import com.apptech.smsdispatcher.smsradar.SmsListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.apptech.smsdispatcher.Defines.TAG;
import static java.lang.Thread.sleep;

public class  SmsApi {
    public void sendRequest(final SmsListener smsListener, final Sms sms) {
        String SmsPrefix;
        //SmsPrefix = getString(R.string.sms_prefix);
        SmsPrefix = "$0#";
        //SmsPrefix = "$r#";
        if(!sms.getMsg().startsWith(SmsPrefix)){
            return;
        }

        try{
        if(smsListener!=null)
            smsListener.onSmsReceived(sms);
        }
        catch(Exception e){}
        SmsRequest request=new SmsRequest();
        request.MSG=sms.getMsg();
      //  request.MSG=sms.getMsg().replace("$r#","$0#");
        request.SNO=sms.getAddress();
        String json = new Gson().toJson(request);
        ApiProvider api = new ApiProvider();
        api.Method= Request.Method.POST;
        api.JsonBody=json;
       String url= Defines.BaseUrl;
        api.Url=url+"ExecuteSms";
        Log.i(TAG,"url: "+url);
        api.execute(new OnResponseListner() {
            @Override
            public void onSuccess(String response) {

                Log.i(TAG,"response: "+response);
                SmsResponse smsResponse=new Gson().fromJson(response,SmsResponse.class);
                sendSms(smsResponse.SNO,smsResponse.MSG);
                if(smsResponse.Success){
                }

                if(smsListener!=null){
                    smsListener.log("send success: "+sms.getMsg());
                }

            }
            @Override
            public void onFailure(String error) {
                if(smsListener!=null){
                    smsListener.log("failed to send: "+error);
                }

                Log.i(TAG,"error: "+error);
            }
        });
    }

    private void sendSms(String phoneNumber, String message){
        try {
            if(message==null || message.length()==0)
                return;

            if(message.length()>70){
                try {
                    message=message.substring(0,68);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
          //  showToast(e.getMessage());
            e.printStackTrace();
        }
    }

    public void checkMessages(final SmsListener smsListener) {

        try {
            log(smsListener," checkMessages "+new Date());
            ApiProvider api = new ApiProvider();

            api.Url=Defines.BaseUrl+"PendingSms";
            api.execute(new OnResponseListner() {
                @Override
                public void onSuccess(String response) {
                    try {
                        Log.i(TAG,"fetch sms success: ");
                        QueryResponse smsResponse=new Gson().fromJson(response, QueryResponse.class);
                        if(smsResponse.Success){
                            processRespose(smsListener,smsResponse.Result);
                        }
                        else{
                        log(smsListener,"failed to fetch pending masseges, details: "+smsResponse.Message);
                        }
                    } catch (JsonSyntaxException e) {
                        log(smsListener,"Exception on CheckMessages-onSuccess,details: "+e.getMessage());
                    }

                }
                @Override
                public void onFailure(String error) {
                    log(smsListener,"failed to send: "+error);
                }
            });
        } catch (Exception e) {
            log(smsListener,"Exception on CheckMessages,details: "+e.getMessage());
        }
    }

    private void processRespose(SmsListener smsListener,String result) {

        List<SmsMessage> data;
        try {
            data = new Gson().fromJson(result, new TypeToken<List<SmsMessage>>(){}.getType());
            log(smsListener,"number of pending sms "+data.size());
            if(data.size()>0)
            {
                for (SmsMessage message : data) {
                    try {
                        dispatchSms(smsListener,message);
                        //sleep(2 * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else
                log(smsListener,"no pending messages found");
        } catch (JsonSyntaxException e) {
            log(smsListener,"error on parse result , details: "+e.getMessage());
        }
        catch (Exception e) {
            log(smsListener,e.getMessage()+ " حدث مشكلة ");
        }

    }


    public  void dispatchSms(final SmsListener smsListener, final SmsMessage smsMessage){

        final String phoneNumber=smsMessage.PhoneNumber;
        final String message=smsMessage.Message;
        try {
            if(message==null || message.length()==0)
                return;

            try {
                ApiProvider api = new ApiProvider();
                api.Url=Defines.BaseUrl+"UpdateSmsState/"+smsMessage.ID ;
                api.execute(new OnResponseListner() {
                    @Override
                    public void onSuccess(String response) {
                        Log.i(TAG,"response: "+response);
                        try {
                            SmsManager sms = SmsManager.getDefault();
                            ArrayList<String> messages = GetParts(message);
                            sms.sendMultipartTextMessage(phoneNumber, null, messages, null, null);
                        } catch (Exception e) {
                            log(smsListener,"couldnt send sms ,details: "+e.getMessage());
                        cancelStatus(smsMessage.ID);
                        }
                    }
                    @Override
                    public void onFailure(String error) {
                         log(smsListener,"failed to update status: details: "+error);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            log(smsListener,"couldnt send sms ,details: "+e.getMessage());
            e.printStackTrace();
        }
    }
/*
    public  void dispatchSms(SmsListener smsListener,SmsMessage smsMessage){

        String phoneNumber=smsMessage.PhoneNumber;
         String message=smsMessage.Message;
        try {
            if(message==null || message.length()==0)
                return;
            SmsManager sms = SmsManager.getDefault();

            ArrayList<String> messages = GetParts(message);
          sms.sendMultipartTextMessage(phoneNumber, null, messages, null, null);
         //   sms.sendTextMessage(phoneNumber, null, message, null, null);

            updateStatus(smsMessage.ID);

        } catch (Exception e) {
            log(smsListener,"couldnt send sms ,details: "+e.getMessage());
            e.printStackTrace();
        }
    }
*/

    public static ArrayList<String> GetParts(String message) {
        ArrayList<String> parts = new ArrayList<>();

        try {
            if (message.length() > 140) {
                parts.add(message.substring(0, 70));
                parts.add(message.substring(70,140));
                parts.add(message.substring(140));
            }
           else if (message.length() > 70) {
                parts.add(message.substring(0, 70));
                parts.add(message.substring(70));
            }
            else{
                    parts.add(message);
                }
        } catch (Exception e) {
            parts.add(message.substring(0, 60));
        }
        return parts;
        }

    private void updateStatus(long id) {
        try {
            ApiProvider api = new ApiProvider();
            api.Url=Defines.BaseUrl+"UpdateSmsState/"+id;
            api.execute(new OnResponseListner() {
                @Override
                public void onSuccess(String response) {
                    Log.i(TAG,"response: "+response);
                }
                @Override
                public void onFailure(String error) {
                  //  log(smsListener,"failed to send: "+error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void cancelStatus(long id) {
        try {
            ApiProvider api = new ApiProvider();
            api.Url=Defines.BaseUrl+"RemoveSmsState/"+id;
            api.execute(new OnResponseListner() {
                @Override
                public void onSuccess(String response) {
                  //  Log.i(TAG,"response: "+response);
                }
                @Override
                public void onFailure(String error) {
                    //  log(smsListener,"failed to send: "+error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void log( SmsListener smsListener,String msg){
        try {
            if(smsListener!=null){
                smsListener.log(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
