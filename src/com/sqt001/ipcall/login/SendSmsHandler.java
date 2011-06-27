package com.sqt001.ipcall.login;

import android.app.Activity;
import android.content.Intent;

import com.sqt001.ipcall.activity.AccountActiveActivity;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.provider.GetSmsTask;

public class SendSmsHandler {

  private Activity mActivity;
  private SendSms mSendSms;

  private SendSmsHandler(Activity activity) {
    mActivity = activity;
    mSendSms = new SendSms(mActivity);
  }

  public static SendSmsHandler getInstance(Activity activity) {
    return new SendSmsHandler(activity);
  }

  public void startGetSmsTask() {
    new GetSmsTask(mActivity).execute(new GetSmsTask.GetSmsTaskListener() {
      @Override
      public void onGetSmsTaskFinish(boolean success, String reason) {
        if (success) {
          String address = AppPreference.getGatewayNumber();
          mSendSms.sendSms(address);
        } else {
          handleFail(reason);
        }
      }
    });
  }

  private void handleFail(String reason) {
    if (!reason.equals(""))
      handleFailResult(reason);
  }

  private void handleFailResult(final String reason) {
    Intent intent = new Intent(mActivity, AccountActiveActivity.class);
    mActivity.startActivity(intent);
    mActivity.finish();
  }
}
