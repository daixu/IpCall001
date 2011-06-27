package com.sqt001.ipcall.provider;

import android.app.ProgressDialog;
import android.content.Context;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.util.UserTask;

public class LoginTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener {
  private LoginRequest mLoginRequest;
  private LoginTaskListener mListener;
  private Context mCtx;
  private String mAccount = null;
  private String mPassword = null;
  private ProgressDialog mProgressDlg;

  public LoginTask(Context context) {
    super();
    mCtx = context;
  }

  public LoginTask execute(String account, String password, LoginTaskListener listener) {
    loginListener(listener);
    setLoginInfo(account, password);
    execute();
    return this;
  }

  private void setLoginInfo(String account, String password) {
    mAccount = account;
    mPassword = password;
  }

  private void loginListener(LoginTaskListener listener) {
    unLoginListener();
    mListener = listener;
  }

  private void unLoginListener() {
    mListener = null;
  }

  @Override
  public void onPreExecute() {
    mProgressDlg = new ProgressDialog(mCtx);
    mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    mProgressDlg.setMessage(mCtx.getString(R.string.login_connect));
    mProgressDlg.setCancelable(true);
    mProgressDlg.show();
  }

  @Override
  public void onCancelled() {
    mProgressDlg.dismiss();
  }

  @Override
  public void onRequestProcessInfo(String info) {
  }

  @Override
  public String doInBackground(Void... values) {
    mLoginRequest = new LoginRequest(mCtx,mAccount, mPassword);
    mLoginRequest.post(this);
    return null;
  }

  @Override
  public void onProgressUpdate(Void... values) {
  }

  @Override
  public void onPostExecute(String result) {
    mProgressDlg.dismiss();
    if (!isCancelled()) {
      handleResult(result);
    }
  }

  private void handleResult(String result) {
    int resultCode = mLoginRequest.getResuleCode();
    if (BaseRequest.NORMAL_STATUS == resultCode) {
      handleNormalResult();
    } else {
      String reason = mLoginRequest.getReason();
      if (BaseRequest.NETWORK_EXCEPTION == resultCode) {
        reason = mCtx.getString(R.string.network_exception);
      }
      handleExceptionResult(reason);
    }
  }

  private void handleExceptionResult(final String reason) {
    if (mListener != null) {
      mListener.onLoginFinish(false, reason);
    }
  }

  private void handleNormalResult() {
    if (mListener != null) {
      mListener.onLoginFinish(true, "");
    }
  }

  public static interface LoginTaskListener {
    public void onLoginFinish(boolean success, String reason);
  }
}
