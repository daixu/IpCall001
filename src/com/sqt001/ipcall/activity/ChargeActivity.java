package com.sqt001.ipcall.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.contact.Account;
import com.sqt001.ipcall.provider.BalanceTask;
import com.sqt001.ipcall.provider.ChargeTask;
import com.sqt001.ipcall.util.AboutDlg;
import com.sqt001.ipcall.util.ResourceUtils;
import com.sqt001.ipcall.util.UserTask;

/**
 * Do charge task;
 * get balance.
 */
public class ChargeActivity extends Activity {
    static final int SERIALMINLENGTH = 1;
    static final int PASSWORDMINLENGTH = 1;
    static final String OTHERCHARGEURL = "http://pay.sqt001.com/czwap/r.aspx?userid=";

    private BalanceTask mBalanceTask = null;
    private ChargeTask mChargeTask = null;
    private LinearLayout mLayout = null;
    private EditText mEditor = null;
    private String mNumber;
    

    public ChargeActivity() {
      String bindPhone = AppPreference.getAccount();
      String number = AppPreference.getMyNum();
      if (bindPhone.length()>0||(!bindPhone.equals(""))) {
        mNumber = bindPhone;
      }else { 
        if (number.length()>0||(!bindPhone.equals(""))) {
          mNumber = number;
        }
      }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charge_layout);     
        
        TextView textView = (TextView) findViewById(R.id.ChargeSeparator2);
//        textView.setText(Html.fromHtml("<a href='http://pay.sqt001.com:10010/czwap/r.aspx?userid='>其他充值方式</a>"));
        textView.setText(Html.fromHtml("<a href='http://pay.sqt001.com/czwap/r.aspx?userid='>其他充值方式</a>"));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        
        setupChargeButtonListener();
    }

    @Override
    public void onPause() {
        hideSoftKeyboardForNumberEdit();
        super.onPause();
    }

    private void hideSoftKeyboardForNumberEdit() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        EditText edit = (EditText)findViewById(R.id.InputSerialField);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
        edit = (EditText)findViewById(R.id.InputPwdField);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    private void setupChargeButtonListener() {
      Button ChargeButton = (Button)findViewById(R.id.DoChargeButton);
      ChargeButton.setOnClickListener(new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
          boolean message = checking();
          if(!message) {
            new AlertDialog.Builder(ChargeActivity.this).setTitle(R.string.exception)
            .setMessage(R.string.error_info).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
              }
            })
            .create()
            .show();
            return;
          }
          mLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.input_number, null);
          mEditor = (EditText)mLayout.findViewById(R.id.input_phone);
          mEditor.setText(mNumber);
          new AlertDialog.Builder(ChargeActivity.this)
          .setTitle(R.string.enter_mobile_phone_number).setMessage(R.string.recharge_prompt).setView(mLayout)
          .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                mNumber = mEditor.getText().toString();
                charge(mNumber);
              }
          })
          .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                mNumber = mEditor.getText().toString();
                charge(mNumber);
              }
          })
          .create()
          .show();
        }
      });
  }
    
    private boolean checking(){
      boolean isChecked = false;
      String serial = fetchSerialNumber();
      String pwd = fetchPwdNumber();
      if((validateSerialNumber(serial))&&(validatePwdNumber(pwd))) {
        isChecked = true;
      }
      return isChecked;
    }
    
    private void charge(String phoneNum) {
        if(Account.activeAccountIfNeed(this)) {
          return;
        }

        String serial = fetchSerialNumber();
        if(!validateSerialNumber(serial)) {
          return;
        }

        String pwd = fetchPwdNumber();
        if(!validatePwdNumber(pwd)) {
          return;
        }
        
        startChargeTask(serial, pwd, phoneNum);
    }

    private String fetchSerialNumber() {
        EditText edit = (EditText)findViewById(R.id.InputSerialField);
        return edit.getText().toString();
    }

    private boolean validateSerialNumber(String number) {
        if(number.length() < SERIALMINLENGTH) {
            return false;
        }
        return true;
    }

    private String fetchPwdNumber() {
        EditText edit = (EditText)findViewById(R.id.InputPwdField);
        return edit.getText().toString();
    }

    private boolean validatePwdNumber(String number) {
        if(number.length() < PASSWORDMINLENGTH) {
            return false;
        }
        return true;
    }
    
    private void startChargeTask(String serial, String pwd, final String phone) {
        mChargeTask = new ChargeTask(this).execute(serial, pwd, phone, new ChargeTask.CallTaskListener() {
            public void onInputMyNum() {
                charge(phone);
            }

            public void onCancelCharge() {
                cancelChargeTask();
            }
        });
    }

    private void cancelChargeTask() {
        if(mChargeTask != null && mChargeTask.getStatus() == UserTask.Status.RUNNING) {
            mChargeTask.cancel(true);
            mChargeTask = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.charge_option, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.charge_option_charge:
            handleChargeOptionItemClick();
            return true;

        case R.id.charge_option_balance:
            handleBalanceOptionItemClick();
            return true;       

        case R.id.charge_option_help:
            handleHelpOptionItemClick();
            return true;     

        case R.id.charge_option_other:
            handleOtherOptionItemClick();
            return true;     

        default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleChargeOptionItemClick() {
      boolean message = checking();
      if(!message) {
        new AlertDialog.Builder(ChargeActivity.this).setTitle(R.string.exception)
        .setMessage(R.string.error_info).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        })
        .create()
        .show();
      }
      mLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.input_number, null);
      mEditor = (EditText)mLayout.findViewById(R.id.input_phone);
      mEditor.setText(mNumber);
      new AlertDialog.Builder(ChargeActivity.this)
      .setTitle(R.string.enter_mobile_phone_number).setMessage(R.string.recharge_prompt).setView(mLayout)
      .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            mNumber = mEditor.getText().toString();
            charge(mNumber);
          }
      })
      .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            mNumber = mEditor.getText().toString();
            charge(mNumber);
          }
      })
      .create()
      .show();
    }

    private void handleBalanceOptionItemClick() {
        balance();
    }

    private void balance() {
        if(Account.activeAccountIfNeed(this)) {
            return;
        }

        startBalanceTask();
    }

    private void startBalanceTask() {
        mBalanceTask = new BalanceTask(this).execute(new BalanceTask.BalanceTaskListener () {
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

    private void handleHelpOptionItemClick() {
        help();
    }

    private void help() {
        String title = getString(R.string.money_save_secret);
        String message = AppPreference.getMoneySaveSecret();
        if(message == null || message.length() < 10) {
            message = new ResourceUtils(this).getStringFromRawResource(R.raw.secret_txt);
        }
        new AboutDlg(this).show(title, message);
    }

    private void handleOtherOptionItemClick() {
        if(Account.activeAccountIfNeed(this)) {
            return;
        }
        gotoWapCharge();
    }

    private void gotoWapCharge() {
        String url = OTHERCHARGEURL;
        String userId = AppPreference.getUserId();
        if(userId != null && userId.length() > 0) {
            url += userId;
        }

        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
