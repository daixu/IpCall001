package com.sqt001.ipcall.provider;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

import com.sqt001.ipcall.application.AppPreference;

public class LoginRequest extends BaseRequest {

  private static final String KCLogin = "KCLogin";
  private String mLoginAccount = null;
  private String mLoginPassword = null;

  public LoginRequest(Context context,String loginAccount, String loginPassowrd) {
    super(context);
    mLoginAccount = loginAccount;
    mLoginPassword = loginPassowrd;
  }

  @Override
  protected void onCreateRequest(XmlSerializer serializer) {
    try {
      tryOnCreateRequest(serializer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void tryOnCreateRequest(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException,
      IOException {
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.TYPE, KCLogin);
    serializer.startTag(Constants.Xml.NULL, Constants.Xml.LOGIN);
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.NAME, mLoginAccount);
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.PASSWORD, mLoginPassword);
    serializer.endTag(Constants.Xml.NULL, Constants.Xml.LOGIN);
  }

  @Override
  protected void onParseResponse(int event, XmlPullParser parser) {
    if (Constants.Xml.NEWUSER.equals(parser.getName())) {
      String userIdStr = parser.getAttributeValue(null, Constants.Xml.UID);
      if (userIdStr != null && userIdStr.length() > 0)
        AppPreference.putUserId(userIdStr);
      String callerStr = parser.getAttributeValue(null, Constants.Xml.CALLER);
      if (callerStr != null && callerStr.length() > 0) 
        AppPreference.putMyNum(callerStr);
      String passwordStr = parser.getAttributeValue(null, Constants.Xml.PASSWORD);
      if(passwordStr != null && passwordStr.length() > 0) 
        AppPreference.putPassword(passwordStr);
    }
  }
}
