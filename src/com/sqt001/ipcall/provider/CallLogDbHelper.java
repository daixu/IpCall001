package com.sqt001.ipcall.provider;

import android.content.Context;

public class CallLogDbHelper {
	static CallLogDbAdapter mCallLog;
	
	static public void  initialize(Context context) {
		mCallLog = new CallLogDbAdapter(context);
		mCallLog = mCallLog.open();
	}
	
	static public void terminate() {
		mCallLog.close();
	}
	
	static public CallLogDbAdapter getDb() {
		return mCallLog;
	}
}
