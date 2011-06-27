package com.sqt001.ipcall.provider;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

public class GetPasswordRequest extends BaseRequest {
  
  private static final String KCGetPassword = "KCGetPassword";
  private String mCaller = null;
  
  public GetPasswordRequest(Context context,String caller){
    super(context);
    mCaller = caller;
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
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.TYPE, KCGetPassword);
    
    serializer.startTag(Constants.Xml.NULL, Constants.Xml.CALLER);
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.NUMBER, mCaller);
    serializer.endTag(Constants.Xml.NULL, Constants.Xml.CALLER);
  }

  @Override
  protected void onParseResponse(int event, XmlPullParser name) {
  }

}
