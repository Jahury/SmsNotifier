package com.apptech.smsdispatcher.rest;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.apptech.smsdispatcher.License;
import com.apptech.smsdispatcher.SmsHost;
import com.apptech.smsdispatcher.utils.DeviceInfo;

import java.util.HashMap;
import java.util.Map;

import static com.apptech.smsdispatcher.rest.VolleyUtils.parseError;

/**
 * Created by Jahury on 4/23/2017.
 */
public class ApiProvider implements IApiProvider {
    private static final String TAG = "smshost" ;

    //public String SessionID;
    public String Url;
    public String JsonBody;
    public String Tag;
    public int Method=0;
    public Map<String, String> mHeaders=new HashMap<>();
    public boolean UseTimeout =false;
    public boolean Cache =false;
    OnResponseListner Listener;

    public ApiProvider () {
        mHeaders = new HashMap<>();

        if(mHeaders.isEmpty()){
    try {
        mHeaders.put("Api-Key","SfSowleejxCY7v+LkbsISUB+UilvCXXu0y/dzQ1SZvQ=");
        mHeaders.put("Agent-Key","");
        mHeaders.put("Keyname", License.Flavor);
        mHeaders.put("DeviceID",DeviceInfo.getDeviceId());
        mHeaders.put("Identifier", DeviceInfo.getAndroidID());
    } catch (Exception e) {
        e.printStackTrace();
    }

}

    }

    public ApiProvider (String url) {
        Url = url;
    }

    @Override
    public void  execute(ApiRequest apiInfo, final OnResponseListner listner) {
        Log.i(TAG, "Api Provider - sendRequest");


        if(apiInfo.getRequestMethod()==Request.Method.POST) {
            executePostRequest(apiInfo.getUrl(), apiInfo.getJsonBody(), apiInfo.getTag(), listner);
        }
       else  if(apiInfo.getRequestMethod()==Request.Method.GET) {
            executeGetRequest(apiInfo.getUrl(), apiInfo.getTag(), listner);
        }
    }


    public void  execute(final OnResponseListner listner) {

        Log.i(TAG, "Api Provider execute : ");
        if(Method==Request.Method.POST) {
            executePostRequest(Url, JsonBody,Tag, listner);
        }
        else  if(Method==Request.Method.GET) {
            executeGetRequest(Url, Tag, listner);
        }
    }
    private void executeGetRequest (String url, String tag, final OnResponseListner listner) {

        url= url.replace(" ", "20%");
        Log.i(TAG, "executeGetRequest ");
        Log.i(TAG, "url: "+url);
        RestRequest request = new RestRequest(Request.Method.GET, url, new Response.Listener<String>()
        {
            @Override
            public void onResponse (String response) {
                Log.i(TAG, "response>>>" + response);
                listner.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError error) {
                String message=  parseError(error);
                listner.onFailure(message);
            }
        },mHeaders);//
      //  request.setRetryPolicy(CoreDroid.getDefaultRetryPolice());
        request.setRetryPolicy(SmsHost.getOneRetryPolice());
        //request.setShouldCache(Cache);
        SmsHost.getInstance().addToRequestQueue(request, tag);
        Log.i(TAG, "Request Added To Volley Queue: ");

    }
    private void executePostRequest (String url, String jsonBody, String tag, final OnResponseListner listner){
        Log.i(TAG, "executePostRequest: ");
        Log.i(TAG, "url: "+url);
        Log.i(TAG, "jsonBody: "+jsonBody);
        RestRequest request = new RestRequest(Request.Method.POST, url, jsonBody, new Response.Listener<String>()
        {
            @Override
            public void onResponse (String response) {
                Log.i(TAG, "response>>>" + response);
                listner.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError error) {
                String message=  parseError(error);
                listner.onFailure(message);
            }
        },mHeaders);
        if(UseTimeout)
        {
            Log.i(TAG, "UseTimeout ");
            request.setRetryPolicy(SmsHost.getCustomRetryPolice());
        }
       else
        request.setRetryPolicy(SmsHost.getOneRetryPolice());
        request.setShouldCache(false);
        SmsHost.getInstance().addToRequestQueue(request, tag);
        Log.i(TAG, "Request Added To Volley Queue: ");
    }


}
