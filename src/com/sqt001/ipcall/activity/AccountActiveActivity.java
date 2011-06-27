package com.sqt001.ipcall.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.sqt001.ipcall.R;

public class AccountActiveActivity extends Activity {
  private static final String TAG = "AccountActiveActivity";

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    initWindow();
    initViews();
  }

  private void initWindow() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.account_active_confirm);
  }

  private void initViews() {
    initIconView();
    initButtonView();
  }

  private void initIconView() {
    View appSnippet = findViewById(R.id.app_snippet);
    ((ImageView) appSnippet.findViewById(R.id.app_icon)).setBackgroundResource(R.drawable.icon);
  }

  private void initButtonView() {
    Button cancel = (Button) findViewById(R.id.cancel_button);
    cancel.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        System.exit(0);
//        finish();
      }
    });

    Button ok = (Button) findViewById(R.id.ok_button);
    ok.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActiveProgress();
      }
    });
  }
  
  private void startActiveProgress() {
    Intent intent = new Intent(this, AccountActiveProgress.class);
    startActivity(intent);
    finish();
  }
}
