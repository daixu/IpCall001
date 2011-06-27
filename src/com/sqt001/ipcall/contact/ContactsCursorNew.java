package com.sqt001.ipcall.contact;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactsCursorNew extends ContactsCursor {
    public ContactsCursorNew(Context context) {
        super(context);
    }
    
    @Override
    protected Cursor onGetContacts() {
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" +
        "1" + "'";    
      return context.getContentResolver().query(getContactContentUri(), null, selection, null, null);
    }
}
