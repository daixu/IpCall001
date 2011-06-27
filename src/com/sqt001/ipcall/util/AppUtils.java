package com.sqt001.ipcall.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

public class AppUtils {
    private static final String USER_APPS_DIR = "/data/app/";
    private static final String APP_EXT = ".apk";
    
    public static class PInfo {   
        private String appname = "";   
        private String pname = "";   
        private String versionName = "";   
        private int versionCode = 0;   
        //private Drawable icon;   
        
        public String getName() {
            return appname;
        }
    }   

    public static ArrayList<PInfo> getInstalledApps(Context context) {   
        ArrayList<PInfo> res = new ArrayList<PInfo>();           
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0); //this flag is useless.  
        for(int i=0;i<packs.size();i++) {   
            PackageInfo p = packs.get(i);   
            if(p.versionName == null) {
                continue;
            }
            if(!isUserApp(p.packageName)) {
                continue;
            }   
            PInfo newInfo = new PInfo();   
            newInfo.appname = p.applicationInfo.loadLabel(context.getPackageManager()).toString();   
            newInfo.pname = p.packageName;   
            newInfo.versionName = p.versionName;   
            newInfo.versionCode = p.versionCode;   
            //newInfo.icon = p.applicationInfo.loadIcon(context.getPackageManager());   
            res.add(newInfo);   
        }   
        return res;    
    }  

    private static boolean isUserApp(String packageName) {
        File f = new File(USER_APPS_DIR +  packageName + APP_EXT);
        if(f.exists()) {
            return true;
        }
        return false;
    }

    public static void deletePackage(Context context, String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
    }

    public static boolean isAppExist(Context context, String packageName) {
        boolean exist = false;
        PackageManager  pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, 0);
            exist = true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return exist;
    }
}
