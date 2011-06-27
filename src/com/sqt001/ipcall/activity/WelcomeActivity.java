package com.sqt001.ipcall.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.contact.Account;
import com.sqt001.ipcall.login.CheckAccount;
import com.sqt001.ipcall.login.RegisterHandler;
import com.sqt001.ipcall.login.SendSmsHandler;
import com.sqt001.ipcall.provider.QueryAccountTask;
import com.sqt001.ipcall.provider.QueryAccountTask.QueryAccountTaskListener;
import com.sqt001.ipcall.util.LogUtil;

/**
 * 欢迎界面
 */
public class WelcomeActivity extends Activity {
  private final static int WELCOME_VIEW = 0;
  private final static int LOGON_RUN = 1;
  public final static int EXIT = 2;

  private Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case WELCOME_VIEW:
        break;

      case LOGON_RUN:
        checkAccount();
        break;
      }
    }
  };

  Timer mTimer = null;

  public void SendMessage(final int what, int duration) {
    if (mTimer == null) {
      mTimer = new Timer();
    }

    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        Message message = new Message();
        message.what = what;
        mHandler.sendMessage(message);
      }
    };
    mTimer.schedule(task, duration);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.welcome_activity);
    SendMessage(LOGON_RUN, 2000);
  }

  private void checkAccount() {
    boolean isAccount = CheckAccount.getInstance().isAccountExist();
    LogUtil.w("isAccount: " + isAccount);
    if (isAccount) {
      startupJump(DialtactsActivity.class, true);
    } else {
      boolean x = Account.isActive();
      if (x) {
        // exist imsi
        getBalance();
      } else {
        // no imsi.
        startupJump(DialtactsActivity.class, true);
        // Account.activeAccountForResult(this, 1);
      }
    }
  }

  private void startupJump(final Class<?> clazz, final boolean isLogined) {
    Intent intent = new Intent(WelcomeActivity.this, clazz);
    intent.putExtra("isLogined", isLogined);
    WelcomeActivity.this.startActivity(intent);
    WelcomeActivity.this.finish();
  }

  private void getBalance() {
    new QueryAccountTask(this).execute(new QueryAccountTaskListener() {
      @Override
      public void onQueryAccountFinish(String isnewuser, String reason) {
        if (isnewuser.equals("0")) {
          newAccountHandler();
        } else if (isnewuser.equals("1")) {
          oldAccountHandler();
        } else {
          if (!reason.equals(""))
            handleFailResult(reason);
        }
      }
    });
  }

  private void oldAccountHandler() {
    oldAccountAutoSendSms();
    oldAccountHandSendSms();
    oldAccountNotSendSms();
  }

  private void oldAccountNotSendSms() {
    if (AppPreference.getControl() == 2) {
      login(FinalPromptActivity.class, true);
    }
  }

  private void oldAccountHandSendSms() {
    if (AppPreference.getControl() == 1) {
      login(ManualBindMainActivity.class, false);
    }
  }

  private void oldAccountAutoSendSms() {
    if (AppPreference.getControl() == 0) {
      SendSmsHandler.getInstance(WelcomeActivity.this).startGetSmsTask();
      login(FinalPromptActivity.class, true);
    }
  }

  private void newAccountHandler() {
    newAccountAutoSendSms();
    newAccountHandSendSms();
    newAccountNotSendSms();
  }

  private void newAccountNotSendSms() {
    if (AppPreference.getControl() == 2) {
      register(FinalPromptActivity.class);
    }
  }

  private void newAccountHandSendSms() {
    if (AppPreference.getControl() == 1) {
      register(ManualBindMainActivity.class);
    }
  }

  private void newAccountAutoSendSms() {
    if (AppPreference.getControl() == 0) {
      SendSmsHandler.getInstance(WelcomeActivity.this).startGetSmsTask();
      register(FinalPromptActivity.class);
    }
  }

  private void register(Class<?> clazz) {
    RegisterHandler.getInstance(WelcomeActivity.this).startRegisterTask(clazz);
  }

  private void login(Class<?> clazz, boolean showPwd) {
    Intent intent = new Intent(WelcomeActivity.this, clazz);
    Bundle bundle = new Bundle();
    bundle.putBoolean("showPwd", showPwd);
    bundle.putBoolean("success", true);
    intent.putExtras(bundle);
    startActivity(intent);
    finish();
  }

  private void handleFailResult(final String reason) {
    Intent intent = new Intent(WelcomeActivity.this, AccountActiveActivity.class);
    startActivity(intent);
    finish();
  }
}
