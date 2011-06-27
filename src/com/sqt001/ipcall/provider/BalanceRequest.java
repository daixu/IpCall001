package com.sqt001.ipcall.provider;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.util.DBUtil;
import com.sqt001.ipcall.util.SoftObj;

public class BalanceRequest extends BaseRequest {
  private static final String KCGETBALANCE = "KCGetBalance";
  private static final String TAG_TEXT = "text";
  private static final String TAG_URL = "url";
  private Context mCtx;
  public BalanceRequest(Context context) {
    super(context);
    mCtx = context;
    
  }

  @Override
  protected void onCreateRequest(XmlSerializer serializer) {
    try {
      tryOnCreateRequest(serializer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void tryOnCreateRequest(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException,
      IOException {
    serializer.attribute(Constants.Xml.NULL, Constants.Xml.TYPE, KCGETBALANCE);
  }

  @Override
  protected void onParseResponse(int event, XmlPullParser parser) {
//    parseNewVersion(parser);
    parseBalance(parser);
    try {
      parseAccountControl(parser);
      parseNewOldAccount(parser);
      parseUpgrade(parser);
      parseSoftwareList(parser);
    } catch (XmlPullParserException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void parseNewVersion(XmlPullParser parser) {
    parseMessage(parser);
    parseUrl(parser);
  }

  private void parseMessage(XmlPullParser parser) {
    if (Constants.Xml.TEXT.equals(parser.getName())) {
      try {
        setMessage(parser.nextText());
      } catch (XmlPullParserException e) {
        // do nothing.
      } catch (IOException e) {
        // do nothing.
      }
    }
  }

  private void setMessage(String msg) {
    if (msg == null || msg.length() == 0) {
      return;
    }
    AppPreference.putNewVersionMsg(msg);
  }

  private void parseUrl(XmlPullParser parser) {
    if (Constants.Xml.URL.equals(parser.getName())) {
      try {
        setUrl(parser.nextText());
      } catch (XmlPullParserException e) {
        // do nothing.
      } catch (IOException e) {
        // do nothing.
      }
    }
  }

  private void setUrl(String url) {
    if (url == null || url.length() == 0) {
      return;
    }
    if (!url.contains(Constants.Other.HTTP_HEAD)) {
      url = Constants.Other.HTTP_HEAD + url;
    }
    AppPreference.putNewVersionUrl(url);
  }

  private void parseNewOldAccount(XmlPullParser parser) throws XmlPullParserException, IOException {
    if (Constants.Xml.ISNEWUSER.equals(parser.getName())) {
      String isnewuserStr = parser.nextText();
      if (isnewuserStr != null) {
        int isnewuserVal = 0;
        try {
          isnewuserVal = (int) Float.parseFloat(isnewuserStr);
        } catch (NumberFormatException e) {
          isnewuserVal = 0;
        }
        AppPreference.putIsnewuser(isnewuserVal);
      }
    }
  }
  
  private void parseAccountControl(XmlPullParser parser) throws XmlPullParserException, IOException {
    if (Constants.Xml.CONTROL.equals(parser.getName())) {
      String controlStr = parser.nextText();
      if (controlStr != null) {
          int controlVal = 0;
          try {
            controlVal = (int) Float.parseFloat(controlStr);
          } catch (NumberFormatException e) {
            controlVal = 0;
          }
          AppPreference.putControl(controlVal);
      }
    }
  }
 
  private void parseSoftwareList(XmlPullParser parser) throws XmlPullParserException, IOException {
    DBUtil dbUtil = new DBUtil(mCtx);
    String name = parser.getName();
    if (Constants.Xml.SOFTWARELIST.equals(name)) {
      dbUtil.delAll();
      int i = parser.nextTag();
      while (i != XmlPullParser.END_TAG) {
        switch (i) {
        case XmlPullParser.START_TAG:
          String id = parser.getAttributeValue(null, Constants.Xml.ID);
          String title = parser.getAttributeValue(null, Constants.Xml.TITLE);
          String message = parser.getAttributeValue(null, Constants.Xml.MESSAGE);
          String url = parser.getAttributeValue(null, Constants.Xml.URL);
          SoftObj obj = new SoftObj(id, title, message, url);
          dbUtil.insertSubject(obj);
          System.out.println(id + title + message + url);
          parser.next();
        }
        i = parser.next();
      }
    }
  }
  
  private void parseUpgrade(XmlPullParser parser) throws XmlPullParserException, IOException{
    String name= parser.getName();
    int depth = parser.getDepth();
    System.out.println(depth);
    if (Constants.Xml.MESSAGE.equals(name) && parser.getDepth() == 2) {
      int i = parser.nextTag();
      depth = parser.getDepth();
      while (i != XmlPullParser.END_TAG ) {
        switch (i) {
        case XmlPullParser.START_TAG:
          if (TAG_URL.equals(parser.getName())) {
            String url = parser.nextText();
            if (!(url.equals(""))&&(url!=null)) {
              setUrl(url);
            }
          } if (TAG_TEXT.equals(parser.getName())) {
            String text = parser.nextText();
            if (!(text.equals(""))&&(text!=null)) {
              setMessage(text);
            }
          }
          parser.next();
          break;
        }
        i = parser.next();
        System.out.println(i);
      }
    }
  }

  private void parseBalance(XmlPullParser parser) {
    if (Constants.Xml.USERFEE.equals(parser.getName())) {
      String balanceStr = parser.getAttributeValue(null, Constants.Xml.BALANCE);
      if (balanceStr != null) {
        int balanceVal = 0;
        try {
          balanceVal = (int) Float.parseFloat(balanceStr);
        } catch (NumberFormatException e) {
          balanceVal = 0;
        }
        if (balanceVal < 0) {
          balanceVal = 0;
        }
        AppPreference.putUserBalance(balanceVal);
      }
    }
  }

}
