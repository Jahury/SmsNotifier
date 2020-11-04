package com.apptech.smsdispatcher.rest;



/**
 * Created by Jahury on 4/23/2017.
 */

    public interface IApiProvider {
/*     interface OnApiRequestListner{
        void onFinish (String response);
        void onFailure (String error);
    }*/
    void execute(ApiRequest apiInfo, OnResponseListner listner);
}
