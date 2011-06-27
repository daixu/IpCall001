package com.sqt001.ipcall.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.application.AppPreference;
import com.sqt001.ipcall.application.BuildConfig;
import com.sqt001.ipcall.util.AppUtils;
import com.sqt001.ipcall.util.AppUtils.PInfo;
import com.sqt001.ipcall.util.HttpManager;
import com.sqt001.ipcall.util.LogUtil;
import com.sqt001.ipcall.util.NetworkFailException;

public abstract class BaseRequest {
  /*
   * Request status code
   */
  public static final int NORMAL_STATUS = 0;
  public static final int NEED_INPUT_NUM_STATUS = 1;
  public static final int ACCOUNT_EXCEPTION_STATUS = 2;
  public static final int OTHER_ERR_STATUS = 3;
  public static final int UPDATE_UPGRADE = 4;
  public static final int NETWORK_EXCEPTION = 100;

  /*
   * Base tags
   */
  private static final String TAG_RESULT = "result";
  private static final String TAG_REASON = "reason";
  private static final String TAG_ERRCODE = "errcode";
  private static final String TAG_USERID = "newuser";
  private static final String ATT_UID = "uid";
  private static final String TAG_PREFER = "message";
  private static final String TAG_ABOUT = "about";

  /*
   * status code
   */
  private int mResultCode = 0;
  private String mReason = null;

  private RequestListener mListener = null;
  
  private Context mContext;
  public BaseRequest(Context context) {
    mContext = context;
  }

  /**
   * Compose request, post to remote server; and get response, parse response;
   * set the result code and reason.
   * 
   * @param listener
   *          notify the process information.
   * @return true if success, false if network exception throwed.
   */
  public boolean post(RequestListener listener) {
    registerRequestListener(listener);
    boolean result = false;
    try {
      tryPost();
      result = true;
    } catch (NetworkFailException e) {
      setResult(NETWORK_EXCEPTION);
      setReason(mContext.getString(R.string.network_exception));
    } finally {
      unRegisterRequestListener();
    }
    return result;
  }

  private void tryPost() throws NetworkFailException {
    // create request
    String request = createRequest();
    Log.v("log", request);
    Log.v("request", request);
    // post and get reponse
    if (request == null || request.length() <= 0) {
      throw new IllegalStateException("createRequest fail");
    }
    
    String response = postAndGetResponse(request);
    Log.v("log", response);
    // parse response
    if (response == null || response.length() <= 0) {
      throw new IllegalStateException("postAndGetResponse fail");
    }
    LogUtil.w("--------yyyyyyyyyyy-----------");
    LogUtil.w("request" + request);
    LogUtil.w("--------1111111-----------");
    LogUtil.w("response" + response);
    parseResponse(response);
  }

  private String createRequest() {
    String request = null;

    try {
      request = tryCreateRequest();
    } catch (IOException e1) {
      throw new IllegalStateException();
    }

    return request;
  }

  private String tryCreateRequest() throws IllegalArgumentException, IllegalStateException, IOException {
    XmlSerializer serializer = Xml.newSerializer();
    StringWriter writer = new StringWriter();
    serializer.setOutput(writer);

    serializer.startDocument("utf-8", true);
    {
      serializer.startTag("", "newdingRequest");
      {
        serializer.attribute("", "version", AppPreference.getVersion());
        serializer.attribute("", "os", "android");

        serializer.startTag("", "userID");
        String id = AppPreference.getUserId();
        serializer.text(id);
        serializer.endTag("", "userID");

        serializer.startTag("", "userName");
        serializer.text(AppPreference.getAccount());
        serializer.endTag("", "userName");

        serializer.startTag("", "userMobileType");
        serializer.text(AppPreference.getPhoneModel());
        serializer.endTag("", "userMobileType");

        serializer.startTag("", "imei");
        serializer.text(AppPreference.getImei());
        serializer.endTag("", "imei");

        serializer.startTag("", "imsi");
//        serializer.text("RmKW-EHkUM6Cn-lx7aWnKC-vOfofQSI-pw7P");
//        serializer.text("460020239514289");
        String imsi = AppPreference.getImsi();
//        String imsi = "460020239514289";
        serializer.text(imsi);
        LogUtil.w("imsi: "+ imsi);
        serializer.endTag("", "imsi");

        serializer.startTag("", "channelID");
        serializer.text(AppPreference.getChannel());
        serializer.endTag("", "channelID");

        serializer.startTag("", "ScreenSize");
        serializer.attribute("", "x", AppPreference.getScreenWidth());
        serializer.attribute("", "y", AppPreference.getScreenHeight());
        serializer.endTag("", "ScreenSize");

        createSoftwareList(serializer);

        serializer.startTag("", "requestCommands");
        {
          serializer.startTag("", "requestCommand");
          {
            onCreateRequest(serializer);
          }
          serializer.endTag("", "requestCommand");
        }
        serializer.endTag("", "requestCommands");

      }
      serializer.endTag("", "newdingRequest");

    }
    serializer.endDocument();

    return writer.toString();
  }

