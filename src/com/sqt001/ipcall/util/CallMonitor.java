package com.sqt001.ipcall.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import com.sqt001.ipcall.application.AppPreference;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

/*
 * Listen incoming call,
 * accept the call.
 */
public class CallMonitor {
  private static final String CALLINTENT = "android.intent.action.PHONE_STATE";
  private static final int LISTENDURATION = 30*1000;
  private static final int TIME_TO_STOP_LISTEN_MESSAGE = 1;
  
  private static final int ANSWERDURATION = 2000;
  private static final int TIME_TO_ANSWER_INCOMING_CALL = 2;

  private Context mCtx;
  private CallMonitorListener mListener; 
  private CallListener mCallListener;
  private CallAnswerer mCallAnswerer;
  private Timer mTimer;
  private Handler mHandler;
  
  private int ANSWERCOUNTMAX = 1;
  private int mAnswerCount = 0;

  public CallMonitor(Context context) {
    mCtx = context;
  }
	
  public void listenAndAnswer(CallMonitorListener listener) {
    registernListener(listener);
    registerCallListener();
    createTimerHandler();
    startTimer();
  }
  
  public void cancel() {
    stopTimer();
    unRegisterCallListener();
  }
	
  private void registernListener(CallMonitorListener listener) {
    unregisterListener();
    mListener = listener;
  }

  private void unregisterListener() {
    mListener = null;
  }

  private void registerCallListener() {
    unRegisterCallListener();
    if(mCallListener == null) {
      mCallListener = new CallListener();
      mCtx.registerReceiver(mCallListener,  new IntentFilter(CALLINTENT));
    }
  }

  private void unRegisterCallListener() {
    if(mCallListener != null) {
      mCtx.unregisterReceiver(mCallListener);
      mCallListener = null;
    }
  }
  
  private void createTimerHandler() {
      mHandler = new Handler(){   
          public void handleMessage(Message msg) {   
            switch (msg.what) {       
              case TIME_TO_STOP_LISTEN_MESSAGE: {
                handleTimeToStopListen();
                break;
              }
              
              case TIME_TO_ANSWER_INCOMING_CALL: {
                  handleTimeToAnswerIncomingCall();
                  break;
              }
            }   
            super.handleMessage(msg);   
          }
        };
    }

  private void startTimer() {
    stopTimer();
    createListenTimer();
  }


  
  private void createListenTimer() {
      createTimerTask(TIME_TO_STOP_LISTEN_MESSAGE, LISTENDURATION);
  }

  private void createTimerTask(final int what, final int duration) {
    if(mTimer != null) {
      mTimer.cancel();
    }
    mTimer = new Timer();

    TimerTask task = new TimerTask() {
        public void run() {
          Message message = new Message();       
          message.what = what;  
          mHandler.sendMessage(message); 
        }
      };
    mTimer.schedule(task, duration);
  }

  private void handleTimeToStopListen() {
    unRegisterCallListener();
    if(mListener != null) {
      mListener.onListenTimeOvered();
    }
  }

  private void stopTimer() {
    if(mTimer != null) {
      mTimer.cancel();
    }
    mTimer = null;
  }
	
  private void handleCallEnd() {
    if(mListener != null) {
      mListener.onCallEnd();
    }
  }

  /*
   * Listen the incoming call
   */
  private class CallListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      TelephonyManager tm = (TelephonyManager)mCtx.getSystemService(Service.TELEPHONY_SERVICE);

      switch ( tm.getCallState()) {
        case TelephonyManager.CALL_STATE_IDLE:
          handleStateIdle();
          break;

        case TelephonyManager.CALL_STATE_OFFHOOK:
          handleStateOffhook();
          break;

        case TelephonyManager.CALL_STATE_RINGING:
          handleStateRinging();
          break;
      }
    }		
		
    private void handleStateIdle() {
      stopTimer();
      unRegisterCallListener();
      handleCallEnd();
    }
		
    private void handleStateOffhook() {
      //Do nothing
    }
		
    private void handleStateRinging() {
      if(needNotAnswerThisIncomingCall()) {
          return;
      }
      stopTimer();
      createAnswerCallTimer();
    }
  } //class CallListener
  
  private void createAnswerCallTimer() {
      createTimerTask(TIME_TO_ANSWER_INCOMING_CALL, ANSWERDURATION);
  }
  
  private boolean needNotAnswerThisIncomingCall() {
      if(mAnswerCount >= ANSWERCOUNTMAX) {
          return true;
      }
      mAnswerCount ++;
      return false;
  }

  private void  handleTimeToAnswerIncomingCall() {
    if(AppPreference.isGingerbreadOrLater()) {
      notifyIncomingCall();
    } else {
      answerIncomingCall();
    }
  }
  
  private void  notifyIncomingCall() {
    if(mListener != null) {
      mListener.onCallRingWithoutAnswer();
    }
  }

  private void answerIncomingCall() {
    if(mCallAnswerer == null) {
      mCallAnswerer = new CallAnswerer();
    }
    mCallAnswerer.answer();

    if(mListener != null) {
      mListener.onCallAnswered();
    }
  }

  /*
   * Answer the incoming call.
   */
  private class CallAnswerer {
    public void answer() {
      try {
        tryAnswer();
      } 
      catch (SecurityException e) {
        throw e;
      } 
      catch (IllegalArgumentException e) {
        throw e;
      }
      catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } 
      catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } 
      catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    private void tryAnswer() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      TelephonyManager mManager = (TelephonyManager)mCtx.getSystemService(Context.TELEPHONY_SERVICE);

      //Get private method: TelephonyManager.getITelephony()
      Method fGetITelephony = mManager.getClass().getDeclaredMethod("getITelephony");
      fGetITelephony.setAccessible(true);

      //Get ITelephony object
      Object mTelephony = fGetITelephony.invoke(mManager);
      Class cTelephony = mTelephony.getClass();

      //Get private method: ITelephony.endCall();
      Method fAnswarRingingCall = cTelephony.getMethod("answerRingingCall");        
      fAnswarRingingCall.setAccessible(true);

      //Answer
      fAnswarRingingCall.invoke(mTelephony);
    }
  } //class CallAnswer

  public static interface CallMonitorListener {
    public void onCallAnswered();
    public void onCallRingWithoutAnswer();
    public void onCallEnd();
    public void onListenTimeOvered();
  } 
}

