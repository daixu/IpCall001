package com.sqt001.ipcall.login;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.gsm.SmsManager;
import android.util.Log;

import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.util.Base64;

public class SendSms {
  static final String SMS_BASE64_TABLE = "abcdVWXefgQRSTUYZhijKLMNOPuvwxyzGHIJklmnopDEFstABCqr2345601789!.";
  private static final int SLEEP = 10 * 1000;
  private Context mCtx;
  
  public SendSms(Context context) {
    mCtx = context;
  }

  public void sendSms(String number) {
    String content = "b_u="+AppPreference.getUserId();
    
    Log.i("content", content);
    String smsContent = Base64.encode(content, SMS_BASE64_TABLE);
    Log.i("smsContent", smsContent);
    SmsManager sms = SmsManager.getDefault();
    PendingIntent pendingIntent = PendingIntent.getBroadcast(mCtx, 0, new Intent(), 0);
    sms.sendTextMessage(number, null, smsContent, pendingIntent, null);
    try {
      Thread.sleep(SLEEP);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
