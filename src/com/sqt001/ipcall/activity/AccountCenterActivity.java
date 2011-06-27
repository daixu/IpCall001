package com.sqt001.ipcall.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.application.BuildConfig;
import com.sqt001.ipcall.contact.Account;
import com.sqt001.ipcall.login.SendSms;
import com.sqt001.ipcall.provider.BalanceTask;
import com.sqt001.ipcall.provider.BindTask;
import com.sqt001.ipcall.provider.DownloadTask;
import com.sqt001.ipcall.provider.GetSmsTask;
import com.sqt001.ipcall.provider.UpdateTask;
import com.sqt001.ipcall.util.AboutDlg;
import com.sqt001.ipcall.util.FileUtils;
import com.sqt001.ipcall.util.ResourceUtils;
import com.sqt001.ipcall.util.UserTask;

public class AccountCenterActivity extends Activity {

  private BalanceTask mBalanceTask = null;
  private TextView mId;
  private TextView mBindPhone;
  private Button mBalanceBtn;
  private Button mChargeBtn;
  private UpdateTask mUpdateTask = null;
  private DownloadTask mDownloadTask = null;
  private SendSms sendSms = new SendSms(this);
  boolean isBind = false;
  private BroadcastReceiver mReceiver;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.account_center_layout);
    initInfo();
    registerReceiver(mReceiver, new IntentFilter(BuildConfig.BROADCOSTRECIVER_XML_RESOVLE));
  }

  @Override
  protected void onResume() {
    super.onResume();
    refresh();
  }

  @Override
  protected void onDestroy() {
    unregisterReceiver(mReceiver);
    super.onDestroy();
  }

  public void refresh() {
    mId = (TextView) findViewById(R.id.id_field);
    String id = AppPreference.getUserId();
    mId.setText(id);
    if (AppPreference.getAccount().equals("") || AppPreference.getAccount().length() == 0) {
      mBindPhone.setText(Html.fromHtml("<a><u>未绑定号码</u></a>"));
    } else {
      if (AppPreference.getAccount().length() > 0 && !AppPreference.getAccount().equals("")) {
        String account = AppPreference.getAccount();
        mBindPhone.setText(account); 
      }
      isBind = true;
    }
  }

  private void initInfo() {
    ImageView imageView = (ImageView) findViewById(R.id.account_center);
    imageView.setImageResource(R.drawable.ttt);

    mId = (TextView) findViewById(R.id.id_field);
    mId.setText(AppPreference.getUserId());

    mBindPhone = (TextView) findViewById(R.id.phone_number_field);
    refresh();

    if (!isBind) {
      isBindNumber();
    }

    mBalanceBtn = (Button) findViewById(R.id.query_balance);
    mBalanceBtn.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (Account.activeAccountIfNeed(AccountCenterActivity.this)) {
          return;
        }
        balance();
      }

      private void balance() {
        startBalanceTask();
      }

      private void startBalanceTask() {
        mBalanceTask = new BalanceTask(AccountCenterActivity.this).execute(new BalanceTask.BalanceTaskListener() {
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
    });

    mChargeBtn = (Button) findViewById(R.id.recharge);
    mChargeBtn.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent intent = new Intent(AccountCenterActivity.this, ChargeActivity.class);
        startActivity(intent);
      }
    });
    mReceiver = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {
        refresh();
      }
    };
  }

  private void isBindNumber() {
    mBindPhone.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        startGetSmsTask();
      }

      private void startGetSmsTask() {
        new GetSmsTask(AccountCenterActivity.this).execute(new GetSmsTask.GetSmsTaskListener() {
          @Override
          public void onGetSmsTaskFinish(boolean success, String reason) {
            if (success) {
              String address = AppPreference.getGatewayNumber();
              sendSms.sendSms(address);
              bind();
            } else {
              if (!reason.equals(""))
                handleFailResult(reason);
            }
          }
        });
      }

      private void bind() {
        new BindTask(AccountCenterActivity.this).execute(new BindTask.BindTaskListener() {

          @Override
          public void onBindFinish(boolean success, String reason) {
            if (success) {
              handleSuccessResult();
            } else {
              bindFailResult();
            }
          }

          private void bindFailResult() {
            new AlertDialog.Builder(AccountCenterActivity.this).setTitle(R.string.wrong).setMessage(R.string.bind_fail)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                  }
                }).show();
          }

          void handleSuccessResult() {
            toResultActivity(true);
          }

          private void toResultActivity(boolean success) {
            new AlertDialog.Builder(AccountCenterActivity.this).setTitle(R.string.success)
                .setMessage(R.string.bind_success)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    onResume();
                  }
                }).show();
          }
        });
      }
    });
  }

  private void handleFailResult(final String reason) {
    new AlertDialog.Builder(AccountCenterActivity.this).setTitle(R.string.wrong).setMessage(reason)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        }).show();
  }

  private static final int DEBUG_MENU_LOG_ITEM_ID = -1;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.account_center_option, menu);
    super.onCreateOptionsMenu(menu);
    if (BuildConfig.isDebug()) {
      menu.add(0, DEBUG_MENU_LOG_ITEM_ID, Menu.NONE, "Log");
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.modify_password: {
      Intent intent = new Intent(this, ModifyAccountPwdActivity.class);
      startActivity(intent);
      return true;
    }

    case R.id.forget_password:
      Intent intent = new Intent(this, ForgetPwdActivity.class);
      startActivity(intent);
      return true;

    case R.id.change_account:
      Intent i = new Intent(this, AccountRegisterActivity.class);
      startActivity(i);
      finish();
      return true;

    case R.id.main_option_friend:
      Uri uri = Uri.parse("smsto:");
      Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
      String id = AppPreference.getUserId();
      smsIntent.putExtra("sms_body", "我在用省钱通软件打电话，超级省钱，推荐你用，wap.sqt001.com。我的ID：" + id + "^_^");
      startActivity(smsIntent);
      return true;

    case R.id.main_option_help:
      handleHelpOptionItemSelected();
      return true;

    case R.id.main_option_about:
      handleAboutOptionItemSelected();
      return true;

    case R.id.main_option_partener:
      handlePartenerOptionItemSelected();
      return true;

    case R.id.main_option_call:
      handleCallOptionItemSelected();
      return true;

    case R.id.main_option_update: {
      handleUpdateOptionItemSelected();
      return true;
    }

      // for test
    case DEBUG_MENU_LOG_ITEM_ID: {
      Intent logIntent = new Intent(this, LogActivity.class);
      startActivity(logIntent);
      return true;
    }
    }
    return super.onOptionsItemSelected(item);
  }

  private void handleHelpOptionItemSelected() {
    String title = getString(R.string.about_help);
    String message = new ResourceUtils(this).getStringFromRawResource(R.raw.help_txt);
    new AboutDlg(this).show(title, message);
  }

  private void handleAboutOptionItemSelected() {
    String title = getString(R.string.about_about);
    String message = AppPreference.getAbout();
    if (message.length() < 2) {
      message = new ResourceUtils(this).getStringFromRawResource(R.raw.about_txt);
    }
    new AboutDlg(this).show(title, message);
  }

  private void handlePartenerOptionItemSelected() {
    String title = getString(R.string.partener);
    String message = new ResourceUtils(this).getStringFromRawResource(R.raw.partener_txt);
    new AboutDlg(this).show(title, message);
  }

  private void handleCallOptionItemSelected() {
    String num = AppPreference.getServiceCallNum();
    if (num != null && num.length() >= 8) {
      Intent phoneIntent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + num));
      startActivity(phoneIntent);
    }
  }

  private void handleUpdateOptionItemSelected() {
    update();
  }

  private void update() {
    mUpdateTask = new UpdateTask(this).execute(new UpdateTask.UpdateTaskListener() {
      @Override
      public void onCancelGetUpdate() {
        if (mUpdateTask != null && mUpdateTask.getStatus() == UserTask.Status.RUNNING) {
          mUpdateTask.cancel(true);
          mUpdateTask = null;
        }
      }

      @Override
      public void onOkGetUpdate(String url) {
        download(url);
      }
    });
  }

  private void download(String url) {
    if (!FileUtils.isSDCardReady()) {
      new AlertDialog.Builder(this).setMessage(getString(R.string.sdcard_not_ready))
          .setPositiveButton(this.getString(R.string.ok), null).show();
      return;
    }

    // for test url:
    // url =
    // "http://220.112.40.202/down/Newding_Android_2.5.0_20100721_Newding.apk";

    mDownloadTask = new DownloadTask(this, url);
    mDownloadTask.execute();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      new AlertDialog.Builder(AccountCenterActivity.this).setTitle(R.string.exit)
          .setIcon(android.R.drawable.ic_menu_info_details).setMessage(R.string.really_exit)
          .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              finish();
              System.exit(0);
            }
          }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
          }).create().show();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}