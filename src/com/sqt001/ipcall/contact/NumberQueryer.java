package com.sqt001.ipcall.contact;

import java.util.ArrayList;

import com.sqt001.ipcall.application.AppPreference;

import android.content.Context;
import android.database.Cursor;


/**
 * <p>Query contact number.</p>
 * <h2>Usage:</h2>
 * <p>
 * String[] numbers = NumberQueryer.create(cursor, context).query();
 * </p>
 */
public abstract class NumberQueryer { 
    protected static final String BLANK_NUMBER = "";     

    protected long contactId = 0;
    protected  ArrayList<String> numberAry = null;
    protected Cursor cursor;
    protected Context context;

    public static NumberQueryer create(Cursor cursor, Context context) {
        if(AppPreference.isEclairOrLater()) {
            return new NumberQueryerNew(cursor, context);
        } else {
            return new NumberQueryerOld(cursor, context);
        }
    }

    public NumberQueryer(Cursor cursor, Context context) {
        this.cursor = cursor;
        this.context = context;
        numberAry = new ArrayList<String>();
    }

    public String[] query() {
        clear();
        queryContactId();
        onQuery();
        return toArray();
    }

    private void clear() {
        numberAry.clear();     
    }

    private void queryContactId() {
        contactId = onQueryContactId();
    }

    protected abstract long onQueryContactId();

    protected abstract void onQuery();

    private String[] toArray() {
        String[] to = null;
        if(!numberAry.isEmpty()) {
            to = new String[numberAry.size()];
            numberAry.toArray(to);    
        }
        return to;
    }

    protected void add(String number) {
        if(number != null && !BLANK_NUMBER.equals(number)) {
            numberAry.add(number);
        }
    }
}