package com.sqt001.ipcall.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.SystemClock;

import com.sqt001.ipcall.application.AppPreference;

/*
 * Encode the request,
 * post it,
 * get response,
 * decode response.
 */
public class HttpManager {
    private static final DefaultHttpClient sClient;
    private static boolean mApplyApn = true;
    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    
    static {
        final HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");

        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setConnectionTimeout(params, 60 * 1000);
        HttpConnectionParams.setSoTimeout(params, 70 * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        HttpProtocolParams.setUserAgent(params, "Xima Software HTTP Client 1.0");

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);

        sClient = new DefaultHttpClient(manager, params);
    }

    public static boolean checkNet(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo netWrokInfo = manager.getActiveNetworkInfo();  
        if (netWrokInfo == null || !netWrokInfo.isAvailable()) { 
            return false;
        }
        return true;
    }


    /*
     * We should not create any instance of HttpManager.
     */
    private HttpManager() { 
    }

    public static String postAndGetResponse(String input) throws NetworkFailException {
        //Check arguments
        if(input == null || input.length() <= 0) {
            throw new IllegalArgumentException();
        }

//        LogUtil.clear();
        LogUtil.w("CMD");
        LogUtil.w("------------------------------\n");

        LogUtil.w("Post: ");
        LogUtil.t();

        LogUtil.w(input);
        LogUtil.w("\n");

        //Do post and get response
        String response = null;
        try {
            response = tryPostAndGetResponse(input);
        } catch (IOException e) {
            LogUtil.w("Network Exception\n");
            throw new NetworkFailException("Network Error");
        }

        //Check result
        if(response == null || response.length() <= 0) {
            LogUtil.w("Response empty\n");
            throw new IllegalStateException("Reponse is empty");
        }

        LogUtil.w("End: ");
        LogUtil.t();
        LogUtil.w("\n\n\n\n");
        
        LogUtil.w("response: "+ response);

        //get result
        return response;
    }

    private static String tryPostAndGetResponse(String input) throws IOException  {
        //encode
        String encodedInput = Coder.encode(input);

        //post and response
        Poster req = new Poster(encodedInput);
        HttpResponse response = req.post();

        LogUtil.w("Response: ");
        LogUtil.t();

        //parse response
        String decodeInput = parseResponse(response);

        //decode
        String decodeOuput = Coder.decode(decodeInput);
        
        LogUtil.w("Decoded\n");
        LogUtil.w(decodeOuput);

        //get result
        return decodeOuput;
    }

    private static String parseResponse(HttpResponse response) throws IOException  {
        //check response
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            LogUtil.w("Response Error\n");
            throw new HttpResponseException(statusCode, "response fail");
        }

        //get result from entity.
        String result = null;
        HttpEntity entity = null;
        try {
            entity = response.getEntity();
            if(entity == null) {
                throw new HttpResponseException(statusCode, "Entity null");
            }
            InputStream responseIn = entity.getContent();
            result = IOUtils.toString(responseIn); // TextUtilies.convertStreamToString(responseIn);  
            LogUtil.w(result);
            LogUtil.w("\n");
        } 
        finally {
            if(entity != null) {
                try {
                    entity.consumeContent();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //get result
        return result;
    }

    /*
     * Create package for post,
     * post the package.
     */
    private static class Poster {
        private static final String API_POST_NAME = "request";

        private HttpPost mPost;
        private ArrayList<BasicNameValuePair> mPairs;

        public Poster(String content) {
            createPairs(content);
        }

        final void createPairs(String content) {
            mPairs = new ArrayList<BasicNameValuePair>();
            mPairs.add(new BasicNameValuePair(API_POST_NAME, content));
        }

        public HttpResponse post() throws ClientProtocolException, IOException {                       
            //try 2 times if fail.
            HttpResponse resp = null;
            boolean suc = true;
            for (int i = 0; i < 3; i++) {
                createPost();
                suc = true;
                resp = null;
                try {
                    resp = HttpManager.execute(mPost);
                } 
                catch (NullPointerException e) {
                    resp = null;
                    suc = false;
                }
                catch (SocketTimeoutException e) {
                    if(i == 0) {
                        mApplyApn = !mApplyApn;
                    }
                    suc = false;
                } 
                catch (IOException e) {
                    LogUtil.w("post Error:" + e.toString() + "\n");
                    suc = false;
                }
                if(suc) {
                    break;
                }
                LogUtil.w("Try post again\n");
                SystemClock.sleep(3000);
            }

            if(resp == null) {
                LogUtil.w("Post Fail!\n");
                throw new IOException();
            }

            return resp;
        }

        void createPost() throws UnsupportedEncodingException {
            mPost = new HttpPost(AppPreference.getPostUrl());
            mPost.setEntity(new UrlEncodedFormEntity(mPairs, HTTP.UTF_8));
        }
    } //class Poster

    private static HttpResponse execute(HttpPost post) throws IOException {
        setProxy();
        return sClient.execute(post);
    }
        
    private static void setProxy() {
        removeProxy();
        if(mApplyApn) {
            chooseSystemProxy();
        }
    }
    
    private static void removeProxy() {
        sClient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
    }
    
    public static String getSystemProxy(Context ctx) {
        String proxy = "";
        Cursor mCursor = ctx.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
        if(mCursor != null) {
            mCursor.moveToFirst();
            if(!mCursor.isAfterLast()) {
                String p = mCursor.getString(mCursor.getColumnIndex("proxy"));
                if(p != null) {
                    proxy = p;
                }
            }
        }
        return proxy.trim();
    }

    private static void chooseSystemProxy() {
        final Context ctx = AppPreference.getContext();

        WifiManager wifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            LogUtil.w("Apn\n");
            String proxyStr = getSystemProxy(ctx);
            if(proxyStr != null && proxyStr.trim().length() > 0){
                LogUtil.w("proxy:"+proxyStr+"\n");
                HttpHost proxy = new HttpHost(proxyStr, 80);
                sClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
            } else {
                LogUtil.w("Wifi\n");
            }
        }
    }

    private static void chooseStaticProxy() {
        final Context ctx = AppPreference.getContext();

        ConnectivityManager manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo info = manager.getActiveNetworkInfo();  
        if(info != null) {
            if(info.getTypeName().equals("MOBILE")) {
                HttpHost proxy = null;
                String name = info.getExtraInfo();
                if(name != null && name.length() > 0) {
                    name = name.toLowerCase();
                    if(name.contains("cmwap")) {
                        proxy = new HttpHost( "10.0.0.172", 80, "http");
                    } 
                    else if(name.contains("uniwap")) {
                        proxy = new HttpHost( "10.0.0.172", 80, "http");
                    } 
                    else if(name.contains("ctwap")) {
                        proxy = new HttpHost( "10.0.0.200", 80, "http");
                    }
                }
                if(proxy != null) {
                    sClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                }
            } 
        }
    }

    /*
     * Encode string to utf8,
     * decode utf8 string.
     */
    private static class Coder {
        public static String encode(String input) {
            String output =  Base64.encode(input);
            return output;
        }

        public static String decode(String input) {
            String output = null;
            try {
                output = new String(Base64.decode(input));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("HttpManager Decode Fail");
            }
            return output;
        }
    } //class Coder

}