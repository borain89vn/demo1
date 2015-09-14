package com.gkxim.android.utils;

import android.content.Context;
import android.database.Cursor;

/**
 * @author QTC
 * 
 */
public class ContentCache {

	private static final String TAG = "ContentCache";
	private Context mContext;
	private ContentCacheDB mDB = null;

	public ContentCache(Context context) {
		mContext = context;
		if (mDB == null) {
			mDB = new ContentCacheDB(mContext);
		}
		if (mDB != null) {
			mDB.open();
		}
	}

	public void getIntance() {
		if (mDB == null) {
			mDB = new ContentCacheDB(mContext);
		}
		if (mDB != null) {
			mDB.open();
		}
	}

	public void clearAll() {
		mDB.clearDatabase();
	}

	public void closeAll() {
		if (mDB != null) {
			mDB.close();
		}
	}

	/** add (url, content, timeout) to DB */
	public void addDBURL(String url, String content, long timeout, String key) {
		if (mDB == null) {
			return;
		}
		long idRow = mDB.insertURL(url, content, timeout, key);
		GKIMLog.l(1, TAG + " TNPreferenceManager OKKKKKKKKKKKK :" + idRow
				+ " key:" + key);
	}

	public void updateDBURL(String url, String content, long timeout,
			String keyCacher) {
		if (mDB == null) {
			return;
		}
		mDB.updateURL(url, url, content, timeout, keyCacher);
	}

	public void removeDBURL(int id) {
		if (mDB == null) {
			return;
		}
		mDB.deleteURL(id);
	}

	public boolean isDBKeyCacherExist(String cacher) {
		if (mDB == null || cacher == null || cacher == "") {
			return false;
		}
		boolean result = false;
		Cursor cursor = mDB.getKeyCacher(cacher);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				result = true;
			}
			cursor.close();
		}

		return result;
	}

	private static final int URL_ID = 0;
	private static final int URL_URL = 1;
	private static final int URL_CONTENT = 2;
	private static final int URL_TIMEOUT = 3;
	private static final int URL_TIMEIN = 4;
	private static final int URL_KEY = 5;
	private static final int URL_SAVED = 6;

	/**
	 * Get content from DB Key cacher
	 */
	public String getContentFromKeyCacher(String keyCacher) {
		if (mDB == null) {
			return null;
		}
		try {
			Cursor cursor = mDB.getKeyCacher(keyCacher);
			// just a double check for sure, but mLastCursor is normally not
			// null
			// when get here.
			if (cursor != null) {
				GKIMLog.l(1, TAG + " getContentFromURL keyCacher:" + keyCacher);
				if (cursor.getCount() >= 1) {
					cursor.moveToFirst();
					String content = cursor.getString(URL_CONTENT);
					// usually the cursor is finish when it has returned the
					// content.
					GKIMLog.l(1, TAG + " getContentFromURL content:" + content);
					cursor.close();
					return content;
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return "";
	}

	@SuppressWarnings("unused")
	private long getTimeout(String keyCacher) {
		if (mDB == null) {
			return 0;
		}
		Cursor cursor = mDB.getKeyCacher(keyCacher);
		if (cursor != null && cursor.getCount() == 1) {
			cursor.moveToFirst();
			String content = cursor.getString(URL_TIMEOUT);
			cursor.close();
			return Long.parseLong(content);
		}
		return 0;
	}

	@SuppressWarnings("unused")
	private long getTimein(String keyCacher) {
		if (mDB == null) {
			return 0;
		}
		Cursor cursor = mDB.getKeyCacher(keyCacher);
		if (cursor != null && cursor.getCount() == 1) {
			cursor.moveToFirst();
			String content = cursor.getString(URL_TIMEIN);
			cursor.close();
			return Long.parseLong(content);
		}
		return 0;
	}

	/**
	 * @Description: Get long duration that the Content has expired
	 * @param url
	 *            key of content
	 * @return long:
	 *         <p>=
	 *         0: Don't know for sure, it rarely return 0 if the key has
	 *         existed.
	 *         </p>
	 *         <p>
	 *         &lt;0: Not expired yet
	 *         </p>
	 *         <p>
	 *         &gt;0: Expired long time as returned.
	 *         </p>
	 */
	public long hasExpiredLong(String keyCacher) {
		if (mDB == null || keyCacher == null || keyCacher == "") {
			return 0;
		}
		long expired = 0;
		// just a double check for sure, but mLastCursor is normally not null
		// when get here.
		Cursor cursor = mDB.getKeyCacher(keyCacher);
		if (cursor != null) {
			if (cursor.getCount() >= 1) {
				cursor.moveToFirst();
				long timeout = Long.parseLong(cursor.getString(URL_TIMEOUT));
				long timein = Long.parseLong(cursor.getString(URL_TIMEIN));
				expired = (System.currentTimeMillis() - timein - timeout);
			}
			cursor.close();
		}
		return expired;
	}

	public void clearCacheToDay() {
		// TODO Auto-generated method stub
		if (mDB == null) {
			return;
		}
		Cursor cursor = mDB.getAll();
		if (cursor != null) {
			GKIMLog.l(1, TAG + " clearCacheToDay :" + cursor.getColumnCount());
			while (cursor.moveToNext()) {
				long timeout = Long.parseLong(cursor.getString(URL_TIMEOUT));
				long timein = Long.parseLong(cursor.getString(URL_TIMEIN));
				long expired = (System.currentTimeMillis() - timein - timeout);
				int saved = cursor.getInt(URL_SAVED);
				if (expired > 0 && saved == 0) {
					GKIMLog.l(1,
							TAG + " clear url to day :" + cursor.getString(2));
					removeDBURL(cursor.getInt(1));
				}
			}
			cursor.close();
		}
	}

	public void updateSaved(String keyCacher, int saved) {
		// TODO Auto-generated method stub
		if (mDB == null) {
			return;
		}
		Cursor cursor = mDB.getKeyCacher(keyCacher);
		if (cursor != null) {
			cursor.moveToFirst();
			int id = cursor.getInt(URL_ID);
			String url = cursor.getString(URL_URL);
			String content = cursor.getString(URL_CONTENT);
			long timeout = Long.parseLong(cursor.getString(URL_TIMEOUT));
			String key = cursor.getString(URL_KEY);
			mDB.udpateSaved(id, url, content, timeout, key, saved);
		}
		cursor.close();
	}

}
