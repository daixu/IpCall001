package com.sqt001.ipcall.provider;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

import com.sqt001.ipcall.application.AppPreference;

public class GetSmsRequest extends BaseRequest{

  private static final String KCGetSMS = "KCGetSMS";
  
  public GetSmsRequest(Context context ){
    super(context);
  }
  
  @Override
  protected void onCreateRequest(XmlSerializer serializer) {
    try {
      tryOnCreateRequest(serializer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void tryOnCreateRequest(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException,IOException {
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.TYPE, KCGetSMS);
  }

  @Override
  protected void onParseResponse(int event, XmlPullParser parser) {
    try {
      accessNumber(parser);
    } catch (XmlPullParserException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void accessNumber(XmlPullParser parser) throws XmlPullParserException, IOException {
    if (Constants.Xml.SMS.equals(parser.getName())) {
      String gatewayNumberStr = parser.nextText();
      if (gatewayNumberStr != null && gatewayNumberStr.length() > 0) {
        AppPreference.putGatewayNumber(gatewayNumberStr);
      }
    }
  }
}
