package com.sqt001.ipcall.provider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.util.FileUtils;
import com.sqt001.ipcall.util.UserTask;


/*
 * Class GetBlanceTask do the task of getting
 * User Update from server.
 */
public class UpdateTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener {
  private BalanceRequest mGetBalanceRequest;
  private UpdateTaskListener mListener;
  private ProgressDialog mProgressDlg;
  private ExceptionResultHandler mResultHandler;
  private Context mCtx;
  private DownloadTask mDownloadTask;
  
  public UpdateTask(Context context) {
    super();
    mCtx = context;
  }
  
  public UpdateTask execute(UpdateTaskListener listener) {
    registerListener(listener);
    execute();
    return this;
  }
  
  private void registerListener(UpdateTaskListener listener) {
    unRegisterListener();
    mListener = listener;
  }
  
  private void unRegisterListener() {
    mListener = null;
  }

  @Override
  public void onPreExecute() {  
    mProgressDlg = new ProgressDialog(mCtx);
    mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER );
    mProgressDlg.setMessage(mCtx.getString(R.string.fetching_update));
    mProgressDlg.setCancelable(true);
    mProgressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
      public void onCancel(DialogInterface dialog) {
        if(mListener != null) {
          mListener.onCancelGetUpdate();
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
    mProgressDlg.dismiss();
    if(!isCancelled())
        handleResult(result);
  }
  
  private void handleResult(String result) {  
    int resultCode = mGetBalanceRequest.getResuleCode();
    boolean isNew = AppPreference.getNewVersionMsg().length() > 0 && AppPreference.getNewVersionUrl().length() > 0;
    if(isNew) {
      handleUpdateResult();
    } else if (BaseRequest.NORMAL_STATUS == resultCode) {
      new AlertDialog.Builder(mCtx)  
      .setTitle(mCtx.getString(R.string.update_ver))
      .setMessage(mCtx.getString(R.string.is_newest_ver))
      .setPositiveButton(mCtx.getString(R.string.ok), null)
      .show();
    } else {
      handleExceptionResult(resultCode);
    }
  }

  private void handleUpdateResult() {
    new AlertDialog.Builder(mCtx).setTitle(R.string.new_version_hint)
    .setMessage(AppPreference.getNewVersionMsg())
    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        String url = AppPreference.getNewVersionUrl();
        download(url);
      }
    }).setNegativeButton(R.string.cancel, null).show();
  }

  private void download(String url) {
    if (!FileUtils.isSDCardReady()) {
      new AlertDialog.Builder(mCtx).setMessage(R.string.sdcard_not_ready)
          .setPositiveButton(R.string.ok, null).show();
      return;
    }

    mDownloadTask = new DownloadTask(mCtx, url);
    mDownloadTask.execute();
  }
  
  private void handleExceptionResult(int resultCode) {
    if(mResultHandler == null) {
      mResultHandler = new ExceptionResultHandler(mCtx);
    }
    mResultHandler.handle(resultCode, mGetBalanceRequest.getReason(), new ExceptionResultHandler.ExceptionResultHandlerListener() {
        @Override
        public void afterInputMyNum(boolean trueIfOk) {
      }

            @Override
            public void handleNetWorkFail() {
            }

            @Override
            public void afterAccountException(boolean trueIfOk) {
            }

            @Override
            public void handleOtherException() {
            }
    });
  }
  
  public static interface UpdateTaskListener {
    public void onCancelGetUpdate();
    public void onOkGetUpdate(String url);
  }
}