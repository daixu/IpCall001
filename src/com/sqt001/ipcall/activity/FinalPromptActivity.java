package com.sqt001.ipcall.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.provider.ModifyTask;
import com.sqt001.ipcall.provider.ModifyTask.ModifyListener;

public class FinalPromptActivity extends Activity {

  private TextView mId;
  private TextView mIdVal;
  private TextView mAccount;
  private TextView mAccountVal;
  private TextView mUnBind;
  private TextView mBalance;
  private TextView mBalanceValue;
  private TextView mPassword;
  private TextView mPasswordVal;
  private TextView mModifyPassword;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.final_prompt_activity);
    initIconView();
    checkResult();

    Button next = (Button) findViewById(R.id.next_button);
    next.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(FinalPromptActivity.this, DialtactsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("success", true);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
      }
    });

    Button back = (Button) findViewById(R.id.back_button);
    back.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(FinalPromptActivity.this, DialtactsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("success", true);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
      }
    });
  }

  private void initIconView() {
    View appSnippet = findViewById(R.id.app_snippet);
    ((ImageView) appSnippet.findViewById(R.id.app_icon)).setBackgroundResource(R.drawable.icon);
  }

  private EditText mEditText;

  @Override
  protected void onResume() {
    super.onResume();
    checkResult();
  }

  public void checkResult() {
    Bundle bundle = this.getIntent().getExtras();
    boolean success = bundle.getBoolean("success");
    boolean showPwd = bundle.getBoolean("showPwd");
    if (success) {
      mId = (TextView) findViewById(R.id.user_id);
      mId.setText(R.string.your_id_is);

      mIdVal = (TextView) findViewById(R.id.user_id_value);

      mIdVal.setText(AppPreference.getUserId());

      mAccount = (TextView) findViewById(R.id.user_account);
      mAccount.setText(R.string.your_account);

      mAccountVal = (TextView) findViewById(R.id.user_account_value);

      mAccountVal.setText(AppPreference.getAccount());

      mUnBind = (TextView) findViewById(R.id.unbind_account);
      if (mAccountVal.getText().length() == 0) {
        mUnBind.setText(R.string.unbind_prompt);
      }

      mBalance = (TextView) findViewById(R.id.user_balance);
      mBalance.setText(R.string.account_balance);

      mBalanceValue = (TextView) findViewById(R.id.user_balance_value);

      float balanceVal = AppPreference.getUserBalance();
      balanceVal /= 100.0;

      mBalanceValue.setText(String.valueOf(balanceVal) + "元");
      if (showPwd) {
        mPassword = (TextView) findViewById(R.id.user_password);
        mPassword.setText(R.string.your_password);

        mPasswordVal = (TextView) findViewById(R.id.user_password_value);
        mPasswordVal.setText(AppPreference.getPassword());

        mModifyPassword = (TextView) findViewById(R.id.modify_password);
        mModifyPassword.setText(Html.fromHtml("<a><u>修改密码</u></a>"));

        mModifyPassword.setOnClickListener(new Button.OnClickListener() {

          @Override
          public void onClick(View v) {
            LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.modify_password_number, null);
            mEditText = (EditText) layout.findViewById(R.id.input_password);

            new AlertDialog.Builder(FinalPromptActivity.this).setTitle(R.string.input_new_password).setView(layout)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                    String password = mEditText.getText().toString();
                    String oldPwd = AppPreference.getPassword();
                    if (password.length() >= 6 && password.length() <= 12 && oldPwd.length() > 0) {
                      modify(oldPwd, password);
                    } else {
                      Toast.makeText(FinalPromptActivity.this, "密码长度错误", Toast.LENGTH_LONG).show();
                      return;
                    }
                  }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                  }
                }).create().show();
          }

          private void modify(String oldPwd, String newPwd) {
            startModifyTask(oldPwd, newPwd);
          }

          private void startModifyTask(final String oldPwd, final String newPwd) {
            new ModifyTask(FinalPromptActivity.this).execute(oldPwd, newPwd, new ModifyListener() {
              @Override
              public void onModifyPwdFinish(boolean success, String reason) {
                if (success) {
                  handleSuccessResult();
                } else {
                  handleFailResult(reason);
                }
              }

              private void handleSuccessResult() {
                toResultActivity(true);
              }

              private void toResultActivity(boolean success) {
                new AlertDialog.Builder(FinalPromptActivity.this).setTitle("成功").setMessage("密码修改成功")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                        String pwd = newPwd;
                        AppPreference.putPassword(pwd);
                        Intent intent = new Intent(FinalPromptActivity.this, DialtactsActivity.class);
                        intent.putExtra("success", true);
                        startActivity(intent);
                        finish();
                      }
                    }).show();
              }

              private void handleFailResult(final String reason) {
                new AlertDialog.Builder(FinalPromptActivity.this).setTitle(R.string.wrong).setMessage(reason)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                      }
                    }).show();
              }
            });
          }
        });
      }
    }
  }
}
