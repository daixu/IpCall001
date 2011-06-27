package com.sqt001.ipcall.contact;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.activity.AccountActiveActivity;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.application.BuildConfig;
import com.sqt001.ipcall.provider.Constants;

public class Account {
  public static boolean isImsiActive() {
    String imsi = AppPreference.getImsi();
    if(imsi == null) {
      return false;
    }

    if(!BuildConfig.isTestingHtc200() && imsi.equals(Constants.Other.HTC200_IMSI)) {
      return false;
    }

    return imsi.length() == AppPreference.IMSI_MININUM_LENGTH || imsi.length() == AppPreference.IMSI_MAXMIUM_LENGTH;
  }

  public static boolean isGuidActive() {
    String imsi = AppPreference.getImsi();
    if(imsi == null) {
      return false;
    }
    return imsi.length() ==  AppPreference.IMSI_MAXMIUM_LENGTH;
  }

  public static boolean isActive() {
    if(isImsiActive() || isGuidActive()) {
      return true;
    }
    return false;
  }

  public static boolean activeAccountIfNeed(final Context context) {
    if(!isActive()) {
      queryActiveAccount(context);
      return true;
    }
    return false;
  }

  public static void queryReActiveAccount(final Context context) {
    new AlertDialog.Builder(context)
    .setMessage(context.getString(R.string.reset_caller_hint))
    .setPositiveButton(context.getString(R.string.ok), new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        activeAccount(context);
      }
    })
    .show();
  }

  public static void queryActiveAccount(final Context context) {
    new AlertDialog.Builder(context)
    .setTitle(context.getString(R.string.active_count))
    .setMessage(context.getString(R.string.active_count_msg))
    .setPositiveButton(context.getString(R.string.ok), new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        activeAccount(context);
      }
    })
    .show();
  }

  public static void activeAccount(final Context context) {
    Intent intent=new Intent(context, AccountActiveActivity.class);
    context.startActivity(intent);
  }
  
  public static void activeAccountForResult(final Activity activity, int requestCode) {
    Intent intent=new Intent(activity, AccountActiveActivity.class);
    activity.startActivityForResult(intent, requestCode);
  }
}
