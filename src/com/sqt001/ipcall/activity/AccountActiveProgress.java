package com.sqt001.ipcall.activity;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.provider.UnImsiRegisterTask;
import com.sqt001.ipcall.util.Base64;
import com.sqt001.ipcall.util.SmsSender;
import com.sqt001.ipcall.util.UserTask;

/**
 * 
 */
public class AccountActiveProgress extends Activity implements OnClickListener {
  private final String TAG="AccountActiveProgress";

  public static final String SMS_BASE64_TABLE = "abcdVWXefgQRSTUYZhijKLMNOPuvwxyzGHIJklmnopDEFstABCqr2345601789!.";
  private static final String SMS_DEST_NUMBER = "075588321332";

  private TextView mStatusTextView;
  private Button mOkButton;
  private ProgressBar mProgressBar;
  private View mOkPanel;
  private UnImsiRegisterTask mBalanceTask;

  private volatile int mResultCode = -1;

  private static final int WAITSMSDURATION = 30*1000;
  private static final int TIME_TO_EXIT_PROGRESS = 1;
  private static final int SMS_SENT_SUC = 2;
  private static final int SMS_SENT_FAIL = 3;
  private static final int SMS_DELIVER_SUC = 4;
  private static final int SMS_DELIVER_FAIL = 5;

  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case TIME_TO_EXIT_PROGRESS:
        handleTimeToStop();
        break;

      case SMS_SENT_SUC:
        handleSmsSendSuc();
        break;

      case SMS_SENT_FAIL:
        handleSmsSendFail();
        break;

      case SMS_DELIVER_SUC:
        handleSmsDeliverSuc();
        break;

      case SMS_DELIVER_FAIL:
        handleSmsDeliverFail();
        break;

      default:
        super.handleMessage(msg);
        break;
      }
    }
  };

  /**
   * should not call this then screen orientation change.
   */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    initView();
    sendSms();
    waitSomeTime();
  }

  /**
   * Init views
   */
  public void initView() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.account_active_progress);

    initIconView();

    setStatusText(R.string.activating_start);

    mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
    mProgressBar.setIndeterminate(true);
    
    // Hide button till progress is being displayed
    mOkPanel = findViewById(R.id.ok_panel);
    mOkButton = (Button)findViewById(R.id.ok_button);
    mOkButton.setOnClickListener(this);
    mOkPanel.setVisibility(View.INVISIBLE);
  }
  
  private void initIconView() {
    View appSnippet = findViewById(R.id.app_snippet);
    ((ImageView)appSnippet.findViewById(R.id.app_icon)).setBackgroundResource(R.drawable.icon);
  }

  private void setStatusText(int resId) {
    mStatusTextView = (TextView)findViewById(R.id.center_text);
    mStatusTextView.setText(resId);
  }

  private void stopProgressBar() {
    mProgressBar.setVisibility(View.INVISIBLE);
    mOkPanel.setVisibility(View.VISIBLE);
  }


  /**
   * Listen the sending of sms.
   */
  SmsSendListener mSmsListener = null;

  private void sendSms() {
    try {
      if(mSmsListener == null) {
        mSmsListener = new SmsSendListener();
      }
      trySendSms(mSmsListener);
    } catch (IOException e) {
      setResultAndFinish(-1);
    }
  }

  private SmsSender mSmsSender = null;

  private void trySendSms(SmsSendListener listener) throws IOException {
    String guid = AppPreference.generateGuidAsAccount();
    /*
    String content = "s_"
      + "g=" + guid
      + "&"
      + "c=" + AppPreference.getChannel()
      + "&"
      + "v=" + AppPreference.getVersion();
    */
    String content = "g_"
      + guid
      + "&"
      + AppPreference.getChannel()
      + "&"
      + "cc";
    
    String smsContent = Base64.encode(content, SMS_BASE64_TABLE);
    if(mSmsSender == null) {
      mSmsSender = new SmsSender(this);
    }
    mSmsSender.sendSMS(SMS_DEST_NUMBER, smsContent, listener, listener);
  }

  private class SmsSendListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      int resultCode = getResultCode();
      String actionName = intent.getAction();
      if(actionName.equals(SmsSender.SENT_ACTION)) {
        handleSmsSend(resultCode);
      }
      else if(actionName.equals(SmsSender.DELIVERED_ACTION)){
        handleSmsDeliver(resultCode);
      }
    }

    private void handleSmsSend(int resultCode) {
      Message message = new Message();
      switch(resultCode) {
      case Activity.RESULT_OK:
        message.what = SMS_SENT_SUC;
        break;

      default:
        message.what = SMS_SENT_FAIL;
        break;
      }
      mHandler.sendMessage(message);
    }

    private void handleSmsDeliver(int resultCode) {
      Message message = new Message();
      switch (resultCode) {
      case Activity.RESULT_OK:
        message.what = SMS_DELIVER_SUC;
        break;

      default:
        message.what = SMS_DELIVER_FAIL;
        break;
      }
      mHandler.sendMessage(message);
    }
  } //class SmsSendListener

  private void handleSmsSendSuc() {
//    LogUtil.toast(this, "Sms Send Suc");
    setStatusText(R.string.activating_sms_sent);
  }

  private void handleSmsSendFail() {
//    LogUtil.toast(this, "Sms Send Fail");
    stopProgressBar();
    setStatusText(R.string.sms_active_fail);
  }

  private void handleSmsDeliverSuc() {
//    LogUtil.toast(this, "Sms deliver Suc");
    setStatusText(R.string.activating_sms_deliver);
  }

  private void handleSmsDeliverFail() {
//    LogUtil.toast(this, "Sms deliver fail");
    stopProgressBar();
    setStatusText(R.string.sms_active_fail);
  }

  /**
   * Wait at least 30s
   */
  private void waitSomeTime() {
    createTimerTask(TIME_TO_EXIT_PROGRESS, WAITSMSDURATION);
  }

  private Timer mTimer;

  private void createTimerTask(final int what, final int duration) {
    if(mTimer != null) {
      mTimer.cancel();
    }
    mTimer = new Timer();

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

  private void handleTimeToStop() {
    stopProgressBar();
    setStatusText(R.string.activating_finish);
  }
  
  private void balance() {
    startBalanceTask();
  }

  private void startBalanceTask() {
    mBalanceTask = new UnImsiRegisterTask(this).execute(new UnImsiRegisterTask.BalanceTaskListener() {
      public void onCancelGetBalance() {
        if (mBalanceTask != null && mBalanceTask.getStatus() == UserTask.Status.RUNNING) {
          mBalanceTask.cancel(true);
          mBalanceTask = null;
        }
      }

      public void afterInputMyNum() {
        balance();
      }
    });
  }

  @Override
  public void onClick(View v) {
    if(v == mOkButton) {
      AppPreference.putUserId("");
      AppPreference.putAccount("");
      AppPreference.putMyNum("");
      balance();
      //setResultAndFinish(0);
    }
  }

  void setResultAndFinish(int retCode) {
    setResult(retCode);
    finish();
  }

  /**
   *  Ignore back key
   */
  @Override
  public boolean dispatchKeyEvent(KeyEvent ev) {
    if (ev.getKeyCode() == KeyEvent.KEYCODE_BACK) {
      if (mResultCode == -1) {
        // Ignore back key when installation is in progress
        return true;
      } else {
        // If installation is done, just set the result code
        setResult(mResultCode);
      }
    }
    return super.dispatchKeyEvent(ev);
  }
}
