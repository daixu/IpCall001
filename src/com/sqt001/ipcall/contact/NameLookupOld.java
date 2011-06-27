package com.sqt001.ipcall.contact;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;

public final class NameLookupOld extends NameLookup{
  @Override
  public String onGetName(Context ctx,String number){
    String[] projection = new String[] {
      Contacts.Phones.DISPLAY_NAME,
      Contacts.Phones.NUMBER };
 
    // encode the phone number and build the filter URI
    Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(number));
    // query time
    Cursor c = ctx.getContentResolver().query(contactUri, projection, null,
                                              null, null);
 
    // if the query returns 1 or more results
    // return the first result
    if (c.moveToFirst()) {
      String name = c.getString(c.getColumnIndex(Contacts.Phones.DISPLAY_NAME));
      return name;
    }
    return "";
  }
}
