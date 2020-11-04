package com.apptech.smsdispatcher.rest;

/**
 * Created by Jahury on 3/13/2018.
 */

public    interface OnResponseListner{
    void onSuccess(String response);
    void onFailure(String error);
}