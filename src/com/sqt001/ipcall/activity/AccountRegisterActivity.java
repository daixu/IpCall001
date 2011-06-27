package com.sqt001.ipcall.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.contact.Account;
import com.sqt001.ipcall.login.SendSms;
import com.sqt001.ipcall.provider.GetSmsTask;
import com.sqt001.ipcall.provider.LoginTask;
import com.sqt001.ipcall.provider.QueryAccountTask;
import com.sqt001.ipcall.provider.QueryAccountTask.QueryAccountTaskListener;
import com.sqt001.ipcall.provider.RegisterTask;

public class AccountRegisterActivity extends Activity {

  public static final int EXIT = 0;
  private SendSms sendSms = new SendSms(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.account_register_activity);

    initIconView();
    initRegister();
  }

  private void initIconView() {
    View appSnippet = findViewById(R.id.app_snippet);
    ((ImageView) appSnippet.findViewById(R.id.app_icon)).setBackgroundResource(R.drawable.icon);
  }

  private void initRegister() {
    TextView forgetPassword = (TextView) findViewById(R.id.textView);
    forgetPassword.setText(Html.fromHtml("<a><u>找回密码</u></a>"));
    forgetPassword.setMovementMethod(LinkMovementMethod.getInstance());
    forgetPassword.setOnClickListener(new Button.OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent intent = new Intent(AccountRegisterActivity.this, ForgetPwdActivity.class);
        startActivity(intent);
      }
    });

    TextView register = (TextView) findViewById(R.id.register_account);
    register.setText(Html.fromHtml("没有帐号,<a><u>点击注册</u></a>"));
    register.setMovementMethod(LinkMovementMethod.getInstance());

    register.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (Account.isActive()) {
          getBalance();
        } else {
          cleanAccountInfo();
          AppPreference.putImsi("");
          Intent intent = new Intent(AccountRegisterActivity.this, DialtactsActivity.class);
          intent.putExtra("isLogined", true);
          startActivity(intent);
          finish();
        }
      }

    });

    Button login = (Button) findViewById(R.id.login_button);
    login.setOnClickListener(new Button.OnClickListener() {

      @Override
      public void onClick(View v) {
        TextView name = (TextView) findViewById(R.id.account_number);
        TextView pwd = (TextView) findViewById(R.id.password);

        String accountName = name.getText().toString();
        String password = pwd.getText().toString();
        loginTask(accountName, password);
      }

      private void loginTask(String accountName, String password) {
        if (checkAccount(accountName, password)) {
          startLoginTask(accountName, password);
        }
      }
    });

    Button back = (Button) findViewById(R.id.back_button);
    back.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(AccountRegisterActivity.this, DialtactsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("success", true);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
      }
    });
  }

  private void cleanAccountInfo() {
    AppPreference.putUserId("");
    AppPreference.putAccount("");
    AppPreference.putMyNum("");
  }

  private void handleFailResult(final String reason) {
    new AlertDialog.Builder(AccountRegisterActivity.this).setTitle(R.string.wrong).setMessage(reason)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        }).show();
  }

  private boolean checkAccount(String accountName, String password) {
    boolean isSuccess = true;
    if (accountName.length() < 6 || accountName.length() > 12 || password.length() < 6 || password.length() > 12) {
      Toast.makeText(this, "帐号或密码长度错误", Toast.LENGTH_LONG).show();
      isSuccess = false;
    }
    return isSuccess;
  }

  private void startLoginTask(String account, String password) {
    new LoginTask(AccountRegisterActivity.this).execute(account, password, new LoginTask.LoginTaskListener() {

      @Override
      public void onLoginFinish(boolean success, String reason) {
        if (success) {
          handleSuccessResult();
          AppPreference.putMyNum("");
        } else {
          handleFailResult(reason);
        }
      }
    });
  }

  private void handleSuccessResult() {
    toResultActivity(DialtactsActivity.class, true);
  }

  private void getBalance() {
    cleanAccountInfo();
    new QueryAccountTask(AccountRegisterActivity.this).execute(new QueryAccountTaskListener() {
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

      private void oldAccountHandler() {
        newAccountAutoSendSms();
        newAccountHandSendSms();
        newAccountNotSendSms();
      }

      private void newAccountNotSendSms() {
        if (AppPreference.getControl() == 2) {
          toResultActivity(FinalPromptActivity.class, false);
        }
      }

      private void newAccountHandSendSms() {
        if (AppPreference.getControl() == 1) {
          toResultActivity(ManualBindMainActivity.class, false);
        }
      }

      private void newAccountAutoSendSms() {
        if (AppPreference.getControl() == 0) {
          startGetSmsTask();
          toResultActivity(FinalPromptActivity.class, false);
        }
      }

      private void newAccountHandler() {
        oldAccountAutoSendSms();
        oldAccountHandSendSms();
        oldAccountNotSendSms();
      }

      private void oldAccountNotSendSms() {
        if (AppPreference.getControl() == 2) {
          register(FinalPromptActivity.class);
        }
      }

      private void oldAccountHandSendSms() {
        if (AppPreference.getControl() == 1) {
          register(ManualBindMainActivity.class);
        }
      }

      private void oldAccountAutoSendSms() {
        if (AppPreference.getControl() == 0) {
          startGetSmsTask();
          register(FinalPromptActivity.class);
        }
      }

      private void register(Class<?> clazz) {
        startRegisterTask(clazz);
      }

      private void startRegisterTask(final Class<?> clazz) {
        new RegisterTask(AccountRegisterActivity.this).execute(new RegisterTask.RegisterTaskListener() {

          @Override
          public void onRegisterFinish(boolean success, String reason) {
            if (success) {
              handleSuccessResult(clazz);
            } else {
              handleFailResult(reason);
            }
          }

          private void handleSuccessResult(Class<?> clazz) {
            toResultActivity(clazz, true);
          }
        });
      }
    });
  }

  private void toResultActivity(Class<?> clazz, boolean success) {
    Intent intent = new Intent(AccountRegisterActivity.this, clazz);
    Bundle bundle = new Bundle();
    bundle.putBoolean("showPwd", true);
    bundle.putBoolean("success", success);
    intent.putExtras(bundle);
    startActivity(intent);
    finish();
  }

  private void startGetSmsTask() {
    new GetSmsTask(AccountRegisterActivity.this).execute(new GetSmsTask.GetSmsTaskListener() {
      @Override
      public void onGetSmsTaskFinish(boolean success, String reason) {
        if (success) {
          String address = AppPreference.getGatewayNumber();
          sendSms.sendSms(address);
        } else {
          if (!reason.equals(""))
            handleFailResult(reason);
        }
      }
    });
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      Intent intent = new Intent(AccountRegisterActivity.this, DialtactsActivity.class);
      intent.putExtra("success", true);
      startActivity(intent);
      finish();
    }
    return super.onKeyDown(keyCode, event);
  }
}