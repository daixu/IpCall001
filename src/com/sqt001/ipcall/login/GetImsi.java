package com.sqt001.ipcall.login;

import android.content.Context;
import android.telephony.TelephonyManager;

public class GetImsi {
  
  private Context mContext;
  public GetImsi(Context context) {
    this.mContext = context;
  }
  
  public boolean getImsi(){
    boolean isImsi = false;
    TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    String imsi =tm.getSubscriberId();
    if (!imsi.equals("")) {
      isImsi = true;
    }
    return isImsi;
  }
}