  /*
   * Method for derived class to overwrite.
   */
  protected abstract void onCreateRequest(XmlSerializer serializer);

  private String postAndGetResponse(String input) throws NetworkFailException {
    return HttpManager.postAndGetResponse(input);
  }

  private void createSoftwareList(XmlSerializer serializer) {
    if (!AppPreference.isNeedPostSoftList()) {
      return;
    }

    ArrayList<PInfo> appAry = AppUtils.getInstalledApps(AppPreference.getContext());
    if (appAry.isEmpty()) {
      return;
    }

    AppPreference.markNextMonthForSoftListPost();

    try {
      serializer.startTag("", "SoftwareList");
      for (PInfo p : appAry) {
        serializer.startTag("", "soft");
        serializer.attribute("", "name", p.getName());
        serializer.attribute("", "id", "");
        serializer.endTag("", "soft");
      }
      serializer.endTag("", "SoftwareList");
    } catch (IllegalArgumentException e) {
      // do nothing.
    } catch (IllegalStateException e) {
      // do nothing.
    } catch (IOException e) {
      // do nothing.
    }
  }

  private void parseResponse(String input) {
    try {
      tryParseResponse(input);
    } catch (IOException e) {
      setResult(NETWORK_EXCEPTION);
      setReason("Network Fail");
      // throw new RuntimeException(e);
    } catch (XmlPullParserException e) {
      setResult(NETWORK_EXCEPTION);
      setReason("Network Fail");
      // throw new RuntimeException(e);
    }
  }

  private void tryParseResponse(String input) throws IOException, XmlPullParserException {

    InputStream ins = new ByteArrayInputStream(input.getBytes());
    final XmlPullParser parser = Xml.newPullParser();
    parser.setInput(new InputStreamReader(ins));

    int event = parser.getEventType();
    boolean stopParse = false;
    while (event != XmlPullParser.END_DOCUMENT && !stopParse) {
      switch (event) {
      case XmlPullParser.START_DOCUMENT: {
        break;
      }

      case XmlPullParser.START_TAG: {
        String parserName = parser.getName();
        if (TAG_RESULT.equals(parserName)) {
          String errcodeStr = parser.getAttributeValue(null, TAG_ERRCODE);
          if (errcodeStr != null) {
            setResult(Integer.parseInt(errcodeStr));
            int x = getResuleCode();
            System.out.println(x);
          } else {
            setResult(-1);
          }
        } else if (TAG_REASON.equals(parserName)) {
          setReason(parser.nextText());
          if (getResuleCode() != 0 && getResuleCode() != 4) {
            stopParse = true;
          }
        } else if (TAG_USERID.equals(parserName)) {
          String userId = parser.getAttributeValue(null, ATT_UID);
          if(userId != null && userId.length() > 0)
            setUserId(userId);
          String callerStr = parser.getAttributeValue(null, Constants.Xml.CALLER);
          if((callerStr != null) && (callerStr.length() > 0) && (!callerStr.equals(""))){
            AppPreference.putAccount(callerStr);
          }
          String passwordStr = parser.getAttributeValue(null, Constants.Xml.PASSWORD);
          if(passwordStr != null && passwordStr.length() > 0)
            AppPreference.putPassword(passwordStr);
        } else if (TAG_PREFER.equals(parserName) && parser.getDepth() == 4) {
          String message = parser.nextText();
          if (message != null && message.length() > 0) {
            AppPreference.putMessage(message);
          }
          setPrefer(message);
        } else if(TAG_ABOUT.equals(parserName)){
          String about = parser.nextText();
          System.out.println(about);
          if (about != null && about.length() > 0) {
            AppPreference.putAbout(about);
          }
        }else {
          onParseResponse(event, parser);
        }
        break;
      }

      case XmlPullParser.END_TAG: {
        break;
      }
      }

      event = parser.next();
    }
    Intent intent = new Intent(BuildConfig.BROADCOSTRECIVER_XML_RESOVLE);
    mContext.sendBroadcast(intent);
  }

  /*
   * Method for derived class to overwrite. return true if continue parse
   */
  protected abstract void onParseResponse(int event, XmlPullParser name);

  private void setResult(int code) {
    mResultCode = code;
  }

  private void setReason(String reason) {
    mReason = reason;
  }

  private void setUserId(String userId) {
    if (userId != null && userId.length() > 0) {
      AppPreference.putUserId(userId);
    }
  }

  public int getResuleCode() {
    return mResultCode;
  }

  public String getReason() {
    return mReason;
  }

  private void setPrefer(String prefer) {
    AppPreference.putMoneySaveSecret(prefer);
  }

  private void registerRequestListener(RequestListener listener) {
    unRegisterRequestListener();
    mListener = listener;
  }

  private void unRegisterRequestListener() {
    mListener = null;
  }

  protected void notifyListener(String info) {
    if (mListener != null) {
      mListener.onRequestProcessInfo(info);
    }
  }

  /*
   * interface RequestListener
   */
  public static interface RequestListener {
    public void onRequestProcessInfo(String info);
  }
}
