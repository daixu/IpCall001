package com.sqt001.ipcall.contact;

import android.content.Context;

import com.sqt001.ipcall.application.AppPreference;

public abstract class NameLookup {
  public static NameLookup create(){
    if(AppPreference.isEclairOrLater()) {
      return new NameLookupNew();
    } else {
      return new NameLookupOld();
    }      
  }     
  
  public String getName(Context ctx,String number) {
    return onGetName(ctx, number);
  }
  
  public abstract String onGetName(Context ctx,String number);
}  
