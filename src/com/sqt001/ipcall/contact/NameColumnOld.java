package com.sqt001.ipcall.contact;

import android.provider.Contacts.People;

public class NameColumnOld extends NameColumn{
    @Override
    protected String[] onGetNameColumn() {
        return new String[] {People.NAME};
    }
}
