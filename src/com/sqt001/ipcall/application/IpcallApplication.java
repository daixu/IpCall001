package com.sqt001.ipcall.application;

import android.app.Application;

import com.sqt001.ipcall.provider.CallLogDbHelper;
import com.sqt001.ipcall.provider.QuickContactDbHelper;

public class IpcallApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        AppPreference.initialize(this);
        CallLogDbHelper.initialize(this);
        QuickContactDbHelper.initialize(this);
    }
    
    @Override
    public void onTerminate() {
    	CallLogDbHelper.terminate();
    	QuickContactDbHelper.terminate();
    	AppPreference.terminate();
    	
    	super.onTerminate();
    }
}
