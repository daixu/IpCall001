package com.sqt001.ipcall.provider;

import android.content.Context;

import com.sqt001.ipcall.util.UserTask;


/*
 * Class CallTask do the task of getting
 * User Call from server.
 */
public class CallTask extends UserTask<Void, Void, String> implements BaseRequest.RequestListener {
    private Context mCtx;
    private CallRequest mCallRequest = null;
    private CallTaskListener mListener = null;
    private ExceptionResultHandler mResultHandler = null;
    private String mCalledNumber = null;
    private String mCallerNumber = null;

    public CallTask(Context context) {
        super();
        mCtx = context;
    }

    public CallTask execute(String calledNumber, String callerNumber,  CallTaskListener listener) {
        registerListener(listener);
        setNumbers(calledNumber, callerNumber);
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

    private void setNumbers(String called, String caller) {
        mCalledNumber = called;
        mCallerNumber = caller;
    }

    @Override
    public void onPreExecute() {			
    }

    @Override
    public void onCancelled() {
    }

    public String doInBackground(Void... values) {
        mCallRequest = new CallRequest(mCtx,mCalledNumber, mCallerNumber);
        mCallRequest.post(this);
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
        int resultCode = mCallRequest.getResuleCode();	
        if(BaseRequest.NORMAL_STATUS == resultCode) {
            handleNormalResult();
        } else {
            handleExceptionResult(resultCode);			
        }
    }

    private void handleNormalResult() {
        if(mListener != null) {
            mListener.onGetCallSuc();
        }
    }  

    private void handleExceptionResult(int resultCode) {
        if(mListener != null) {
            mListener.onExceptionResult();
        }

        if(mResultHandler == null) {
            mResultHandler = new ExceptionResultHandler(mCtx);
        }
        mResultHandler.handle(resultCode, mCallRequest.getReason(), new ExceptionResultHandler.ExceptionResultHandlerListener() {
            @Override
            public void afterInputMyNum(boolean trueIfOk) {
                if(trueIfOk) {
                    if(mListener != null) {
                        mListener.onInputMyNum();
                    }
                } else {
                    if(mListener != null) {
                        mListener.onCallTaskFail();
                    }
                }
            }

            @Override
            public void handleNetWorkFail() {
                if(mListener != null) {
                    mListener.onCallTaskFail();
                }
            }

            @Override
            public void afterAccountException(boolean trueIfOk) {
                if(mListener != null) {
                    mListener.onCallTaskFail();
                }
            }

            @Override
            public void handleOtherException() {
                if(mListener != null) {
                    mListener.onCallTaskFail();
                }
            }
        });
    }

    public static interface CallTaskListener {
        public void onGetCallSuc();
        public void onCancelGetCall();
        public void onExceptionResult();
        public void onInputMyNum();
        public void onCallTaskFail();
    }
}