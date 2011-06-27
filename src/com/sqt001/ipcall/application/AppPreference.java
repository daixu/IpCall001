package com.sqt001.ipcall.application;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.DisplayMetrics;

import com.sqt001.ipcall.contact.Account;
import com.sqt001.ipcall.provider.Constants;
import com.sqt001.ipcall.util.FileUtils;
import com.sqt001.ipcall.util.GuidGenerator;
import com.sqt001.ipcall.util.LogUtil;
import com.sqt001.ipcall.util.Tuple;

public final class AppPreference {
  private static final String PREFER_NAME = "AppPreference";
	
  private static final String MODEL_SDK_KEY = "model";
  private static final String MODEL_SDK_DEFAULT = "Unknow/Unknow";
	
  public static final int IMSI_MININUM_LENGTH = 15;
  public static final int IMSI_MAXMIUM_LENGTH = 36;
  public static final String IMSI_DEFAULT = "000000000000000";
  private static final int IMSI_LENGTH = 15;
  private static final String IMSI_KEY = "imsi";
	
  private static final String IMEI_KEY = "imei";
  private static final String IMEI_DEFAULT = "000000000000000";
		
  private static final String MYNUM_KEY = "mynum";
  private static final String MYNUM_DEFAULT="";
  
  private static final String PASSWORD_KEY ="password";
  private static final String PASSWORD_DEFAULT ="";
	
  private static final String USERNAME_KEY = "username";
  private static final String USERNAME_DEFAULT = "";
	
  private static final String SCREEN_WIDTH_KEY = "screen_x";
  private static final String SCREEN_WIDTH_DEFAULT = "320";
	
  private static final String SCREEN_HEIGHT_KEY = "screen_y";
  private static final String SCREEN_HEIGHT_DEFAULT = "480";
	
  private static final String USER_BALANCE_KEY = "balance";
  private static final int USER_BALANCE_DEFAULT = 0;
	
  private static final String USER_ID = "userid";
  private static final String USER_ID_DEFAULT = "";
  
  private static final String NEW_VERSION_HINT_COUNT = "new_version_count";
  private static final int NEW_VERSION_HINT_COUNT_DEFAULT = 0;
  private static final int NEW_VERSION_HINT_COUNT_MAX = 2;
  private static final String NEW_VERSION_MSG = "new_version_msg";
  private static final String NEW_VERSION_URL = "new_version_url";
  private static final String VERSION_CODE_BAK = "version_code";
  private static final int VERSION_CODE_DEFAULT = 1;
  private static boolean mNeedShowNewVersion = false;
	
  private static final String SOFT_LIST_UPDATE_TIME = "soft_list_time";
  private static final int SOFT_LIST_UPDATE_TIME_DEFAULT = -1;
  private static boolean mNeedPostSoftList = false;
	
  private static final String MONEY_SAVE_SECRET = "money_save_secret";
  private static final String MONEY_SAVE_SECRET_DEFAULT  = "";
	
  private static final String ABOUT_INFO_KEY = "about_info";
  private static final String ABOUT_INFO_DEFAULT = "";
  
  private static final String SERVICE_NUM_KEY = "service_num";
  private static final String SERVICE_NUM_DEFAULT = "075561363001";
  
  private static final String GATEWAY_NUMBER_KEY = "gatewayNumber";
  private static final String GATEWAY_NUMBER_DEFAULT = "13800138000";

  private static final String ISNEWUSER_KEY = "isnewuser";
  private static final int ISNEWUSER_KEY_DEFAULT = 0;
  
  private static final String MESSAGE_KEY = "message";
  private static final String MESSAGE_KEY_DEFAULT = "";
  
  private static final String CONTROL_KEY = "control";
  private static final int CONTROL_KEY_DEFAULT = 0;

  private static final String TEXT_KEY = "text";
  private static final String TEXT_KEY_DEFAULT = "";

  private static final String URL_KEY = "url";
  private static final String URL_KEY_DEFAULT = "";
  
  private static final String FIRST_BOOT_KEY = "first_boot";
  private static final boolean FIRST_BOOT_DEFAULT = true;
  
  private static SharedPreferences mPrefer;
  
  private static boolean mIsEclairOrLater = false;
  private static boolean mIsGingerbreadOrLater = false;
  
  private static Context context;
  
  private static final String FORCE_UPDATE_FLAG_KEY = "force_update_flag";
  private static final boolean FORCE_UPDATE_DEFAULT_VALUE = false;
  
  public static void  initialize(Context context) {
    AppPreference.context = context;
    
    mPrefer = context.getSharedPreferences(PREFER_NAME, Context.MODE_WORLD_WRITEABLE);
    SharedPreferences.Editor editor = mPrefer.edit();
    initModel(editor);
    initImeiImsi(editor);
    initScreenSize(editor);
    markFirstBootFinished(editor);
    editor.commit();

    validateImsi();
    
    initSdkVersion();
    initNewVersionHint();
    checkNeedPostSoftList();
  }
	
