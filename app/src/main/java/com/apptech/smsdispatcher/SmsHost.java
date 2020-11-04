package com.apptech.smsdispatcher;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.blankj.utilcode.util.Utils;

public class SmsHost extends Application {

    private static final  String TAG =Defines.TAG;
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;

    private static SmsHost mInstance;
    public static synchronized SmsHost getInstance() {
        return mInstance;
    }
    @Override
    public void onCreate () {

        super.onCreate();
        mInstance = this;
        Utils.init(this);
        if (!BuildConfig.DEBUG) {
            //initExceptionHandler();
        }
    }


    private void initExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        Log.i("volley", "handleUncaughtException: " + e.getMessage());

        System.exit(1); // kill off the crashed app
        /*try {
            Intent broadcastIntent = new Intent("com.android.techtrainner");
            sendBroadcast(broadcastIntent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    public static DefaultRetryPolicy getCustomRetryPolice() {
        return new DefaultRetryPolicy(20000,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    public static DefaultRetryPolicy getDefaultRetryPolice() {
        return new DefaultRetryPolicy(10000,
                2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    public static DefaultRetryPolicy getOneRetryPolice() {
        return new DefaultRetryPolicy(0,-1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        // solved 0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        //DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    }



    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }


    public  <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
        System.setProperty("http.keepAlive", "false");
    }


    public  <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public static void cancelPendingRequests(Object tag) {
        if(tag==null)
            return;
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
