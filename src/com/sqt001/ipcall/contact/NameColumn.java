package com.sqt001.ipcall.contact;

import com.sqt001.ipcall.application.AppPreference;

/**
 * <p>Get contact name column.</p>
 * <h2>Usage:</h2>
 * <p>
 * String[] column = NameColumn.create().getColumn();
 * </p>
 */
public abstract class NameColumn {
    public static NameColumn create() {
        if(AppPreference.isEclairOrLater()) {
            return new NameColumnNew();
        } else {
            return new NameColumnOld();
        }
    }
    
    public String[] getColumn() {
        return onGetNameColumn();
    }
    
    protected abstract String[] onGetNameColumn();
}