  public static void terminate() {
    context = null;
    mPrefer = null;
  }
  
  
	
  public static Context getContext() {
    return context;
  }
		
  private static void initModel(SharedPreferences.Editor editor) {
    //get model and sdk version
    String model = Build.MODEL + "/" + Build.VERSION.RELEASE;
    editor.putString(MODEL_SDK_KEY, model);
  }
	
  private static boolean initImeiImsi(SharedPreferences.Editor editor) {
    //get imei
    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    String imei =tm.getDeviceId();
    LogUtil.imsi("imei:" + imei + "\n");
    editor.putString(IMEI_KEY, imei);
        
    //get imsi
    String newImsi =tm.getSubscriberId();  
    if(BuildConfig.isForTestNoneImsi()) {
        newImsi = "";
    }
    if(newImsi == null || newImsi.length() != IMSI_LENGTH) {
        newImsi = "";
    }
    LogUtil.imsi("imsi:" + newImsi + "\n");
        
    //saved imsi
    String savedImsi = mPrefer.getString(IMSI_KEY, null);
    if(savedImsi != null) {
        LogUtil.imsi("oldImsi:" + savedImsi + "\n");
    }
         
    //for test only.
    if(BuildConfig.isTestingHtc200()) {
        newImsi = Constants.Other.HTC200_IMSI;
    }
    
    //check if is changed.
    boolean imsiChanged = false;
    
    if(!newImsi.equals(savedImsi)) {
        editor.putString(IMSI_KEY, newImsi);
        imsiChanged = true;
    }
    
    return imsiChanged;
  }

  private static void resetUserNumAndUserId(SharedPreferences.Editor editor) {
    editor.putString(MYNUM_KEY, MYNUM_DEFAULT);
    editor.putString(USER_ID, USER_ID_DEFAULT);
  }
    
  private static void validateImsi() {
    if(!Account.isActive()) {
        useUuidAsImsi();
    }
  }
  
  private static void useUuidAsImsi() {
    Tuple<Boolean> rw = getExternalStorageState();
      
    if(rw.get(0)) {
      if(readBackupGuid()) {
        return;
      }
    }
  }
  
  private static Tuple<Boolean> getExternalStorageState() {
    boolean readable = false;
    boolean writable = false;
    String state = Environment.getExternalStorageState();    
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      readable = writable = true;
    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {   
      readable = true;       
      writable = false;
    } else {
      readable = writable = false;    
    }
      
    return new Tuple<Boolean>(readable, writable);
  }

  private final static String IPCALL_DATA_GUID_PATH = Environment.getExternalStorageDirectory()
      + "/ndipcall-data-user-guid";
  
  private static boolean readBackupGuid() {
    File temp = new File(IPCALL_DATA_GUID_PATH);
    if(!temp.exists()) {
      return false;
    }

    String guid;
    try {
      guid = FileUtils.readFileToString(temp);
      if(guid != null && guid.length() > 0) {
        putString(IMSI_KEY, guid);
        return true;
      }
    } catch (IOException e) {
      //ingore and return false
    }

    return false;
  }
  
  public static String generateGuidAsAccount() throws IOException {
    File temp = new File(IPCALL_DATA_GUID_PATH);
    if(!temp.exists()) {
      temp.createNewFile();
    }
    
    String guid = GuidGenerator.generate();
    FileUtils.writeStringToFile(temp, guid);
    putString(IMSI_KEY, guid);
    LogUtil.w("guid: " + guid + "\n");
    
    return guid;
  }
  
  private static void initScreenSize(SharedPreferences.Editor editor) {   
    //get screen size
    DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
    int screenWidth = dm.widthPixels;
    int screenHeight = dm.heightPixels;
    editor.putString(SCREEN_WIDTH_KEY, String.valueOf(screenWidth));    
    editor.putString(SCREEN_HEIGHT_KEY, String.valueOf(screenHeight));     
  }

  private static void initSdkVersion() {
    //get sdk version
    boolean newVersion = false;
    boolean moreNewVersion = false;
    try {
      int v = Integer.parseInt(Build.VERSION.SDK);
      if(v >= 5) {
        newVersion = true;
      }
      if(v >= 9) {
        moreNewVersion = true;
      }
    } catch (NumberFormatException e) {
      newVersion = false;
    }
    mIsEclairOrLater = newVersion; 
    mIsGingerbreadOrLater = moreNewVersion;
    
    LogUtil.m9(mIsEclairOrLater ? "2" : "1");
    if(BuildConfig.isMeizuM9()) {
        mIsEclairOrLater = true;
    }
  }

