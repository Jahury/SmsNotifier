package com.apptech.smsdispatcher.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.ConnectivityManager;

public class Utils {

    public static void copyToClipboard(Context context, String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager)context. getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Francy_Copy", text);
            clipboard.setPrimaryClip(clip);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            return cm.getActiveNetworkInfo() != null &&
                    cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
