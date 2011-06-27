package com.sqt001.ipcall.provider;

import android.app.ProgressDialog;
import android.content.Context;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.util.UserTask;

public class BindTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener {

  private BalanceRequest mGetBalanceRequest;
  private BindTaskListener mListener;
  private Context mCtx;
  private ProgressDialog mProgressDlg;

  public BindTask(Context context) {
    super();
    mCtx = context;
  }

  public BindTask execute(BindTaskListener listener) {
    registerListener(listener);
    execute();
    return this;
  }

  private void registerListener(BindTaskListener listener) {
    unRegisterListener();
    mListener = listener;
  }

  private void unRegisterListener() {
    mListener = null;
  }

  @Override
  public void onPreExecute() {
    mProgressDlg = new ProgressDialog(mCtx);
    mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    mProgressDlg.setMessage(mCtx.getString(R.string.binging));
    mProgressDlg.show();
  }

  @Override
  public void onCancelled() {
    mProgressDlg.dismiss();
  }

  @Override
  public String doInBackground(Void... params) {
    mGetBalanceRequest = new BalanceRequest(mCtx);
    mGetBalanceRequest.post(this);
    return null;
  }

  @Override
  public void onRequestProcessInfo(String info) {
  }

  @Override
  public void onProgressUpdate(Void... values) {
  }

  @Override
  public void onPostExecute(String result) {
    mProgressDlg.dismiss();
    if (!isCancelled())
      handleResult(result);
  }

  private void handleResult(String result) {
    int resultCode = mGetBalanceRequest.getResuleCode();
    String number = AppPreference.getAccount();
    if (number.length()>0) {
      handleNormalResult();
    } else if (resultCode == 3) {
      String reason = mGetBalanceRequest.getReason();
      reason = mCtx.getString(R.string.bind_fail);
      handleExceptionResult(reason);
    } else {
      String reason = mGetBalanceRequest.getReason();
      if (BaseRequest.NETWORK_EXCEPTION == resultCode) {
        reason = mCtx.getString(R.string.network_exception);
      }
      handleExceptionResult(reason);
    }
  }
  
  private void handleNormalResult() {
    if (mListener != null) {
      mListener.onBindFinish(true, "");
    }
  }

  private void handleExceptionResult(final String reason) {
    if (mListener != null) {
      mListener.onBindFinish(false, reason);
    }
  }

  public static interface BindTaskListener {
    public void onBindFinish(boolean success, final String reason);
  }

}
