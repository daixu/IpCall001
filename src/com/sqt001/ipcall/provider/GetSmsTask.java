package com.sqt001.ipcall.provider;

import android.content.Context;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.util.UserTask;

public class GetSmsTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener {

  private Context mCtx;
  private GetSmsRequest mGetSmsRequest = null;
  private GetSmsTaskListener mListener = null;

  public GetSmsTask(Context context) {
    super();
    this.mCtx = context;
  }

  public GetSmsTask execute(GetSmsTaskListener listener) {
    registerListener(listener);
    execute();
    return null;
  }

  private void registerListener(GetSmsTaskListener listener) {
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

  @Override
  public void onRequestProcessInfo(String info) {
  }

  @Override
  public String doInBackground(Void... params) {
    mGetSmsRequest = new GetSmsRequest(mCtx);
    mGetSmsRequest.post(this);
    return null;
  }

  @Override
  public void onProgressUpdate(Void... values) {
  }

  @Override
  public void onPostExecute(String result) {
    if (!isCancelled()) {
      handleResult(result);
    }
  }

  private void handleResult(String result) {
    int resultCode = mGetSmsRequest.getResuleCode();
    if (BaseRequest.NORMAL_STATUS == resultCode) {
      handleNormalResult();
    } else {
      String reason = mGetSmsRequest.getReason();
      reason = mCtx.getString(R.string.network_exception);
      handleExceptionResult("", reason);
    }
  }
  
  private void handleNormalResult() {
    if (mListener != null) {
      mListener.onGetSmsTaskFinish(true, "");
    }
  }

  private void handleExceptionResult(final String address, String reason) {
    if(mListener != null) {
      mListener.onGetSmsTaskFinish(false, reason);
    }
  }

  public static interface GetSmsTaskListener {
    public void onGetSmsTaskFinish(boolean success, String reason);
  }
}
