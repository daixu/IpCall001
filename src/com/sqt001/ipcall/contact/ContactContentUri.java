package com.sqt001.ipcall.contact;

import com.sqt001.ipcall.application.AppPreference;

import android.net.Uri;

/**
 * <p>Get contact content URI.</p>
 * 
 * <h2>Usage:</h2>
 * <p>
 * Uri uri = ContactContentUri.create().getUri();
 *</p>
 */
public abstract class ContactContentUri {
    public static ContactContentUri create() {
        if(AppPreference.isEclairOrLater()) {
            return new ContactContentUriNew();
        } else {
            return new ContactContentUriOld();
        }
    }

    public Uri getUri() {
        return onGetUri();
    }

    protected abstract Uri onGetUri();
}
