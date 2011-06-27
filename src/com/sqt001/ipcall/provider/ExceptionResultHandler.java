package com.sqt001.ipcall.provider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.contact.Account;
import com.sqt001.ipcall.util.AboutDlg;

/*
 * Handle exception.
 */
public class ExceptionResultHandler {
  private Context mCtx = null;
  private ExceptionResultHandlerListener mListener = null;

  public ExceptionResultHandler(Context context) {
    mCtx = context;
  }

  public void handle(int code, String reason, ExceptionResultHandlerListener listener) {
    registerListener(listener);
    handle(code, reason);
  }
	
  private void registerListener(ExceptionResultHandlerListener listener) {
    unRegisterListener();
    if(listener != null) {
      mListener = listener;
    } else {
      mListener = new DefaultExceptionResultHandlerListener();
    }
  }
	
  private void unRegisterListener() {
    mListener = null;
  }
	
  private void handle(int code, String reason) {
    Handler handler = makeHandler(code);
    handler.handle(reason);
  }
    
  /*
   * Create handler according code
   */
  private Handler makeHandler(int code) {
    switch(code) {
      case BaseRequest.NEED_INPUT_NUM_STATUS:
        return new InputMyNumHandler();
      case BaseRequest.ACCOUNT_EXCEPTION_STATUS:
        return new AccountExceptionHandler();
      case BaseRequest.OTHER_ERR_STATUS:
        return new ShowMessageHandler();
      case BaseRequest.NETWORK_EXCEPTION:
        return new NetworkFailHandler();
    }
    return new NullHandler(); 
  }
	
  /*
   * Base class for all handler
   */
  private static abstract class Handler {
    public abstract boolean handle(String reason);
  }
	
  /*
   * Null Object
   */
  private class NullHandler extends Handler {
    @Override
    public boolean handle(String reason) {
      return true;
    }
  } //class NullHandler
	
  /*
   * Handle input number
   */
  private class InputMyNumHandler extends Handler {
    @Override
    public boolean handle(String reason) {
      handleInputMyNum(reason);
      return true;
    }
	    
    private void handleInputMyNum(String reason) {
      CallerNumberSetter setter = new CallerNumberSetter();
      setter.show(mCtx, new CallerNumberSetter.Listener() {
          @Override
          public void onOk(String number) {
            mListener.afterInputMyNum(true);
          }
                
          @Override
          public void onInputError() {
            mListener.afterInputMyNum(false);
          }
                
          @Override
          public void onCancel() {
            mListener.afterInputMyNum(true);
          }
        });
    }
  } //class InputMyNumHandler

	
  /*
   * Handle account exception. 
   */
  private class AccountExceptionHandler extends Handler {
    @Override
    public boolean handle(String reason) {
      handleAcountException(reason);
      return true;
    }

    private void handleAcountException(String reason) {
      new AlertDialog.Builder(mCtx)  
          .setTitle(mCtx.getString(R.string.account_exception))
          .setMessage(reason)
          .setPositiveButton(mCtx.getString(R.string.ok), new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                sendExceptionSms();
                mListener.afterAccountException(true);
              }
            })
          .setNegativeButton(mCtx.getString(R.string.cancel), new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                mListener.afterAccountException(false);
              }
            })
          .show();
    }

    private void sendExceptionSms() {
        Account.activeAccount(mCtx);
    }
  } //class AccountExceptionHandler
	
  /*
   * Show exception message
   */
  private class ShowMessageHandler extends Handler {
    @Override
    public boolean handle(String reason) {
      handleOtherException(reason);
      return true;
    }

    /*
     * Show other exception message.
     */
    private void handleOtherException(String reason) {
      new AboutDlg(mCtx).show(mCtx.getString(R.string.exception), reason,  new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mListener.handleOtherException();
          }
        });
    }
  }
	
  /*
   * Show exception message
   */
  private class NetworkFailHandler extends Handler {
    @Override
    public boolean handle(String reason) {
      handleNetworkException(reason);
      return true;
    }

    /*
     * Show other exception message.
     */
    private void handleNetworkException(String reason) {
      new AlertDialog.Builder(mCtx)  
          .setTitle(mCtx.getString(R.string.exception))
          .setMessage(mCtx.getString(R.string.network_exception))
          .setPositiveButton(mCtx.getString(R.string.ok),  new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                mListener.handleNetWorkFail();
              }
            })
          .show();
    }
  }
	
  /*
   * Notify the user to input number
   */
  public interface ExceptionResultHandlerListener {
    public void afterInputMyNum(boolean trueIfOk);
    public void handleNetWorkFail();
    public void afterAccountException(boolean trueIfOk);
    public void handleOtherException();
  }
	
  static class DefaultExceptionResultHandlerListener implements ExceptionResultHandlerListener {
    public void afterInputMyNum(boolean trueIfOk) {};
    public void handleNetWorkFail() {};
    public void afterAccountException(boolean trueIfOk) {};
    public void handleOtherException() {};
  }
}
