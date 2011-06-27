package com.sqt001.ipcall.provider;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

public class ChargeRequest extends BaseRequest {
	private static final String KCRECHARGE = "KCRecharge";
	private String mSerialNumber = null;
	private String mPwdNumber = null;
	private String mPhoneNumber = null;
	
	public ChargeRequest(Context context,String serial, String pwd, String phone) {
	  super(context);
		mSerialNumber = serial;
		mPwdNumber = pwd;
		mPhoneNumber = phone;
	}

	@Override
	protected void onCreateRequest(XmlSerializer serializer) {
		try {
		    tryOnCreateRequest(serializer);
		} catch (IOException e) {
			throw new RuntimeException(e);   
		}  
	}
	
	private void tryOnCreateRequest(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.attribute(Constants.Xml.NULL, Constants.Xml.TYPE, KCRECHARGE);

        serializer.startTag(Constants.Xml.NULL, Constants.Xml.CALLER); 
        serializer.attribute(Constants.Xml.NULL, Constants.Xml.NUMBER, mPhoneNumber);
        serializer.endTag(Constants.Xml.NULL, Constants.Xml.CALLER); 
        
        serializer.startTag(Constants.Xml.NULL, Constants.Xml.CARDINFO);  
        serializer.attribute(Constants.Xml.NULL, Constants.Xml.NUMBER, mSerialNumber);
        serializer.attribute(Constants.Xml.NULL, Constants.Xml.PASSWD, mPwdNumber);
        serializer.attribute(Constants.Xml.NULL, Constants.Xml.MONEY, "0");
        serializer.endTag(Constants.Xml.NULL, Constants.Xml.CARDINFO);  
	}

	@Override
	protected void onParseResponse(int event, XmlPullParser parser) {
	    if(Constants.Xml.ORDERID.equals(parser.getName())) {
	        //save order id
	    }
	}
}
