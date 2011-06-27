package com.sqt001.ipcall.login;

import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.application.BuildConfig;

public class CheckAccount {

  private CheckAccount() {
  }

  public static CheckAccount getInstance() {
    return new CheckAccount();
  }

  public boolean isAccountExist() {
    boolean isExists = false;
    boolean isRegistered = isAccountRegistered();
    if (isRegistered) {
      isExists = true;
    }
    return isExists;
  }

  /**
   * @return true if is registered, false else.
   */
  private boolean isAccountRegistered() {
    if (BuildConfig.isDebug()) {
      return false;
      // return AppPreference.getAccount().length() > 0 || AppPreference.getUserId().length() > 0;
    } else {
      return AppPreference.getAccount().length() > 0 || AppPreference.getUserId().length() > 0;
    }
  }
}
