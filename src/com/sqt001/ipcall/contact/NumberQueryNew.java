package com.sqt001.ipcall.contact;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

class NumberQueryerNew extends NumberQueryer {
  public NumberQueryerNew(Cursor cursor, Context context) {
    super(cursor, context);
  }
  
  @Override
  protected long onQueryContactId() {
    return cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));  
  }
  
  @Override
  protected void onQuery() {
    queryAll();
  }
  
  private void queryAll() {
    boolean hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0;
    if(!hasPhone) 
      return;
            
    Cursor phones = context.getContentResolver().query(  
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  
        null,  
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, 
        null, 
        null);
      
    if(phones.moveToFirst()) {
      do {
        String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));    
        add(number);
      } while (phones.moveToNext());
    }
    phones.close();
  }
}