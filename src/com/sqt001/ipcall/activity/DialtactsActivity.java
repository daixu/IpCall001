package com.sqt001.ipcall.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

import com.sqt001.ipcall.R;

/**
 * The dialer activity that has one tab with the virtual 12key dialer, a tab
 * with recent calls in it, a tab with the contacts and a tab with the favorite.
 * This is the container and the tabs are embedded using intents. The dialer
 * tab's title is 'phone', a more common name (see strings.xml).
 */
public class DialtactsActivity extends TabActivity implements TabHost.OnTabChangeListener {
  private TabHost mTabHost;

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.dialer_activity);

    mTabHost = getTabHost();
    mTabHost.setOnTabChangedListener(this);

    // Setup the tabs
    setupDialerTab();
    setupAccountCenterTab();
    setupRecommendFriendsTab();
  }

  /*
   * 拨号界面
   */
  private void setupDialerTab() {
    Bundle bundle = getIntent().getExtras();
    String number = bundle.getString("number");
    Intent intent = new Intent();// "com.android.phone.action.TOUCH_DIALER");
    intent.putExtra("number", number);
    intent.setClass(this, TwelveKeyDialer.class);

    mTabHost.addTab(mTabHost.newTabSpec("dialer")
        .setIndicator(getString(R.string.dialerIconLabel), getResources().getDrawable(R.drawable.ic_tab_dialer))
        .setContent(intent));
  }

  /*
   * 用户中心
   */
  private void setupAccountCenterTab() {
    Intent intent = new Intent();// UI.LIST_STREQUENT_ACTION);
    intent.setClass(this, AccountCenterActivity.class);

    mTabHost.addTab(mTabHost.newTabSpec("account_center")
        .setIndicator(getString(R.string.account_center), getResources().getDrawable(R.drawable.ic_tab_account_center))
        .setContent(intent));
  }

  /*
   * 推荐好友
   */
  private void setupRecommendFriendsTab() {
    Intent intent = new Intent();// UI.LIST_STREQUENT_ACTION);
    intent.setClass(this, RecommendColumnActivity.class);

    mTabHost.addTab(mTabHost.newTabSpec("recommend_column")
        .setIndicator(getString(R.string.recommend_column), getResources().getDrawable(R.drawable.ic_tab_recommend))
        .setContent(intent));
  }

  public void onTabChanged(String arg0) {
  }

  /*
   * 判断手机卡的联网状态
   */
  /*private void getNetworkStatus() {
    TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    // 蓝牙服务
    ContentResolver cr = this.getContentResolver();
    String tmps = android.provider.Settings.System.getString(cr, android.provider.Settings.System.BLUETOOTH_ON);
    if (tmps.equals("1")) {
      // 已经打开的操作
      new AlertDialog.Builder(DialtactsActivity.this).setMessage(R.string.bluetooth_serve_opened)
          .setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
          }).create().show();
    } else {
      // 未打开的操作
      new AlertDialog.Builder(DialtactsActivity.this).setMessage(R.string.bluetooth_serve_closed)
          .setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
          }).create().show();
    }

    // 飞行模式是否打开
    tmps = android.provider.Settings.System.getString(cr, android.provider.Settings.System.AIRPLANE_MODE_ON);
    if (tmps.equals("1")) {
      // 已经打开的操作
      new AlertDialog.Builder(DialtactsActivity.this).setMessage(R.string.flight_mode_opened)
          .setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
          }).create().show();
    } else {
      // 未打开的操作
      new AlertDialog.Builder(DialtactsActivity.this).setMessage(R.string.flight_mode_closed)
          .setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
          }).create().show();
    }
  }*/
}