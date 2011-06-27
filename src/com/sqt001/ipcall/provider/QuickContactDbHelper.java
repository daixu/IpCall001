package com.sqt001.ipcall.provider;

import android.content.Context;

public class QuickContactDbHelper {
	static QuickContactDbAdapter mContacts;
	
	static public void  initialize(Context context) {
		mContacts = new QuickContactDbAdapter(context);
		mContacts = mContacts.open();
	}
	
	static public void terminate() {
		mContacts.close();
	}
	
	static public QuickContactDbAdapter getDb() {
		return mContacts;
	}
}
