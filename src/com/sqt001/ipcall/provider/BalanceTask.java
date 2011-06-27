package com.sqt001.ipcall.provider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.activity.AccountActiveActivity;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.util.FileUtils;
import com.sqt001.ipcall.util.UserTask;

/*
 * Class GetBlanceTask do the task of getting
 * User balance from server.
 */
public class BalanceTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener {
  private BalanceRequest mGetBalanceRequest;
  private BalanceTaskListener mListener;
  private ProgressDialog mProgressDlg;
  private Activity mActivity;
  private DownloadTask mDownloadTask;

  public BalanceTask(Activity activity) {
    super();
    mActivity = activity;
  }

  public BalanceTask execute(BalanceTaskListener listener) {
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
    mProgressDlg.setMessage(mActivity.getString(R.string.fetching_balance));
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
//    int resultCode = 55;
    if (BaseRequest.NORMAL_STATUS == resultCode) {
      handleNormalResult();
    } else if (BaseRequest.UPDATE_UPGRADE == resultCode) {
      handleUpdateResult();
    } else {
      String reason = mGetBalanceRequest.getReason();
      handleExceptionResult(reason);
    }
  }

  private void handleUpdateResult() {
    AppPreference.markForceUpdate(true);
    String text = AppPreference.getUpdateText();
    new AlertDialog.Builder(mActivity)
        .setIcon(android.R.drawable.ic_menu_info_details)
        .setTitle(R.string.update_versions)
        .setMessage(text)
        .setPositiveButton(mActivity.getString(R.string.upgrade), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            AppPreference.markForceUpdate(false);
            String url = AppPreference.getUpdateUrl();
            if ((url != null) && (url != null)) {
              download(url);
            }
          }
        })
        .create()
        .show();
  }
  
  private void download(String url) {
    if (!FileUtils.isSDCardReady()) {
      new AlertDialog.Builder(mActivity).setMessage(R.string.sdcard_not_ready)
          .setPositiveButton(R.string.ok, null).show();
      return;
    }

    mDownloadTask = new DownloadTask(mActivity, url);
    mDownloadTask.execute();
  }

  private void handleNormalResult() {
    float balanceVal = AppPreference.getUserBalance();
    balanceVal /= 100.0;
    String messageFormat = mActivity.getString(R.string.your_balance_is);
    String message = String.format(messageFormat, balanceVal);

    new AlertDialog.Builder(mActivity).setTitle(mActivity.getString(R.string.balance)).setMessage(message)
        .setPositiveButton(mActivity.getString(R.string.ok), null).show();
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