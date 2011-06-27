package com.sqt001.ipcall.provider;

import android.app.ProgressDialog;
import android.content.Context;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.util.UserTask;

public class ModifyTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener {
  private ModifyRequest mModifyRequest;
  private ModifyListener mListener;
  private Context mCtx;
  private String mOldPassword = null;
  private String mNewPassword = null;
  private ProgressDialog mProgressDlg;

  public ModifyTask(Context context) {
    super();
    mCtx = context;
  }

  public ModifyTask execute(String oldPassword, String newPassword, ModifyListener listener) {
    loginListener(listener);
    setModifyInfo(oldPassword, newPassword);
    execute();
    return this;
  }

  private void setModifyInfo(String oldPassword,String newPassword) {
    mOldPassword = oldPassword;
    mNewPassword = newPassword;
  }

  private void loginListener(ModifyListener listener) {
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
    mProgressDlg.setMessage(mCtx.getString(R.string.modifying));
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
    mModifyRequest = new ModifyRequest(mCtx,mOldPassword, mNewPassword);
    mModifyRequest.post(this);
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
    int resultCode = mModifyRequest.getResuleCode();
    if (BaseRequest.NORMAL_STATUS == resultCode) {
      handleNormalResult();
    } else if (resultCode == 3) {
      String reason = mModifyRequest.getReason();
      reason = mCtx.getString(R.string.old_password_incorrect);
      handleExceptionResult(reason);
    } 
    else {
      String reason = mModifyRequest.getReason();
      if (BaseRequest.NETWORK_EXCEPTION == resultCode) {
        reason = mCtx.getString(R.string.network_exception);
      }
      handleExceptionResult(reason);
    }
  }

  private void handleExceptionResult(final String reason) {
    if (mListener != null) {
      mListener.onModifyPwdFinish(false, reason);
    }
  }

  private void handleNormalResult() {
    if (mListener != null) {
      mListener.onModifyPwdFinish(true, "");
    }
  }

  public static interface ModifyListener {
    public void onModifyPwdFinish(boolean success, String reason);
  }
}
