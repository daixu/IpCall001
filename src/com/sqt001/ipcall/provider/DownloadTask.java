package com.sqt001.ipcall.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.sqt001.ipcall.R;
import com.sqt001.ipcall.util.HttpManager;
import com.sqt001.ipcall.util.Tuple;
import com.sqt001.ipcall.util.UserTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.SystemClock;

public class DownloadTask extends UserTask<Void, String, Void> {
    private static final String APK_FILE = "/sdcard/ndipcall/NdIpCall.apk";
    private static final String APK_FILE_PART = "/sdcard/ndipcall/NdIpCall.apk.part";
    private static final String DOWNLOAD_DIR = "/sdcard/ndipcall";
    
    private Context context;
    private ProgressDialog pd;
    public boolean please_abort = false;
    private String url = "";
    
    private boolean success = true;
    private String reason = "";

    public DownloadTask(Context context, String url){
        this.context = context;
        this.url = url;
    }

    @Override
    public void  onPreExecute(){
        pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.update_downloading));
        pd.setIndeterminate(true);
        pd.setCancelable(true);

        pd.setOnDismissListener( new DialogInterface.OnDismissListener(){
            @Override
            public void onDismiss(DialogInterface dialog) {
                please_abort = true;
            }
        });

        pd.setOnCancelListener( new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog) {
                please_abort = true;
            }
        });

        pd.show();
    }

    public static String printSize( int size ){
        if ( size >= (1<<20) )
            return String.format("%.1f MB", size * (1.0/(1<<20)));
        if ( size >= (1<<10) )
            return String.format("%.1f KB", size * (1.0/(1<<10)));
        return String.format("%d bytes", size);
    }

    private void downloadApk() throws Exception{
        prepareDownloadDir();
        
        InputStream is = openDownloadInputStream(); 
        FileOutputStream fos = new FileOutputStream (APK_FILE_PART);
        byte[]  buffer = new byte [4096];
        int totalcount =0;
        long tprint = SystemClock.uptimeMillis();
        int partialcount = 0;

        while(true){
            if(please_abort) {
                throw new Exception("Cancel");
            }

            int count = is.read (buffer);
            if(count<=0) break;
            fos.write (buffer, 0, count);

            totalcount += count;
            partialcount += count;

            long tnow =  SystemClock.uptimeMillis();
            if((tnow-tprint)> 1000) {
                float size_MB = totalcount * (1.0f/(1<<20));
                float speed_KB = partialcount  * (1.0f/(1<<10)) * ((tnow-tprint)/1000.0f);

                //In sdk 2.3, string with '%' will build fail.
                publishProgress( String.format("下载 %.1f MB (%.1f KB/秒)",
                        size_MB, speed_KB));

                tprint = tnow;
                partialcount = 0;
            }
        }
        is.close();
        fos.close();
        
        if(please_abort) {
            deleteFile(APK_FILE_PART);
            return;
        } 
        else { 
            deleteFile(APK_FILE);
            new File(APK_FILE_PART)
            .renameTo(new File(APK_FILE));
        }

        SystemClock.sleep(2000);
    }
    
    private void prepareDownloadDir() {
        File dir = new File(DOWNLOAD_DIR);
        if(!dir.mkdirs()) {
            deleteFile(APK_FILE_PART);
        }
    }
    
    private void deleteFile(String path) {
        File f = new File(path);
        if(f.exists()) {
            f.delete();
        }
    }
    
    private InputStream openDownloadInputStream() throws MalformedURLException, IOException {
        String proxy = HttpManager.getSystemProxy(context);
        
        //cmnet
        if(proxy == null || proxy.length() <= 0) { 
            return new URL(url).openStream();
        }
        
        //cmwap
        Tuple<String> urls = swapHostWithProxy(proxy);    
        HttpURLConnection conn = openConnection(urls.get(0), urls.get(1));
        return  conn.getInputStream();
    }
    
    private Tuple<String> swapHostWithProxy(String proxy) throws MalformedURLException {       
        String host = new URL(url).getHost();
        String newUrl = url.replace(host, proxy);
        return new Tuple<String>(newUrl, host);
    }
    
    private HttpURLConnection openConnection(String newUrl, String host) throws MalformedURLException, IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(newUrl).openConnection(); 
        conn.setRequestProperty("X-Online-Host", host);
        conn.setDoInput(true);
        conn.connect();
        return conn;
    }

    @Override
    public Void doInBackground(Void... unused) {
        try {
            long t = SystemClock.uptimeMillis();
            downloadApk();
            t = SystemClock.uptimeMillis() - t;
        } catch (Exception e) {
            markFail();
            e.printStackTrace();
        }
        return(null);
    }

    private void markFail( ) {
        success = false;
        reason = context.getString(R.string.download_fail);
    }
    
    @Override
    public void onProgressUpdate(String... progress) {
        pd.setMessage( progress[0]);
    }

    @Override
    public void onPostExecute(Void unused) {
        pd.dismiss();
        if(success) {
            handleSuccess();
        } else {
            handleFail();
        }
    }

    private void handleSuccess() {
        File f = new File(APK_FILE);
        if(f.exists()) {
            Intent i = new Intent();
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setAction(android.content.Intent.ACTION_VIEW);

            String type = "application/vnd.android.package-archive";
            i.setDataAndType(Uri.fromFile(f), type);
            context.startActivity(i);
        }
    }

    private void handleFail() {
        new AlertDialog.Builder(context)
        .setMessage(reason)
        .setPositiveButton(context.getString(R.string.ok), null)
        .show();
    }
}