package com.knx.framework.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "ARise.db";
	private static final int DB_VERSION = 1;

	private static final String ARISE_HISTORY_TABLE = "history";
	private static final String ARISE_BOOKMARK_TABLE = "bookmark";

	private static final String COL_ID = "id";
	private static final String COL_TITLE = "title";
	private static final String COL_POSTER_URL = "posterURL";
	private static final String COL_VIEW_TIME = "viewtime";
	private static final String COL_AR_CONTENT = "arContent";
	
	private static DbHelper instance = null;
	
	public static DbHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DbHelper(context);
		}
		return instance;
	}
	
	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Hashtable<String, String> columns = new Hashtable<String, String>();
		columns.put(COL_ID, "VARCHAR (100) PRIMARY KEY");
		columns.put(COL_TITLE, "VARCHAR (500)");
		columns.put(COL_POSTER_URL, "VARCHAR (500)");
		columns.put(COL_AR_CONTENT, "TEXT");
		columns.put(COL_VIEW_TIME, "INTEGER");
		createTable(db, ARISE_HISTORY_TABLE, columns);
		createTable(db, ARISE_BOOKMARK_TABLE, columns);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Update this method when we need a database upgrade
	}
	
	public List<Map<String, String>> getHistory() {
		List<Map<String, String>> items = new ArrayList<Map<String, String>>();
		Cursor cursor = getWritableDatabase().query(ARISE_HISTORY_TABLE, null, null, null, null, null, COL_VIEW_TIME);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				Map<String, String> item = new HashMap<String, String>();
				item.put(COL_ID, cursor.getString(cursor.getColumnIndex(COL_ID)));
				item.put(COL_TITLE, cursor.getString(cursor.getColumnIndex(COL_TITLE)));
				item.put(COL_POSTER_URL, cursor.getString(cursor.getColumnIndex(COL_POSTER_URL)));
				item.put(COL_AR_CONTENT, cursor.getString(cursor.getColumnIndex(COL_AR_CONTENT)));
				item.put(COL_VIEW_TIME, "" + cursor.getLong(cursor.getColumnIndex(COL_VIEW_TIME)));
				items.add(0, item);
			} while (cursor.moveToNext());
		}
		return items;
    }
	
	public long addToHistory(String id, String title, String posterURL, String arContent) {
		ContentValues values = new ContentValues();
		values.put(COL_ID, id);
		values.put(COL_TITLE, title);
		values.put(COL_POSTER_URL, posterURL);
		values.put(COL_AR_CONTENT, arContent);
		values.put(COL_VIEW_TIME, Long.valueOf(new Date().getTime()).toString());
		
		if (isAddedToHistory(id)) {
			return getWritableDatabase().replace(ARISE_HISTORY_TABLE, null, values);
		} else {
			return getWritableDatabase().insert(ARISE_HISTORY_TABLE, null, values);
		}
	}
	
	public boolean isAddedToHistory(String id) {
		Cursor cursor = getReadableDatabase().rawQuery("select * from " + ARISE_HISTORY_TABLE + " where id=?", new String[] {id});
		if (cursor != null && cursor.getCount() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public int removeFromHistory(String id) {
		return getWritableDatabase().delete(ARISE_HISTORY_TABLE, "id = ?", new String[] { id });
	}
	
	public void clearHistory() {
        getWritableDatabase().delete(ARISE_HISTORY_TABLE, null, null);
    }
	
	public List<Map<String, String>> getBookmarks() {
		List<Map<String, String>> items = new ArrayList<Map<String, String>>();
		Cursor cursor = getWritableDatabase().query(ARISE_BOOKMARK_TABLE, null, null, null, null, null, COL_VIEW_TIME);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				Map<String, String> item = new HashMap<String, String>();
				item.put(COL_ID, cursor.getString(cursor.getColumnIndex(COL_ID)));
				item.put(COL_TITLE, cursor.getString(cursor.getColumnIndex(COL_TITLE)));
				item.put(COL_POSTER_URL, cursor.getString(cursor.getColumnIndex(COL_POSTER_URL)));
				item.put(COL_AR_CONTENT, cursor.getString(cursor.getColumnIndex(COL_AR_CONTENT)));
				item.put(COL_VIEW_TIME, "" + cursor.getLong(cursor.getColumnIndex(COL_VIEW_TIME)));
				items.add(item);
			} while (cursor.moveToNext());
		}
		return items;
    }
	
	public long addToBookmark(String id, String title, String posterURL, String arContent) {
		ContentValues values = new ContentValues();
		values.put(COL_ID, id);
		values.put(COL_TITLE, title);
		values.put(COL_POSTER_URL, posterURL);
		values.put(COL_AR_CONTENT, arContent);
		values.put(COL_VIEW_TIME, Long.valueOf(new Date().getTime()).toString());
		return getWritableDatabase().insert(ARISE_BOOKMARK_TABLE, null, values);
	}
	
	public int removeFromBookmark(String id) {
        return getWritableDatabase().delete(ARISE_BOOKMARK_TABLE, "id = ?", new String[] { id });
    }
	
	public void clearBookmark() {
    	getWritableDatabase().delete(ARISE_BOOKMARK_TABLE, null, null);
    }
	
    public boolean isBookmarked(String id) {
        Cursor cursor = getWritableDatabase().rawQuery("select * from " + ARISE_BOOKMARK_TABLE +" where id = ?", new String[] { id });
        return cursor != null && cursor.getCount() > 0;
    }

	private void createTable(SQLiteDatabase db, String tableName, Hashtable<String, String> columns) {
		StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " ( ");
		for (String column : columns.keySet()) {
			sql.append(" " + column + " " + columns.get(column) + ",");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(" );");
		db.execSQL(sql.toString());
	}
}
