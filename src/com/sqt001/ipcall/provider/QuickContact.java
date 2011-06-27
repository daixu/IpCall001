package com.sqt001.ipcall.provider;

public class QuickContact {
	private String mName;
	private String mNumber;
	
	public QuickContact(String name, String number) {
		mName = name;
		mNumber = number;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getNumber() {
		return mNumber;
	}
}
