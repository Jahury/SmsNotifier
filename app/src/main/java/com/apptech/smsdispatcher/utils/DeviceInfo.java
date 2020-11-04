package com.apptech.smsdispatcher.utils;

import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.TimeUtils;

import java.util.Date;

public  class DeviceInfo {

    public static   String getSDKVersionName(){
        return  DeviceUtils.getSDKVersionName();
    }
    public static   int getSDKVersionCode(){
        return  DeviceUtils.getSDKVersionCode();
    }
    public static   String getAndroidID(){
        return  DeviceUtils.getAndroidID();
    }
    public static   String getMacAddress(){
        return  DeviceUtils.getMacAddress();
    }
    public static   String getManufacturer(){
        return  DeviceUtils.getManufacturer();
    }

    public static   String getModel(){
        return  DeviceUtils.getModel();
    }

    public static   String getUniqueDeviceId(){
        return  DeviceUtils.getUniqueDeviceId();
    }
    public static   boolean isSameDevice(){
        return  DeviceUtils.isSameDevice(getUniqueDeviceId());
    }


    public static   String getDeviceId(){
        return PhoneUtils.getDeviceId();
    }


    public static   String getSerial(){
        return PhoneUtils.getSerial();
    }


    public static   String getIMEI(){
        return PhoneUtils.getIMEI();
    }

    public static   long getMillis(){
        return TimeUtils.date2Millis(new Date());
    }


    public static   long getNowMills(){
        return TimeUtils.getNowMills();
    }

    public static   long getMillis2(){
        return TimeUtils.getNowMills();
    }
}
