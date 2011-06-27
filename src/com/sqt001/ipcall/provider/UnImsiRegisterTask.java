package com.sqt001.ipcall.provider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.activity.AccountActiveActivity;
import com.sqt001.ipcall.activity.FinalPromptActivity;
import com.sqt001.ipcall.util.UserTask;

/*
 * Class GetBlanceTask do the task of getting
 * User balance from server.
 */
public class UnImsiRegisterTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener {
  private BalanceRequest mGetBalanceRequest;
  private BalanceTaskListener mListener;
  private ProgressDialog mProgressDlg;
  private Activity mActivity;

  public UnImsiRegisterTask(Activity activity){
    super();
    mActivity = activity;
  }

  public UnImsiRegisterTask execute(BalanceTaskListener listener) {
    registerListener(listener);
    execute();
    return this;
  }

  private void registerListener(BalanceTaskListener listener) {
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
    mProgressDlg.setMessage(mActivity.getString(R.string.login_connect));
    mProgressDlg.setCancelable(true);
    mProgressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
      public void onCancel(DialogInterface dialog) {
        if (mListener != null) {
          mListener.onCancelGetBalance();
        }
      }
    });
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
    if (!isCancelled())
      handleResult(result);
  }

  private void handleResult(String result) {
     int resultCode = mGetBalanceRequest.getResuleCode();
    if (BaseRequest.NORMAL_STATUS == resultCode) {
      handleNormalResult();
    } else {
      String reason = mGetBalanceRequest.getReason();
      handleExceptionResult(reason);
    }
  }

  private void handleNormalResult() {
    Intent intent = new Intent(mActivity, FinalPromptActivity.class);
    Bundle bundle = new Bundle();
    bundle.putBoolean("showPwd", true);
    bundle.putBoolean("success", true);
    intent.putExtras(bundle);
    mActivity.startActivity(intent);
    mActivity.finish();
  }

  private void handleExceptionResult(String reason) {
    new AlertDialog.Builder(mActivity).setTitle(R.string.wrong).setMessage(reason)
    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Intent intent=new Intent(mActivity, AccountActiveActivity.class);
        mActivity.startActivity(intent);
        mActivity.finish();
      }
    }).show();
  }

  public static interface BalanceTaskListener {
    public void onCancelGetBalance();

    public void afterInputMyNum();
  }
}