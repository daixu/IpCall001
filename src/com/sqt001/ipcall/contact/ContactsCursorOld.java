package com.sqt001.ipcall.contact;

import android.content.Context;
import android.database.Cursor;

public class ContactsCursorOld extends ContactsCursor {
    public ContactsCursorOld(Context context) {
        super(context);
    }
    
    @Override
    protected Cursor onGetContacts() {
        return context.getContentResolver().query(getContactContentUri(), null, null, null, null);
    }
}
