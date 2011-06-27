package com.sqt001.ipcall.provider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.util.UserTask;


/*
 * Class GetBlanceTask do the task of getting
 * User Call from server.
 */
public class ChargeTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener {
	private Context mCtx;
	private ChargeRequest mChargeRequest = null;
	private CallTaskListener mListener = null;
	private ProgressDialog mProgressDlg = null;
	private ExceptionResultHandler mResultHandler = null;
	private String mSerialNumber = null;
  private String mPwdNumber = null;
  private String mPhoneNumber = null;
    
	public ChargeTask(Context context) {
		super();
		mCtx = context;
	}

	public ChargeTask execute(String serial, String pwd, String phone, CallTaskListener listener) {
		registerListener(listener);
		setChardInfo(serial, pwd, phone);
		execute();
		return this;
	}

	private void registerListener(CallTaskListener listener) {
		unRegisterListener();
		mListener = listener;
	}

	private void unRegisterListener() {
		mListener = null;
	}

	private void setChardInfo(String serial, String pwd, String phone) {
		mSerialNumber = serial;
		mPwdNumber = pwd;
		mPhoneNumber = phone;
	}

	@Override
	public void onPreExecute() {	
		mProgressDlg = new ProgressDialog(mCtx);
		mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER );
		mProgressDlg.setMessage(mCtx.getString(R.string.recharging));
		mProgressDlg.setCancelable(true);
		mProgressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				if(mListener != null) {
					mListener.onCancelCharge();
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
		mChargeRequest = new ChargeRequest(mCtx,mSerialNumber, mPwdNumber, mPhoneNumber);
		mChargeRequest.post(this);
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
		int resultCode = mChargeRequest.getResuleCode();	
		if(BaseRequest.NORMAL_STATUS == resultCode) {
			handleNormalResult();
		} else {
			handleExceptionResult(resultCode);			
		}
	}

	private void handleNormalResult() {
	    new AlertDialog.Builder(mCtx)  
	    .setTitle(mCtx.getString(R.string.recharge))
	    .setMessage(mCtx.getString(R.string.recharge_suc))
	    .setPositiveButton(mCtx.getString(R.string.ok), null)
	    .show();
	}  

	private void handleExceptionResult(int resultCode) {
		if(mResultHandler == null) {
			mResultHandler = new ExceptionResultHandler(mCtx);
		}
		mResultHandler.handle(resultCode, mChargeRequest.getReason(), new ExceptionResultHandler.ExceptionResultHandlerListener() {
		    @Override
		    public void afterInputMyNum(boolean trueIfOk) {
		        if(trueIfOk) {
		            if(mListener != null) {
		                mListener.onInputMyNum();
		            }
		        }
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

	public static interface CallTaskListener {
		public void onCancelCharge();
		public void onInputMyNum();
	}
}