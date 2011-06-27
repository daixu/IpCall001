package com.sqt001.ipcall.login;

import android.content.Context;

public class AccountManager {
  public final static String XML_NAME= "ACCOUNT_XML";
  public final static String KEY_ACCOUNT = "KEY_account";
  public final static String KEY_ACCOUNT_DEFAULT = "";
  
  private Context mContext;
  public AccountManager(Context context) {
    this.mContext = context;
  }
  
  /** 
   * 保存帐号信息
   */
  public void saveAccount(String account){
    if(account != null)
      return;
    
    mContext.getSharedPreferences(XML_NAME, 0)
          .edit()
          .putString(KEY_ACCOUNT, account)
          .commit();
  }
  
  /**
   * 读取帐号信息
   * @return String
   */
  public  String readAccount(){
    String account = mContext.getSharedPreferences(XML_NAME, 0).getString(KEY_ACCOUNT, KEY_ACCOUNT_DEFAULT);
    return account;
  }
}
