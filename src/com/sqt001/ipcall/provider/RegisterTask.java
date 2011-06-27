package com.sqt001.ipcall.provider;

import android.content.Context;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.util.UserTask;


/*
 * Class GetBlanceTask do the task of getting
 * User Register from server.
 */
public class RegisterTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener {
  private BalanceRequest mGetBalanceRequest;
  private RegisterTaskListener mListener;
  private Context mCtx;

  public RegisterTask(Context context) {
    super();
    mCtx = context;
  }

  public RegisterTask execute(RegisterTaskListener listener) {
    registerListener(listener);
    execute();
    return this;
  }

  private void registerListener(RegisterTaskListener listener) {
    unRegisterListener();
    mListener = listener;
  }

  private void unRegisterListener() {
    mListener = null;
  }

  @Override
  public void onPreExecute() {	
  }

  @Override
  public void onCancelled() {
  }

  public String doInBackground(Void... values) {
    mGetBalanceRequest = new BalanceRequest(mCtx);
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
    if(!isCancelled())
      handleResult(result);
  }

  private void handleResult(String result) {	
    int resultCode = mGetBalanceRequest.getResuleCode();
    if(BaseRequest.NORMAL_STATUS == resultCode) {
      handleNormalResult();
    } else {
      String reason = mGetBalanceRequest.getReason();
      if(BaseRequest.NETWORK_EXCEPTION == resultCode) {
        reason = mCtx.getString(R.string.network_exception);
      }
      handleExceptionResult(reason);
    }
  }
  
  private void handleNormalResult() {
    if(mListener != null) {
      mListener.onRegisterFinish(true, "");
    }
  }  

  private void handleExceptionResult(final String reason) {
    if(mListener != null) {
      mListener.onRegisterFinish(false, reason);
    }
  }

  public static interface RegisterTaskListener {
    public void onRegisterFinish(boolean success, final String reason);
  }
}