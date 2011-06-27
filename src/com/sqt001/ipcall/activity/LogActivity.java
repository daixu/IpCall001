package com.sqt001.ipcall.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.util.LogUtil;

public class LogActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.log_viewer);   
        populateLogText();
    }
    
    private void populateLogText() {
        TextView viewer = (TextView)findViewById(R.id.LogViewText);
        viewer.setText(LogUtil.r());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.log_option, menu);
      super.onCreateOptionsMenu(menu);
      return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch(item.getItemId()) {
        case R.id.log_option_email:
          handleEmailItemSelected();
          return true;
      }
      return super.onOptionsItemSelected(item);
    }
      
    private void handleEmailItemSelected() {
        Intent intent = new Intent(Intent.ACTION_SEND); 
        intent.setType("message/rfc822"); 
        String[] recivers = new String[]{"james.chow@newding.com"}; 
        String emailBody = LogUtil.r();
        intent.putExtra(Intent.EXTRA_EMAIL, recivers); 
        intent.putExtra(Intent.EXTRA_TEXT, emailBody); 
        intent.putExtra(Intent.EXTRA_SUBJECT, "IpCall log message"); 
        startActivity(Intent.createChooser(intent, "发送"));
    }
}
