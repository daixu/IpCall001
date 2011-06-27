package com.sqt001.ipcall.provider;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.activity.AccountActiveProgress;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.util.UserTask;

public class QueryAccountTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener{
  private BalanceRequest mGetBalanceRequest;
  private QueryAccountTaskListener mListener;
  private ProgressDialog mProgressDlg;
  private Activity mActivity;

  public QueryAccountTask(Activity activity) {
    super();
    mActivity = activity;
  }

  public QueryAccountTask execute(QueryAccountTaskListener listener) {
    registerListener(listener);
    execute();
    return this;
  }

  private void registerListener(QueryAccountTaskListener listener) {
    unRegisterListener();
    mListener = listener;
  }

  private void unRegisterListener() {
    mListener = null;
  }

  @Override
  public void onPreExecute() {
    mProgressDlg = new ProgressDialog(mActivity);
    mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    mProgressDlg.setMessage(mActivity.getString(R.string.binging));
    mProgressDlg.setCancelable(true);
    mProgressDlg.show();
  }

  @Override
  public void onCancelled() {
    mProgressDlg.dismiss();
  }

  public String doInBackground(Void... values) {
    mGetBalanceRequest = new BalanceRequest(mActivity);
    mGetBalanceRequest.post(this);
    return null;
  }

  public void onRequestProcessInfo(String info) {
  }

  @Override
  public void onProgressUpdate(Void... values) {
  }

  @Override
  public void onPostExecute(String result) {
    mProgressDlg.dismiss();
    if(!isCancelled()) {
      handleResult(result);
    }
  }

  private void handleResult(String result) {
    int resultCode = mGetBalanceRequest.getResuleCode();
    int isnewuser = AppPreference.getIsnewuser();
    if(BaseRequest.NORMAL_STATUS == resultCode && isnewuser==0) {
      handleNewAccount();
    } else if (BaseRequest.NORMAL_STATUS == resultCode && isnewuser==1) {
      handleOldAccount();
    } else{
      String reason = mGetBalanceRequest.getReason();
      handleExceptionResult(reason);
    }
  }
  
  private void handleExceptionResult(final String reason) {
    Intent intent = new Intent(mActivity, AccountActiveProgress.class);
    mActivity.startActivity(intent);
    mActivity.finish();
  }

  private void handleNewAccount() {
    if(mListener != null) {
      mListener.onQueryAccountFinish("0", "");
    }
  }  

  private void handleOldAccount() {
    if(mListener != null) {
      mListener.onQueryAccountFinish("1", "");
    }
  }

  public static interface QueryAccountTaskListener {
    public void onQueryAccountFinish(String isnewuser, final String reason);
  }
}
