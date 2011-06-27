package com.sqt001.ipcall.contact;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.sqt001.ipcall.application.AppPreference;

/**
 * <p>Get contacts's cursor.</p>
 * 
 * <h2>Usage:</h2>
 * <p>
 * Cursor c = ContactsCursor.create().getContacts();
 * </p>
 */
public abstract class ContactsCursor {
    protected Context context;

    public static ContactsCursor create(Context context) {
        if(AppPreference.isEclairOrLater()) {
            return new ContactsCursorNew(context);
        } else {
            return new ContactsCursorOld(context);
        }
    }

    public ContactsCursor(Context context) {
        this.context = context;
    }

    public Cursor getContacts() {
        return onGetContacts();
    }

    protected abstract Cursor onGetContacts();

    protected  Uri getContactContentUri() {
        return  ContactContentUri.create().getUri();
    }
}
