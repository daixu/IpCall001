package com.sqt001.ipcall.provider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.activity.TwelveKeyDialer;
import com.sqt001.ipcall.application.AppPreference;

public class CallerNumberSetter {
    private Listener listener;
    private TwelveKeyDialer mTwelveKeyDialer = null;
    
    public CallerNumberSetter() {
    }
    
    public CallerNumberSetter(TwelveKeyDialer twelveKeyDialer) {      
      this.mTwelveKeyDialer = twelveKeyDialer;
    }
    
    public void show(Context context, Listener listener) {
        setListener(listener);
        showInputDialog(context);
    }
    
    private void setListener(Listener listener) {
        if(listener != null) {
            this.listener = listener;
        } else {
            this.listener = new NullListener();
        }
    }
    private void showInputDialog(final Context context) {
        final EditText editor = new EditText(context);  
        String number = AppPreference.getMyNum();
        if (number.length()>0&&!number.equals("")) {
          editor.setText(number);
        } else{
          if (AppPreference.getAccount().length()>0&&!AppPreference.getAccount().equals("")) {
            editor.setText(AppPreference.getAccount());
          }
        }
        editor.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        new AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.input_num))
        .setIcon(android.R.drawable.ic_dialog_info)
        .setMessage(context.getString(R.string.your_answer_num))
        .setView(editor)
        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              String number = editor.getText().toString();
                if(number == null) {
                    number = "";
                }
                if((number.length() >= 4)&&(number.indexOf(".")<=0)&&(number.length() <= 11)) {
                    AppPreference.putMyNum(number);
                    if (mTwelveKeyDialer!=null) {
                      mTwelveKeyDialer.showCallerLable();
                    }
                    listener.onOk(number);
                } else {
                    Toast.makeText(context, R.string.number_format_err, Toast.LENGTH_LONG).show();
                    listener.onInputError();
                }
                
            }
        })
        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onCancel();
            }
        })
        .show();
    }
    
    public interface Listener {
        public void onOk(String number);
        public void onCancel();
        public void onInputError();
    }
    
    static class NullListener implements Listener {
        public void onOk(String number) {}
        public void onCancel() {}
        public void onInputError() {}
    }
}
