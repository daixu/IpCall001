package com.sqt001.ipcall.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.sqt001.ipcall.application.BuildConfig;


/*
 * Do some log work.
 * make it blank in release version.
 */
public class LogUtil {
    public static final String LOG_FILE_PATH = "/sdcard/IpCall_log.txt";

    public static void i(String tag, String info) {
        Log.i(tag, info);
    }

    public static void t() {
        if(BuildConfig.isRelease()) {
            return;
        }
        Time time = new Time();
        time.setToNow();
        String timeStr = time.format("%Y-%m-%d  %H:%M:%S") + "\n";
        w(timeStr);
    }

    public static void w(String value) {
        if(BuildConfig.isRelease()) {
            return;
        }
        w(value, true);
    }

    public static void clear() {
        if(BuildConfig.isRelease()) {
            return;
        }
        w(" ", false);
    }

    public static void imsi(String value) {
        if(BuildConfig.isRelease()) {
            return;
        }
        w(value, true);
    }
    
    public static void m9(String value) {
        if(!BuildConfig.isMeizuM9()) {
            return;
        }
        w(value, true);
    }

    private static void w(String value, boolean append) {
        if(value == null || value.length() <= 0) {
            return;
        }

        FileWriter writer = null;
        try{
            File file = new File(LOG_FILE_PATH);
            if(!file.exists()) {
                file.createNewFile();
            }

            writer = new FileWriter(file, append);
            writer.write(value);
            writer.write("\n");  
            writer.flush();      
        } catch (Exception e) {
            //do nothing.
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    //do nothing.
                }
            }
        }
    }


    public static String r() {
        FileReader reader = null;
        String result = "";
        try{
            File file = new File(LOG_FILE_PATH);
            if(!file.exists()) {
                file.createNewFile();
            }

            reader = new FileReader(file);
            int length = (int)file.length();
            char[] temp = new char[length];
            reader.read(temp);
            result = new String(temp);
        } catch (Exception e) {
            //do nothing.
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //do nothing.
                }
            }
        }

        return result;
    }

    public static void toast(Context context, String message) {
        if(BuildConfig.isRelease()) {
            return;
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
