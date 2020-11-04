package com.apptech.smsdispatcher.rest;

import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.Map;

//import static android.support.v7.widget.StaggeredGridLayoutManager.TAG;

/**
 * Created by Jahury on 12/21/2016.
 */

public class RestRequest extends StringRequest {

    public static final int MissingStatusCode = 9999;
    private static final String TAG ="RestRequest" ;
    private static final String PROTOCOL_CHARSET = "utf-8";
    Map<String, String> params;
    private String mAccessToken;
    private int requestStatusCode;
    private String requestUrl;
    private  Response.Listener<String> successListener;
    private  String requestBody;
    private Map<String, String> mHeaders;

    public RestRequest(int method, String url, String requestBody, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method,url,listener,errorListener);
        this.requestBody = requestBody;
        this.successListener = listener;
        this.params = null;
    }
    public RestRequest(int method, String url, String requestBody, Response.Listener<String> listener, Response.ErrorListener errorListener,String accessToken) {
        super(method,url,listener,errorListener);
        this.requestBody = requestBody;
        this.successListener = listener;
        this.params = null;
        this.mAccessToken=accessToken;
    }
    public RestRequest(int method, String url, String requestBody, Response.Listener<String> listener, Response.ErrorListener errorListener,  Map<String, String> headers) {
        super(method,url,listener,errorListener);
        this.requestBody = requestBody;
        this.successListener = listener;
        this.params = null;
        this.mHeaders=headers;
    }
    public RestRequest(int method, String url,  Map<String, String> params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method,url,listener,errorListener);
        this.params = params;
        this.successListener = listener;
        this.requestBody = null;
    }

    public RestRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method,url,listener,errorListener);
        this.params = null;
        this.requestBody = null;
        this.successListener = listener;
    }

    public RestRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener,String accessToken) {
        super(method,url,listener,errorListener);
        this.params = null;
        this.requestBody = null;
        this.successListener = listener;
        this.mAccessToken=accessToken;
    }


    public RestRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener,  Map<String, String> headers) {
        super(method,url,listener,errorListener);
        this.params = null;
        this.requestBody = null;
        this.successListener = listener;
        this.mHeaders=headers;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }
    /*    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
      headers.put("Client-Version", "0.1");
      headers.put("Device-Token", "");
    //   headers.put("Device-Token", CoreDroidApp.DeviceID);
   //    Determine if request should be authorized.
           // String credentials =  mAccessToken + ":";
          //  String encodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
           if(mAccessToken!=null)
            headers.put("Authorization", "Bearer " + mAccessToken);

        return headers;
    }*/

    @Override
    protected Map<String,String> getParams(){
        return params;
    }

    @Override
    public byte[] getBody() {
        try {
            return requestBody == null ? null : requestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, PROTOCOL_CHARSET);
        //    Log.i(com.apptech.smshost.Defines.TAG,"UnsupportedEncodingException="+"Unsupported Encoding while trying to get the bytes of %s using %s");
            return null;
        }
    }

    @Override
    protected void deliverResponse(@NonNull String response) {
        successListener.onResponse(response);
    }

    @Override
    public String getBodyContentType() {
        return String.format("application/json; charset=utf-8");
    }
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            requestStatusCode = response.statusCode;
       //     if (BuildConfig.DEBUG)
         //       Log.i(com.apptech.smshost.Defines.TAG,"%s URL: %s. ResponseCode: %d"+ this.getClass().getSimpleName()+ requestUrl);
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
        return super.parseNetworkResponse(response);
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        if (volleyError.networkResponse != null) {
            // Save request status code
            requestStatusCode = volleyError.networkResponse.statusCode;

                // If Token expired. Logout user and redirect to home page.
          /*      if (getStatusCode() == HttpURLConnection.HTTP_FORBIDDEN && fragmentManager != null) {
                    //   LoginDialogFragment.logoutUser();
                    //     DialogFragment loginExpiredDialogFragment = new LoginExpiredDialogFragment();
                    //    loginExpiredDialogFragment.show(fragmentManager, LoginExpiredDialogFragment.class.getSimpleName());
                }*/
        } else {
            requestStatusCode = MissingStatusCode;
        }
        return super.parseNetworkError(volleyError);
    }

    public int getStatusCode() {
        return requestStatusCode;
    }
}
