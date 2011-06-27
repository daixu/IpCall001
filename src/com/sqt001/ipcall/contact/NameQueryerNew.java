package com.sqt001.ipcall.contact;

import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * Get name
 */
class NameQueryerNew extends NameQueryer {
  public NameQueryerNew(Cursor cursor) {
    super(cursor);
  }
        
  @Override
  protected String[] onQuery() {
    String[] nameAry = new String[1]; 
    String nameColumn = ContactsContract.Contacts.DISPLAY_NAME;
    Cursor c = getCursor();
    String name = c.getString(c.getColumnIndexOrThrow(nameColumn));
    if(name != null) {
      nameAry[0] = name;
    } else {
      nameAry[0] = EMPTY_NAME;
    }
    return nameAry;
  }
}