  private static void initNewVersionHint() {    
    //check update
    int realVersion = getApplicationVersionCode(getContext());
    int bakVersion = getInt(VERSION_CODE_BAK, VERSION_CODE_DEFAULT); 
    if(realVersion > bakVersion) {
      putInt(VERSION_CODE_BAK, realVersion);
      putNewVersionUrl("");
      putInt(NEW_VERSION_HINT_COUNT, NEW_VERSION_HINT_COUNT_DEFAULT);
      mNeedShowNewVersion = false;
      return;
    }
            
    //check need show new version
    int ncount = getInt(NEW_VERSION_HINT_COUNT, NEW_VERSION_HINT_COUNT_DEFAULT);
    if(ncount < NEW_VERSION_HINT_COUNT_MAX) {
      String url = getNewVersionUrl();
      if(url.length() > 10) {
        mNeedShowNewVersion = true;
      }
    }
  }
    
  public static int getApplicationVersionCode(Context context) {
    int result = VERSION_CODE_DEFAULT;
    try {
      PackageManager manager = context.getPackageManager();
      PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
      result = info.versionCode;
    } catch (NameNotFoundException e) {
      result = VERSION_CODE_DEFAULT;
    }

    return result;
  }

  private static void checkNeedPostSoftList() {
    Time time = new Time();
    time.setToNow();
    int nm = time.month;
    int om = getInt(SOFT_LIST_UPDATE_TIME, SOFT_LIST_UPDATE_TIME_DEFAULT);
    if(nm != om) {
      mNeedPostSoftList = true;
    }
  }
    
  public static void markNextMonthForSoftListPost() {
    Time time = new Time();
    time.setToNow();
    int nm = time.month;
    putInt(SOFT_LIST_UPDATE_TIME, nm);
  }
  
  private static void markFirstBootFinished(SharedPreferences.Editor editor) {
      editor.putBoolean(FIRST_BOOT_KEY, false);
  }
  
  public static boolean isFirstBoot() {
      return mPrefer.getBoolean(FIRST_BOOT_KEY, FIRST_BOOT_DEFAULT);
  }
    
  public static boolean isNeedPostSoftList() {
    return mNeedPostSoftList;
  }
    
  public static String getPhoneModel() {
    return getString(MODEL_SDK_KEY, MODEL_SDK_DEFAULT);
  }
	
  public static String getImei() {
      if(BuildConfig.isDevice()) {
          return getString(IMEI_KEY, IMEI_DEFAULT);
      } else {
          return "359807013828020";
      }
  }
  
  public static void putImsi(String imsi){
    putString(IMSI_KEY, imsi);
  }
	
  public static String getImsi() {
      if(BuildConfig.isDevice() || BuildConfig.isForTestNoneImsi()) {
          return getString(IMSI_KEY, IMSI_DEFAULT);
      } else {
          return "460005212160944";
      }
//      String imsi = getString(IMSI_KEY, IMSI_DEFAULT);
//      if(imsi.equals("")) {
//        String im = GuidGenerator.generate();
//        AppPreference.putImsi(im);
//        return im;
//      } else {
//        return imsi;
//      }
  }
  
  public static String getMyNum() {
    return getString(MYNUM_KEY, MYNUM_DEFAULT);
  }
	
  public static void putMyNum(String num) {
    putString(MYNUM_KEY, num);
  }
  
  public static void putPassword(String password){
    putString(PASSWORD_KEY,password);
  }
  
  public static String getPassword(){
    return getString(PASSWORD_KEY,PASSWORD_DEFAULT);
  }
  
  public static void putAccount(String username){
    putString(USERNAME_KEY, username);
  }
	
  public static String getAccount() {
    return getString(USERNAME_KEY, USERNAME_DEFAULT);
  }
	
  public static String getChannel() {
    return BuildConfig.getAppChannel();
  }
	
  public static String getScreenWidth() {
    return getString(SCREEN_WIDTH_KEY, SCREEN_WIDTH_DEFAULT);
  }
	
  public static String getScreenHeight() {
    return getString(SCREEN_HEIGHT_KEY, SCREEN_HEIGHT_DEFAULT);
  }
	
  public static int getUserBalance() {
    return getInt(USER_BALANCE_KEY, USER_BALANCE_DEFAULT);
  }
	
  public static void putUserBalance(int value) {
    putInt(USER_BALANCE_KEY, value);
  }
  
  public static String getUserId() {
    return getString(USER_ID, USER_ID_DEFAULT);
  }
	
  public static void putUserId(String userId) {
    putString(USER_ID, userId);
  }
	
  public static String getMoneySaveSecret() {
    return getString(MONEY_SAVE_SECRET, MONEY_SAVE_SECRET_DEFAULT);
  }
	
