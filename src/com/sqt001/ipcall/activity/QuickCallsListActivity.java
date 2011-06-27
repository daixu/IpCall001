package com.sqt001.ipcall.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.contact.Account;
import com.sqt001.ipcall.provider.Constants;
import com.sqt001.ipcall.provider.QuickContactDbAdapter;
import com.sqt001.ipcall.provider.QuickContactDbHelper;

public class QuickCallsListActivity extends ListActivity {
    private QuickContactDbAdapter mDb;
    private SimpleCursorAdapter mAdapter;
    private Cursor mCursor;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.quick_contact);
        registerContextMemuForListview();
    }
    
    private void registerContextMemuForListview() {
        registerForContextMenu(getListView());
    }   
    
    @Override
    public void onResume() {
        super.onResume();
        populateRecentCallList();
    }

    private void populateRecentCallList() {
        mDb = QuickContactDbHelper.getDb();
        mCursor = mDb.fetchAlllogs();    
        startManagingCursor(mCursor);
        
        mAdapter = new SimpleCursorAdapter(this, 
                // Use a template that displays a text view
                android.R.layout.simple_list_item_2, 
                // Give the cursor to the list adatper
                mCursor, 
                // Map the NAME column in the people database to...
                new String[] {QuickContactDbAdapter.KEY_NAME, QuickContactDbAdapter.KEY_NUMBER},
                // The "text1" view defined in the XML template
                new int[] {android.R.id.text1, android.R.id.text2}); 
        setListAdapter(mAdapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.quick_contact_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.quick_contact_context_call: {
                handleCallContextItemClick(item);
                return true;
            }

            case R.id.quick_contact_context_del: {
                handleDelContextItemClick(item);
                return true;
            }
        }

        return super.onContextItemSelected(item);
    }
    
    private int getSelectedItemPositonFromContextMenu(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo =(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        return menuInfo.position;
    }
    
    private void handleCallContextItemClick(MenuItem item) {
        call(getSelectedItemPositonFromContextMenu(item));
    }
    
    private void handleDelContextItemClick(MenuItem item) {
        delete(getSelectedItemPositonFromContextMenu(item));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.quick_contact_option, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.quick_contact_option_call:
            handleCallOptionItemClick();
            return true;
            
        case R.id.quick_contact_option_del:
            handleDelOptionItemClick();
            return true;
            
        case R.id.quick_contact_option_clear:
            handleClearOptionItemClick();
            return true;
            
        default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private int getSelectedItemPositonFromOptionMenu() {        
        return getListView().getSelectedItemPosition();
    }
    
    private void handleCallOptionItemClick() {
        call(getSelectedItemPositonFromOptionMenu());     
    }
    
    private void handleDelOptionItemClick() {
        delete(getSelectedItemPositonFromOptionMenu());
    }
    
    private void handleClearOptionItemClick() {
        clear();
    }
    
    private void call(int position) {
        if(Account.activeAccountIfNeed(this)) {
            return;
        }
        
        //validate position.
        if(positionIsInValid(position)) {
            return;
        }
        
              //number
        String number = mCursor.getString(mCursor.getColumnIndexOrThrow(QuickContactDbAdapter.KEY_NUMBER));
        if(number == null || number.length() < 3) {
            return;
        }
        
        //name
        String name = mCursor.getString(mCursor.getColumnIndexOrThrow(QuickContactDbAdapter.KEY_NAME));
        
        //start calling activity
        Intent callIntent = new Intent(this, CallScreenActivity.class);
        if(name != null) {
            callIntent.putExtra(Constants.Call.CALLED_NAME, name);
        }
        callIntent.putExtra(Constants.Call.CALLED_NUM, number);
        startActivity(callIntent);
    }
    
    private void delete(int position) {
        //validate position.
        if(positionIsInValid(position)) {
            return;
        }
        mCursor.moveToPosition(position);
        int id = mCursor.getInt(mCursor.getColumnIndexOrThrow(QuickContactDbAdapter.KEY_ROWID));
        if(mDb.deletelog(id)) {
            mCursor.requery();
            mAdapter.notifyDataSetChanged();
        }
    }
    
    private boolean positionIsInValid(int position) {
        if(getListAdapter().getCount() <= 0) {
            return true;
        }
        
        if(position == AdapterView.INVALID_POSITION) {
            return true;
        }
        
        return false;
    }
    
    private void clear() {
        if(positionIsInValid(0)) {
            return;
        }
        
        if(mDb.deleteAllLogs()) {
            mCursor.requery();
            mAdapter.notifyDataSetChanged();
        }
    }
} //class CallLogActivity
