package com.sqt001.ipcall.contact;

import android.provider.ContactsContract;

public class NameColumnNew extends NameColumn{
    @Override
    protected String[] onGetNameColumn() {
        return new String[] { ContactsContract.Contacts.DISPLAY_NAME};
    }
}
