package com.sqt001.ipcall.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.provider.GetPasswordTask;
import com.sqt001.ipcall.provider.GetPasswordTask.GetPasswordListener;

public class ForgetPwdActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.forget_password_activity);
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.forget_password_title);

    Button submit = (Button) findViewById(R.id.submit_button);
    submit.setOnClickListener(new Button.OnClickListener() {

      @Override
      public void onClick(View v) {
        TextView name = (TextView) findViewById(R.id.account_number);

        String number = name.getText().toString();
        int count = number.length();
        System.out.println(count);
        if (number.length() >= 6 && number.length() <= 12) {
          getPwd(number);
        } else {
          Toast.makeText(ForgetPwdActivity.this, "帐号输入错误", Toast.LENGTH_LONG).show();
          return;
        }
      }

      private void getPwd(String number) {
        startGetPasswordTask(number);
      }

      private void startGetPasswordTask(final String number) {
        new GetPasswordTask(ForgetPwdActivity.this).execute(number, new GetPasswordListener() {

          @Override
          public void onGetPasswordFinish(boolean success, String reason) {
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
            new AlertDialog.Builder(ForgetPwdActivity.this).setTitle(R.string.new_pwd).setMessage("请求已收到,请稍后查看短信")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                  }
                }).show();
          }
        });
      }
    });

    Button back = (Button) findViewById(R.id.back_button);
    back.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
  }

  private void handleFailResult(final String reason) {
    new AlertDialog.Builder(ForgetPwdActivity.this).setTitle(R.string.wrong).setMessage(R.string.unbind_point)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        }).show();
  }
}
