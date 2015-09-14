package com.gkxim.android.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class ContentCacheDB {

	private static final String DATABASE_NAME = "contentcache.db";
	private static final int DATABASE_VERSION = 1;

	public static final String CONTENT_TABLE = "CONTENT";

	private static final String URL_ID = "_id";
	private static final String URL_URL = "Url";
	private static final String URL_CONTENT = "Content";
	private static final String URL_TIMEOUT = "Timeout";
	private static final String URL_TIMEIN = "Timein";
	private static final String URL_KEY = "Key";
	private static final String URL_SAVED = "Save";
	private static final String CREATE_TABLE_LABEL = "create table "
			+ CONTENT_TABLE + " (" + URL_ID
			+ " integer primary key autoincrement, " + URL_URL
			+ " text not null, " + URL_CONTENT + " text not null, "
			+ URL_TIMEOUT + " text not null, " + URL_TIMEIN + " text not null,"
			+ URL_KEY + " text not null, " + URL_SAVED + " integer not null);";
	private static final String TAG = "ContentCacheDB";

	private SQLiteDatabase mDB;
	private final Context mContext;
	private DbHelper mDBHelper;
	private String mDBPath = "";

	public ContentCacheDB(Context context) {
		mContext = context;
		// Nam.nguyen
		mDBHelper = new DbHelper(mContext, DATABASE_NAME, null,
				DATABASE_VERSION);
		mDBPath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
	}

	public void open() {
		try {
			mDB = mDBHelper.getWritableDatabase();
		} catch (Exception e) {
			GKIMLog.lf(null, 4,
					"ContentCacheDB=>SQLiteException: " + e.getMessage());
		}
	}

	public void close() {
		mDB.close();
	}

	/*
	 * Process CREATE_TABLE_LABEL
	 */
	public long insertURL(String url, String content, long timeout, String key) {
		if (mDBHelper == null) {
			return -1;
		}
		if (mDB == null) {
			open();
		}
		String contents = removeChar(content);
		ContentValues newLabelValues = new ContentValues();
		newLabelValues.put(URL_URL, url);
		newLabelValues.put(URL_CONTENT, contents);
		newLabelValues.put(URL_TIMEOUT, String.valueOf(timeout));
		newLabelValues.put(URL_TIMEIN,
				String.valueOf(System.currentTimeMillis()));
		newLabelValues.put(URL_KEY, key);
		newLabelValues.put(URL_SAVED, 0);
		return mDB.insert(CONTENT_TABLE, null, newLabelValues);

		// mDB.execSQL("INSERT INTO `CONTENT` (`Url`, `Content`, `Timeout`, `Timein`, `Key`) VALUES('"
		// + url
		// + "','"
		// + contents
		// + "','"
		// + timeout
		// + "','"
		// + String.valueOf(System.currentTimeMillis())
		// + "','"
		// + key
		// + "')");
		// return 1;

	}

	public void updateURL(String oldUrl, String url, String content,
			long timeout, String key) {

		int id = getKeyCacherID(key);

		if (id != -1) {
			ContentValues newLabelValues = new ContentValues();
			newLabelValues.put(URL_URL, url);
			newLabelValues.put(URL_CONTENT, removeChar(content));
			newLabelValues.put(URL_TIMEOUT, String.valueOf(timeout));
			newLabelValues.put(URL_TIMEIN,
					String.valueOf(System.currentTimeMillis()));
			newLabelValues.put(URL_KEY, key);
			newLabelValues.put(URL_SAVED, 0);
			mDB.update(CONTENT_TABLE, newLabelValues, URL_ID + "=" + id, null);
		}
	}

	public void udpateSaved(int id, String url, String content, long timeout,
			String key, int saved) {
		ContentValues newLabelValues = new ContentValues();
		newLabelValues.put(URL_URL, url);
		newLabelValues.put(URL_CONTENT, removeChar(content));
		newLabelValues.put(URL_TIMEOUT, String.valueOf(timeout));
		newLabelValues.put(URL_TIMEIN,
				String.valueOf(System.currentTimeMillis()));
		newLabelValues.put(URL_KEY, key);
		newLabelValues.put(URL_SAVED, saved);
		mDB.update(CONTENT_TABLE, newLabelValues, URL_ID + "=" + id, null);

	}

	public long deleteURL(int id) {
		return mDB.delete(CONTENT_TABLE, URL_ID + "=?",
				new String[] { id + "" });
	}

	public Cursor getURLs() {
		if (mDB == null) {
			open();
		}
		Cursor cursor = null;
		try {
			cursor = mDB.query(CONTENT_TABLE, new String[] { URL_ID, URL_URL,
					URL_CONTENT, URL_TIMEOUT, URL_TIMEIN, URL_KEY, URL_SAVED },
					null, null, null, null, null);
		} catch (SQLiteException e) {
			return null;
		}

		return cursor;
	}

	public Cursor getURL(int id) {
		if (mDB == null) {
			open();
		}
		Cursor cursor = null;
		try {
			cursor = mDB.query(CONTENT_TABLE, new String[] { URL_ID, URL_URL,
					URL_CONTENT, URL_TIMEOUT, URL_TIMEIN, URL_KEY, URL_SAVED },
					URL_ID + " = " + id, null, null, null, null);
		} catch (SQLiteException e) {
			return null;
		}

		return cursor;

	}

	public Cursor getKeyCacher(String cacher) {
		if (mDB == null) {
			open();
		}
		Cursor cursor = null;
		try {
			cursor = mDB
					.query(CONTENT_TABLE, new String[] { URL_ID, URL_URL,
							URL_CONTENT, URL_TIMEOUT, URL_TIMEIN, URL_KEY,
							URL_SAVED }, URL_KEY + " = " + setFormat(cacher),
							null, null, null, null);
			if (cursor == null && cacher.contains("/issue")) {
				cursor = mDB.query(CONTENT_TABLE, new String[] { URL_ID,
						URL_URL, URL_CONTENT, URL_TIMEOUT, URL_TIMEIN, URL_KEY,
						URL_SAVED },
						URL_KEY + " LIKE " + setFormatLike(cacher), null, null,
						null, null);
			}
		} catch (SQLiteException e) {
			return null;
		}

		return cursor;
	}

	public int getKeyCacherID(String cacher) {
		if (mDB == null) {
			open();
		}
		Cursor cursor = null;
		int id = -1;
		try {
			cursor = mDB
					.query(CONTENT_TABLE, new String[] { URL_ID, URL_URL,
							URL_CONTENT, URL_TIMEOUT, URL_TIMEIN, URL_KEY,
							URL_SAVED }, URL_KEY + " = " + setFormat(cacher),
							null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				String sID = cursor.getString(1);
				if (sID != null && sID != "") {
					try {
						id = Integer.parseInt(sID);
					} catch (Exception e) {
						return id;
					}
				}
			}
		} catch (SQLiteException e) {
			return id;
		}
		return id;
	}

	public Cursor getAll() {
		if (mDB == null) {
			open();
		}
		Cursor cursor = null;
		try {
			cursor = mDB.query(CONTENT_TABLE, new String[] { URL_ID, URL_URL,
					URL_CONTENT, URL_TIMEOUT, URL_TIMEIN, URL_KEY, URL_SAVED },
					null, null, null, null, null);
		} catch (SQLiteException e) {
			return null;
		}

		return cursor;
	}

	public boolean databaseExist() {
		SQLiteDatabase checkDB = null;
		try {
			checkDB = SQLiteDatabase.openDatabase(mDBPath, null,
					SQLiteDatabase.OPEN_READONLY);
			checkDB.close();
			return true;
		} catch (SQLiteException e) {
			GKIMLog.lf(null, 4,
					"ContentCacheDB=>SQLiteException: " + e.getMessage());
		}

		return false;
	}

	public void clearDatabase() {
		mDB.delete(CONTENT_TABLE, null, null);
	}

	public String setFormat(String str) {
		if (str != null) {
			String s = str.replaceAll("\\\\", "\\\\\\\\");
			s = "'" + s.replaceAll("'", "''") + "'";
			return s;
		}
		return null;
	}

	public String setFormatLike(String str) {
		if (str != null) {
			String s = str.replaceAll("\\\\", "\\\\\\\\");
			s = "'" + s.replaceAll("'", "''") + "%'";
			return s;
		}
		return null;
	}

	public String removeChar(String str) {
		GKIMLog.l(1, TAG + " removeChar :" + str);
		if (str != null && str.contains("'")) {
			String s = str.replaceAll("'", "''");
			GKIMLog.l(1, TAG + " removeChar new string   :" + s);
			return s;
		}
		return str;
	}

	private static class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_LABEL);
			// db.setMaximumSize(1024 * 50);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
			// Create a new one.
			onCreate(db);
		}

	}
}
