package com.sqt001.ipcall.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IpCallHelper extends SQLiteOpenHelper {
	private final static String DBNAME = "IPCALL_DBSQLITE";
	private final static int DB_VERSION = 1;

	public final static String SOFT_TABLE = "softTable";
	public final static String SOFT_ID = "soft_id";
	public final static String SOFT_TITLE = "soft_title";
	public final static String SOFT_MESSAGE = "soft_message";
	public final static String SOFT_URL = "soft_url";
	
	public IpCallHelper(Context context) {
		super(context, DBNAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String bdSoft = "CREATE TABLE IF NOT EXISTS "
			+ SOFT_TABLE + " ("
			+ SOFT_ID + " TEXT, "
			+ SOFT_TITLE + " TEXT, "
			+ SOFT_MESSAGE + " TEXT, "			
			+ SOFT_URL + " TEXT);";
		
		db.execSQL(bdSoft);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// 数据库被改变时，将原先的表删除，然后建立新表
		//删除黑名单表
		String bdSoft = "drop table if exists " + SOFT_TABLE;
		db.execSQL(bdSoft);
	}

}
