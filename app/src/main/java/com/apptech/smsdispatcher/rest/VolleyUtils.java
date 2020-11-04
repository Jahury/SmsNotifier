package com.apptech.smsdispatcher.rest;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.apptech.smsdispatcher.Defines;

import org.json.JSONException;
import org.json.JSONObject;

import static android.util.Log.i;

/**
 * Created by Jahury on 3/22/2018.
 */

public class VolleyUtils {
    public static String parseError (VolleyError error) {

        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Unknown error";
        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout";
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server";
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = new JSONObject(result);
                String status = response.getString("status");
                String message = response.getString("message");

                i(Defines.TAG, status);
                i(Defines.TAG, message);

                errorMessage="Error Status: "+ status + " "+"Error Message: "+ message +" - ";
                errorMessage = getErrorStatusCode(networkResponse, errorMessage, message);


            } catch (JSONException e) {
                errorMessage = getErrorStatusCode(networkResponse, errorMessage, "");
            }
        }
        i(Defines.TAG, errorMessage);
        error.printStackTrace();
        return errorMessage;
    }

    private static String getErrorStatusCode(NetworkResponse networkResponse, String errorMessage, String message) {
        try {
            if (networkResponse.statusCode == 404) {
                errorMessage += "Resource not found";
            } else if (networkResponse.statusCode == 401) {
                errorMessage += message + " انتهت الجلسة, الرجاء قم بأعادة تسجيل الدخول";
            } else if (networkResponse.statusCode == 400) {
                errorMessage += message + " Check your inputs";
            } else if (networkResponse.statusCode == 500) {
                errorMessage += message + " Something is getting wrong";
            }
        } catch (Exception e) {
            return "unknown error- couldnt get Error Status Code";
        }
        return errorMessage;
    }

}
