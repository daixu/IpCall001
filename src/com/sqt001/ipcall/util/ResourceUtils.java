package com.sqt001.ipcall.util;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

/**
 * Get String from Raw resource.
 * 
 * Usage:
 * ResourceUtils util = new ResourceUtils(Context);
 * String message = util.getStringfromRawResource(rawResId);
 */
public class ResourceUtils {
    private static final String EMPTY_STRING = "";
    private Context context;
    
    public ResourceUtils(Context context) {
        this.context = context;
    }
    
    public String getStringFromRawResource(int rawResId) {
        InputStream is = context.getResources().openRawResource(rawResId);   
        String result = null;
        try {
            result = IOUtils.toString(is);// TextUtilies.convertStreamToStringWithBreakLine(is);
        } catch (IOException e) {
            result = EMPTY_STRING;
        }
        return result;
    }
}
