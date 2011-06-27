package com.sqt001.ipcall.application;

public class BuildConfig {
    //open the debug
    private static final boolean mRelease = true;
    
    //if on emulator, give it a static imsi and imei.
    private static final boolean mDevice = true;
    
    //On some machine, there is no imsi.
    private static final boolean mTestNoneImsi = false;
    
    //Meizu close the contacts API for third-party app.
    private static final boolean mMeizuM9 = false;
    
    //On htc200, all imsi is:460030911111111
    private static final boolean mHtc200 = false;
    
    private static final String APP_VERSION_TEST = "1.8.0";
    private static final String APP_VERSION_RELEASE = "1.8.0";
    
//    private static final String API_POST_URL_TEST = "http://vma.newding.com:8081/dosql.aspx";
    private static final String API_POST_URL_TEST = "http://voip.sqt001.com/dosql.aspx";
    private static final String API_POST_URL_RELEASE = "http://voip.sqt001.com/dosql.aspx";
    
    private static final String APP_CHANNEL_TEST = "1000";
    private static final String APP_CHANNEL_RELEASE = "16";
    
    public static final String BROADCOSTRECIVER_XML_RESOVLE ="com.sqt001.ipcall.xml.resolve";

    public static boolean isRelease() {
        return mRelease;
    }
    
    public static boolean isDebug() {
        return !mRelease;
    }
    
    public static boolean isDevice() {
        return mDevice;
    }
    
    public static boolean isMonitor() {
        return !mDevice;
    }
    
    public static String getPostUrl() {
        if(isRelease()) {
            return API_POST_URL_RELEASE;
        } else {
            return API_POST_URL_TEST;
        }
    }
    
    public static String getAppVersion() {
        if(isRelease()) {
            return APP_VERSION_RELEASE;
        } else {
            return APP_VERSION_TEST;
        }
    }
    
    public static String getAppChannel() {
        if(isRelease()) {
            return APP_CHANNEL_RELEASE;
        } else {
            return APP_CHANNEL_TEST;
        }
    }
    
    public static boolean isForTestNoneImsi() {
        return mTestNoneImsi;
    }
    
    public static boolean isMeizuM9() {
        return mMeizuM9;
    }
    
    public static boolean isTestingHtc200() {
        return mHtc200;
    }
}