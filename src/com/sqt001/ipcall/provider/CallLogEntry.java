package com.sqt001.ipcall.provider;

public class CallLogEntry {
	private String mName;
	private String mNumber;
	private String mTime;
	
	public CallLogEntry(String name, String number, String time) {
		mName = name;
		mNumber = number;
		mTime = time;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getNumber() {
		return mNumber;
	}
	
	public String getTime() {
		return mTime;
	}
}