  public static void putMoneySaveSecret(String secret) {
    putString(MONEY_SAVE_SECRET, secret);
  }
  
  public static String getAbout() {
    return getString(ABOUT_INFO_KEY, ABOUT_INFO_DEFAULT);
  }
  
  public static void putAbout(String about) {
    putString(ABOUT_INFO_KEY, about);
  }
  
  public static String getServiceCallNum() {
    return getString(SERVICE_NUM_KEY, SERVICE_NUM_DEFAULT);
  }

  public static void putServiceCallNum(String num) {
    putString(SERVICE_NUM_KEY, num);
  }
  
  public static String getGatewayNumber(){
    return getString(GATEWAY_NUMBER_KEY,GATEWAY_NUMBER_DEFAULT);
  }
  
  public static void putGatewayNumber(String number){
    putString(GATEWAY_NUMBER_KEY , number);
  }
  
  public static void putMessage(String message) {
    putString(MESSAGE_KEY, message);
  }
  
  public static String getMessage(){
    return getString(MESSAGE_KEY, MESSAGE_KEY_DEFAULT);
  }

  public static int getIsnewuser(){
    return getInt(ISNEWUSER_KEY,ISNEWUSER_KEY_DEFAULT);
  }
  
  public static void putIsnewuser(int number){
    putInt(ISNEWUSER_KEY, number);
  }
  
  public static int getControl(){
    return getInt(CONTROL_KEY,CONTROL_KEY_DEFAULT);
  }
  
  public static void putControl(int number){
    putInt(CONTROL_KEY, number);
  }

  public static String getUpdateText(){
    return getString(TEXT_KEY,TEXT_KEY_DEFAULT);
  }
  
  public static void putUpdateText(String text){
    putString(TEXT_KEY, text);
  }

  public static String getUpdateUrl(){
    return getString(URL_KEY,URL_KEY_DEFAULT);
  }
  
  public static void putUpdateUrl(String url){
    putString(URL_KEY, url);
  }
		
  private static String getString(String key, String defValue) {
    return mPrefer.getString(key, defValue);
  }
	
  private static int getInt(String key, int defValue) {
    return mPrefer.getInt(key, defValue);
  }
  
  private static boolean getBoolean(String key, boolean defValue) {
      return mPrefer.getBoolean(key, defValue);
  }
	
  private static void putString(String key, String value) {
    SharedPreferences.Editor editor = mPrefer.edit();
    editor.putString(key, value);
    editor.commit();
  }
	
  private static void putInt(String key, int value) {
    SharedPreferences.Editor editor = mPrefer.edit();
    editor.putInt(key, value);
    editor.commit();
  }
  
  private static void putBoolean(String key, boolean value) {
      SharedPreferences.Editor editor = mPrefer.edit();
      editor.putBoolean(key, value);
      editor.commit();
  }
    
  public static boolean isEclairOrLater() {
    return mIsEclairOrLater;
  }
	
  public static boolean isGingerbreadOrLater() {
    return mIsGingerbreadOrLater;
  }
  
  public static void putNewVersionMsg(String msg) {
    putString(NEW_VERSION_MSG, msg);
  }
	
  public static String getNewVersionMsg() {
    return getString(NEW_VERSION_MSG, "");
  }
	
  public static void putNewVersionUrl(String url) {
    putString(NEW_VERSION_URL, url);
  }
	
  public static String getNewVersionUrl() {
    return getString(NEW_VERSION_URL, "");
  }
	
  public static boolean checkNeedShowNewVersionHint() {
    return mNeedShowNewVersion;
  }
	
  public static void markNeedNotShowNewVersionHint() {
    int ncount = getInt(NEW_VERSION_HINT_COUNT, NEW_VERSION_HINT_COUNT_DEFAULT);
    ncount += 1;
    putInt(NEW_VERSION_HINT_COUNT, ncount);
    mNeedShowNewVersion = false;
  }

  public static String getVersion() {
      return BuildConfig.getAppVersion();
  }
	
  public static String getPostUrl() {
      return BuildConfig.getPostUrl();
  }
  
  public static boolean isImsiActive() {
      return getImsi().length() == IMSI_MININUM_LENGTH;
  }
  
  public static void markForceUpdate(boolean update) {
    putBoolean(FORCE_UPDATE_FLAG_KEY, update);
  }
  
  public static final boolean isForceUpdateMarked(){
    return getBoolean(FORCE_UPDATE_FLAG_KEY, FORCE_UPDATE_DEFAULT_VALUE);
  }
  
  private AppPreference() {	
  }

//  public static boolean checkRemindVersionHint() {
//    
//    return null==getString(TEXT_KEY,TEXT_KEY_DEFAULT)||"".equals(getString(TEXT_KEY,TEXT_KEY_DEFAULT));
//  }

}
