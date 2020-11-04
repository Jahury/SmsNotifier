package com.apptech.smsdispatcher.entities;

public class SmsMessage {
    public SmsMessage(String message, String phoneNumber) {
        Message = message;
        PhoneNumber = phoneNumber;
    }

    public SmsMessage() {
    }

    public long ID;
    public String Message;
    public String PhoneNumber;
}
