package com.sqt001.ipcall.contact;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

public final class NameLookupNew extends NameLookup {
  @Override
  public String onGetName(Context ctx,String number){
    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
    String[] projection1 = new String[]{ PhoneLookup.DISPLAY_NAME};
    Cursor cursor1 = ctx.getContentResolver().query(uri, projection1, null,null,null);
    if (cursor1.moveToFirst()) {
      String name = cursor1.getString(cursor1
                                      .getColumnIndex(PhoneLookup.DISPLAY_NAME));
      int phoneNameIndex = cursor1.getColumnIndex(PhoneLookup.DISPLAY_NAME);
      String phoneNameStr = cursor1.getString(phoneNameIndex);
      return name;
    }
    return "";
  }
}
