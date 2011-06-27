package com.sqt001.ipcall.provider;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

import com.sqt001.ipcall.application.AppPreference;

public class ModifyRequest extends BaseRequest {

  private static final String KCModify = "KCModify";
  private String mOldPassword = null;
  private String mNewPassword = null;

  public ModifyRequest(Context context,String oldPassword, String newPassword) {
    super(context);
    mOldPassword = oldPassword;
    mNewPassword = newPassword;
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
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.TYPE, KCModify);

    serializer.startTag(Constants.Xml.NULL, Constants.Xml.PASSWORD);
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.OLDPWD, mOldPassword);
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.NEWPWD, mNewPassword);
    serializer.endTag(Constants.Xml.NULL, Constants.Xml.PASSWORD);
  }

  @Override
  protected void onParseResponse(int event, XmlPullParser parser) {
    if (Constants.Xml.NEWUSER.equals(parser.getName())) {
      String userIdStr = parser.getAttributeValue(null, Constants.Xml.UID);
      String callerStr = parser.getAttributeValue(null, Constants.Xml.CALLER);
      String passwordStr = parser.getAttributeValue(null, Constants.Xml.PASSWORD);
      if (userIdStr != null && userIdStr.length() > 0)
        AppPreference.putUserId(userIdStr);
      if (callerStr != null && callerStr.length() > 0)
        AppPreference.putMyNum(callerStr);
      if(passwordStr != null && passwordStr.length() > 0) 
        AppPreference.putPassword(passwordStr);
    }
  }
}
