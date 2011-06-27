package com.sqt001.ipcall.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.contact.Account;
import com.sqt001.ipcall.provider.CallLogDbHelper;
import com.sqt001.ipcall.provider.CallLogEntry;
import com.sqt001.ipcall.provider.CallTask;
import com.sqt001.ipcall.provider.CallerNumberSetter;
import com.sqt001.ipcall.provider.Constants;
import com.sqt001.ipcall.util.CallMonitor;
import com.sqt001.ipcall.util.UserTask;

public class CallScreenActivity extends Activity {
    private CallTask mCallTask = null;
    private CallMonitor mCallMonitor = null;
    private String mCalledNumber = null;
    private String mCalledName = null;
    private String mCallerNumber = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.call_screen);   
        setupBackground();
        
        initNumbers();
        System.out.println(mCallerNumber);
        if(validateCaller()) {
            if(validateCalled()) {
                call();
            }
        }
    }
        
    @Override
    public void onDestroy() {
        cancelCallMonitor();
        cancelCallTask();
        super.onDestroy();
    }
    
    private void setupBackground() {//设置背景
        GradientDrawable grad = new GradientDrawable( 
                Orientation.TOP_BOTTOM, 
                new int[] {Color.GRAY, Color.BLACK} 
        ); 
        grad.setDither(true);
        getWindow().setBackgroundDrawable(grad);
        
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
    }
    
    private boolean validateCaller() {
        if(Account.isGuidActive()) {
            //mCallerNumber = "";
            return true;
        }
        
        if(mCallerNumber != null && mCallerNumber.length() >= 4) {
            return true;
        }
        
        CallerNumberSetter setter = new CallerNumberSetter();
        setter.show(this, new CallerNumberSetter.Listener() {
            @Override
            public void onOk(String number) {
              System.out.println(number);
                setCallerAndContinue(number);
            }
            
            @Override
            public void onInputError() {
                endCallScreen();
            }
            
            @Override
            public void onCancel() {
                endCallScreen();
            }
        });

        return false;
    }
    
    private void setCallerAndContinue(String number) {
        mCallerNumber = number;
        if(validateCalled()) {
            call();
        }
    } 
    
    private boolean validateCalled() {
        if(mCalledNumber == null || mCalledNumber.length() < 4) {
            new AlertDialog.Builder(this)  
            .setTitle(getString(R.string.exception))
            .setMessage(getString(R.string.called_number_err))
            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    endCallScreen();
                }
            })
            .show();
            return false;
        }
        return true;
    }
    
    private void call() {
        writeCallLog();
        showNumbers();
        startCallTask();
        startCallMonitor();
    }

    private void updateProgressText(int resId) {
        TextView t = (TextView)findViewById(R.id.calling_text);
        t.setText(resId);
    }
    
    private void showTimeOverMsgSecond() {
        TextView t = (TextView)findViewById(R.id.calling_text_second);
        String format = getString(R.string.calling_time_over_second);
        String msg = null;
        if(mCallerNumber != null && mCallerNumber.length() > 0) {
            msg = String.format(format, mCallerNumber);
        } else {
            msg = String.format(format, "");
        }
        t.setText(msg);
    }
    
    private void initNumbers() {
        //check extras
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            return;
        }
        
        //called number
        mCalledNumber = extras.getString(Constants.Call.CALLED_NUM);
        mCalledNumber = mCalledNumber.replaceAll("-","");
        
        //called name
        mCalledName = extras.getString(Constants.Call.CALLED_NAME);
        
        //caller number
        mCallerNumber = AppPreference.getMyNum();
        System.out.println(mCallerNumber);
    }
    
    private void showNumbers() {
        //show called
        String called = (mCalledName != null && mCalledName.length() > 0) ? mCalledName : mCalledNumber;
        TextView nameText = (TextView)findViewById(R.id.calling_name);
        nameText.setText(called);
    }
    
    void writeCallLog() {
        if(mCalledNumber == null) {
            return;
        }
        if(mCalledName == null) {
            mCalledName = mCalledNumber;
        }
        
        Time time = new Time();
        time.setToNow();
        String timeStr = time.format("%Y-%m-%d  %H:%M:%S");
        CallLogDbHelper.getDb().createlog(new CallLogEntry(mCalledName, mCalledNumber, timeStr));
    }
    
    private void stopAndHideProgressbar() {
        ProgressBar bar = (ProgressBar)findViewById(R.id.calling_progress_bar);
        bar.setVisibility(View.GONE);
    }
    
    private void startCallTask() {
        //check number
        if(mCalledNumber == null) {
            return;
        }
        
        //update progress text
        updateProgressText(R.string.calling_first);
        
        //cancel last one
        cancelCallTask();
        
        //start new task
        mCallTask = new CallTask(this).execute(mCalledNumber, mCallerNumber, new CallTask.CallTaskListener() {
            public void onCancelGetCall() {
                cancelCallTask();
            }

            public void onGetCallSuc() {
                int id = AppPreference.isGingerbreadOrLater() ? R.string.calling_second_no_answer : R.string.calling_second;
                updateProgressText(id);
            }
            
            public void onExceptionResult() {
              cancelCallMonitor();
            }

            public void onInputMyNum() {
                startCallTask();
                startCallMonitor();
            }
            
            public void onCallTaskFail() {
                cancelCallTask();
                cancelCallMonitor();
                endCallScreen();
            }
        });
    }
    
    private void cancelCallTask() {
        if(mCallTask != null && mCallTask.getStatus() == UserTask.Status.RUNNING) {
            mCallTask.cancel(true);
            mCallTask = null;
        }
    }
    
    private void startCallMonitor() {//监听通话
        //cancel last 
        cancelCallMonitor();
        
        //start new listener
        if(mCallMonitor == null) {
            mCallMonitor = new CallMonitor(this);
        }
        mCallMonitor.listenAndAnswer(new CallMonitor.CallMonitorListener() {
            public void onListenTimeOvered() {
                stopAndHideProgressbar();
                updateProgressText(R.string.calling_time_over);
                showTimeOverMsgSecond();
                Toast.makeText(CallScreenActivity.this, R.string.calling_time_over, Toast.LENGTH_LONG).show();
            }

            public void onCallRingWithoutAnswer() {
                stopAndHideProgressbar();
                updateProgressText(R.string.calling_answer_by_self);
                Toast.makeText(CallScreenActivity.this, R.string.calling_answer_by_self, Toast.LENGTH_LONG).show();
            }
            
            public void onCallAnswered() {
                stopAndHideProgressbar();
                updateProgressText(R.string.calling_third);
                Toast.makeText(CallScreenActivity.this, R.string.calling_third, Toast.LENGTH_LONG).show();
            }
            
            public void onCallEnd() {
                endCallScreen();
            }
        });
    }
    
    private void endCallScreen() {
        finish();
    }
    
    private void cancelCallMonitor() {
        if(mCallMonitor != null) {
            mCallMonitor.cancel();
            mCallMonitor = null;
        }
    }
}
