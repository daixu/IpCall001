package com.sqt001.ipcall.util;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sqt001.ipcall.provider.IpCallHelper;

public class DBUtil {
	private IpCallHelper mDbHelper;
	public DBUtil(Context context) {
		mDbHelper = new IpCallHelper(context);
	}
	
	/** 读取所有Soft数据，返回ArrayList数据集群 */
	public ArrayList<SoftObj> readAll() {
		ArrayList<SoftObj> buffer = new ArrayList<SoftObj>();
		
		SQLiteDatabase db = null;
		Cursor cur = null;
		try {
			db = mDbHelper.getWritableDatabase();
			
			cur = db.query(IpCallHelper.SOFT_TABLE, null, null, null, null, null, null);
			
			if(cur == null) 
				return buffer;
			if(cur.getCount() > 0) {
				while(cur.moveToNext()) {
					String id = cur.getString(cur.getColumnIndex(IpCallHelper.SOFT_ID));
					String title = cur.getString(cur.getColumnIndex(IpCallHelper.SOFT_TITLE));
					String message = cur.getString(cur.getColumnIndex(IpCallHelper.SOFT_MESSAGE));
					String url = cur.getString(cur.getColumnIndex(IpCallHelper.SOFT_URL));
					
					
					buffer.add(new SoftObj(id, title, message, url));
				}
			}
		} catch(Exception ex) {
			//exception...
		} finally {
			if(cur != null)
				cur.close();
			if(db != null)
				db.close();				
		}		
		return buffer;
	}
	
	/** 删除所有数据*/
	public boolean delAll() {
		boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = mDbHelper.getWritableDatabase();
			db.delete(IpCallHelper.SOFT_TABLE, null, null);
		} catch(Exception ex) {
			//exception...
		} finally {
			if(db != null)
				db.close();			
		}
		return flag;
	}
	
	/**
	 * 插入SoftObj(id,title,message,url)进入数据库
	 * @param obj
	 * @return
	 */
	public boolean insertSubject(SoftObj obj) {
		boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = mDbHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(IpCallHelper.SOFT_ID, obj.getId());
			cv.put(IpCallHelper.SOFT_TITLE, obj.getTitle());
			cv.put(IpCallHelper.SOFT_MESSAGE, obj.getMessage());
			cv.put(IpCallHelper.SOFT_URL, obj.getUrl());
			
			db.insert(IpCallHelper.SOFT_TABLE, null, cv);
			flag = true;
		} catch(Exception ex) {
			//exception...
		} finally {
			if(db != null)
				db.close();		
		}		
		return flag;
	}
}
