package com.sqt001.ipcall.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.sqt001.ipcall.application.AppPreference;

/**
 * This class is responsible for sending SMS.
 */
public class SmsSender {
    public static final String DELIVERED_ACTION = "SMS_DELIVERED_ACTION";
    public static final String SENT_ACTION = "SMS_SENT_ACTION";

    private Context ctx;

    public SmsSender(Context ctx){
        this.ctx = ctx;
    }

    @SuppressWarnings("deprecation")
    public static void sendSMS(String to, String smsContent) {
        if(AppPreference.isEclairOrLater()) {
            android.telephony.SmsManager.getDefault().
            sendTextMessage(to, null, smsContent, null, null);
        } else {
            android.telephony.gsm.SmsManager.getDefault().
            sendTextMessage(to, null, smsContent, null, null);
        }
    }

    /**
     *
     * @param phoneNumber The phone number to send to.
     * @param action The ODK action, e.g. 'reg'.
     * @param parameters A map of parameters for the SMS.
     * @param onSent A BroadcastReceiver that will be called when the message is sent.
     * @param onDelivered A BroadcastReceiver that will be called when the message is delivered.
     */
    public void sendSMS(String phoneNumber, String action, Map<String,String> parameters, BroadcastReceiver onSent, BroadcastReceiver onDelivered){
        sendSMS(phoneNumber, createSmsWithParameters(action, parameters), onSent, onDelivered);
    }

    /**
     *
     * @param phoneNumber The phone number to send to.
     * @param content The message content.
     * @param onSent A BroadcastReceiver that will be called when the message is sent.
     * @param onDelivered A BroadcastReceiver that will be called when the message is delivered.
     */
    @SuppressWarnings("deprecation")
    public void sendSMS(String phoneNumber, String content, BroadcastReceiver onSent, BroadcastReceiver onDelivered) {
        PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0,
                new Intent(SENT_ACTION), 0);
        ctx.registerReceiver(onSent, new IntentFilter(SENT_ACTION));

        PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0,
                new Intent(DELIVERED_ACTION), 0);
        ctx.registerReceiver(onDelivered, new IntentFilter(DELIVERED_ACTION));

        if(AppPreference.isEclairOrLater()) {
            android.telephony.SmsManager.getDefault().
            sendTextMessage(phoneNumber, null, content, sentPI, deliveredPI);
        } else {
            android.telephony.gsm.SmsManager.getDefault().
            sendTextMessage(phoneNumber, null, content, sentPI, deliveredPI);
        }

        Log.i("SmsSender", "Sms sent: Recipient: " + phoneNumber + "; Message: " + content);
    }

    /**
     *
     * @param action The ODK action, e.g. 'reg'.
     * @param parameters A map of parameters for the SMS.
     * @return The SMS content, e.g. 'reg imei=12345&userid=johndoe'
     */
    private String createSmsWithParameters(String action, Map<String,String> parameters){
        List<String> props = new ArrayList<String>();
        for (String prop : parameters.keySet()){
            if (prop != null && parameters.get(prop) != null){
                props.add(prop + "=" + parameters.get(prop));
            }
        }
        return action + " " + join(props, "&");
    }

    private String join(List<String> s, String delimiter) {
        if (s.isEmpty()) return "";
        Iterator<String> iter = s.iterator();
        StringBuffer buffer = new StringBuffer(iter.next());
        while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
        return buffer.toString();
    }

}