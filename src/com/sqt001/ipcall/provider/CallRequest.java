package com.sqt001.ipcall.provider;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

import com.sqt001.ipcall.application.AppPreference;

public class CallRequest extends BaseRequest {
  private static final String KCMAKECALL = "KCMakeCall";
  private String mCalledNumber = null;
  private String mCallerNumber = null;
	
  public CallRequest(Context context,String calledNumber, String callerNumber) {
    super(context);
    mCalledNumber = calledNumber;
    mCallerNumber = callerNumber;
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
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.TYPE, KCMAKECALL);

    serializer.startTag(Constants.Xml.NULL, Constants.Xml.CALLER);  
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.NUMBER, mCallerNumber);  
    serializer.endTag(Constants.Xml.NULL, Constants.Xml.CALLER);  
        
    serializer.startTag(Constants.Xml.NULL, Constants.Xml.CALLED);  
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.NUMBER, mCalledNumber);  
    serializer.endTag(Constants.Xml.NULL, Constants.Xml.CALLED);  
  }

  @Override
  protected void onParseResponse(int event, XmlPullParser parser) {
      try {
        parseAboutInfo(parser);
    } catch (XmlPullParserException e) {
        // do nothing
    } catch (IOException e) {
        // do nothing
    }
  }
  
  private void parseAboutInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
    if(Constants.Xml.ABOUT.equals(parser.getName())) {
      String aboutStr = parser.nextText();
      if(aboutStr != null && aboutStr.length() > 1) {
        AppPreference.putAbout(aboutStr);
      }
    }
    else if(Constants.Xml.SERVICENUM.equals(parser.getName())) {
      String serviceNumStr = parser.nextText();
      if(serviceNumStr != null && serviceNumStr.length() > 1) {
        AppPreference.putServiceCallNum(serviceNumStr);
      }
    }
  } //parseAboutInfo
  
}
