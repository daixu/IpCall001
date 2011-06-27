package com.sqt001.ipcall.provider;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.util.UserTask;

public class GetPasswordTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener {
  
  private GetPasswordRequest mGetPasswordRequest;
  private GetPasswordListener mListener;
  private Context mCtx;
  private String mNumber;
  private ProgressDialog mProgressDlg;
  
  public GetPasswordTask(Context context){
    super();
    mCtx = context;
  }
  
  public GetPasswordTask execute(String number, GetPasswordListener listener){
    loginListener(listener);
    setGetPasswordInfo(number);
    execute();
    return this;
  }
  private void loginListener(GetPasswordListener listener) {
    unLoginListener();
    mListener = listener;
  }

  private void unLoginListener() {
    mListener = null;
  }

  private void setGetPasswordInfo(String number) {
    mNumber = number;
  }
  
  @Override
  public void onPreExecute(){
    mProgressDlg = new ProgressDialog(mCtx);
    mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    mProgressDlg.setMessage(mCtx.getString(R.string.get_password));
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
  public String doInBackground(Void... params) {
    mGetPasswordRequest = new GetPasswordRequest(mCtx,mNumber);
    mGetPasswordRequest.post(this);
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
    int resultCode = mGetPasswordRequest.getResuleCode();
    Log.i("resultCode", String.valueOf(resultCode));
    if (BaseRequest.NORMAL_STATUS == resultCode) {
      handleNormalResult();
    } else if (resultCode == 3) {
      String reason = mGetPasswordRequest.getReason();
      reason = mCtx.getString(R.string.account_unbind);
      handleExceptionResult(reason);
    }
    else {
      String reason = mGetPasswordRequest.getReason();
      if (BaseRequest.NETWORK_EXCEPTION == resultCode) {
        reason = mCtx.getString(R.string.network_exception);
      }
      handleExceptionResult(reason);
    }
  }

  private void handleExceptionResult(final String reason) {
    if (mListener != null) {
      mListener.onGetPasswordFinish(false, reason);
    }
  }

  private void handleNormalResult() {
    if (mListener != null) {
      mListener.onGetPasswordFinish(true, "");
    }
  }
  
  public static interface GetPasswordListener {
    public void onGetPasswordFinish(boolean success, String reason);
  }

}
