package com.sqt001.ipcall.contact;

import com.sqt001.ipcall.application.AppPreference;

import android.database.Cursor;

/**
 * <p>Query contact name.</p>
 * <h2>Usage:</h2>
 * <p>
 * String[] names = NameQueryer.create(cursor).query();
 * </p>
 */
public abstract class NameQueryer {
    protected static final String EMPTY_NAME = "";
    private Cursor cursor;
    
    public static NameQueryer create(Cursor cursor) {
        if(AppPreference.isEclairOrLater()) {
            return new NameQueryerNew(cursor);
        } else {
            return new NameQueryerOld(cursor);
        }
    }

    public NameQueryer(Cursor cursor) {
        this.cursor = cursor;
    }

    public String[] query() {
        return onQuery();
    }

    protected Cursor getCursor() {
        return cursor;
    }

    protected abstract String[] onQuery();
}



