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
import com.sqt001.ipcall.provider.ModifyTask;
import com.sqt001.ipcall.provider.ModifyTask.ModifyListener;

public class ModifyAccountPwdActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.update_password);
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.modify_password_title);
    
    Button submit = (Button) findViewById(R.id.submit_button);
    submit.setOnClickListener(new Button.OnClickListener() {

      @Override
      public void onClick(View v) {
        TextView oldPwd = (TextView) findViewById(R.id.old_password);
        TextView newPwd = (TextView) findViewById(R.id.new_password);

        String oldPassword = oldPwd.getText().toString();
        String newPassword = newPwd.getText().toString();
        if (newPassword.length() >= 6 && newPassword.length() <= 12 && oldPassword.length() >= 6
            && oldPassword.length() <= 12) {
          modify(oldPassword, newPassword);
        } else {
          Toast.makeText(ModifyAccountPwdActivity.this, "密码长度错误", Toast.LENGTH_LONG).show();
          return;
        }
      }

      private void modify(String oldPwd, String newPwd) {
        startModifyTask(oldPwd, newPwd);
      }

      private void startModifyTask(final String oldPwd, final String newPwd) {
        new ModifyTask(ModifyAccountPwdActivity.this).execute(oldPwd, newPwd, new ModifyListener() {
          @Override
          public void onModifyPwdFinish(boolean success, String reason) {
            if (success) {
              handleSuccessResult(newPwd);
            } else {
              handleFailResult(reason);
            }
          }

          private void handleSuccessResult(String newPwd) {
            toResultActivity(true, newPwd);
          }

          private void toResultActivity(boolean success, String newPwd) {
            new AlertDialog.Builder(ModifyAccountPwdActivity.this).setTitle("成功").setMessage("密码修改成功")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    finish();
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
    new AlertDialog.Builder(ModifyAccountPwdActivity.this).setTitle(R.string.wrong).setMessage(reason)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        }).show();
  }
}