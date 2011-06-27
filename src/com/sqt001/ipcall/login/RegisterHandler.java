package com.sqt001.ipcall.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sqt001.ipcall.activity.AccountActiveActivity;
import com.sqt001.ipcall.provider.RegisterTask;

public class RegisterHandler {
  private Activity mActivity;
  
  private RegisterHandler(Activity activity) {
    mActivity = activity;
  }

  public static RegisterHandler getInstance(Activity activity) {
    return new RegisterHandler(activity);
  }

  public void startRegisterTask(final Class<?> clazz) {
    new RegisterTask(mActivity).execute(new RegisterTask.RegisterTaskListener() {

      @Override
      public void onRegisterFinish(boolean success, String reason) {
        if (success) {
          handleSuccessResult(clazz);
        } else {
          handleFailResult(reason);
        }
      }
    });
  }

  private void handleSuccessResult(Class<?> clazz) {
    toResultActivity(clazz);
  }

  private void toResultActivity(Class<?> clazz) {
    activityJump(clazz, true);
  }

  private void activityJump(Class<?> clazz, boolean showPwd) {
    Intent intent = new Intent(mActivity, clazz);
    Bundle bundle = new Bundle();
    bundle.putBoolean("showPwd", showPwd);
    bundle.putBoolean("success", true);
    intent.putExtras(bundle);
    mActivity.startActivity(intent);
    mActivity.finish();
  }

  private void handleFailResult(final String reason) {
    Intent intent = new Intent(mActivity, AccountActiveActivity.class);
    mActivity.startActivity(intent);
    mActivity.finish();
  }
}
