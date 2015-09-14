/**
 * Copyright (C) Textwith dexter
 * Website http://www.textwithdextr.com
 * 
 * File: DextrPreferenceManager.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: Jun 12, 2012
 * 
 */
package com.gkxim.android.utils;

import java.util.Hashtable;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Handler;
import android.preference.PreferenceManager;

public class UtilsPreference {

	private static final String TAG = "UtilsPreference";
	public static final String EMPTY_STRING = "";
	protected static final long CONST_DELAY_MESSAGE = 200;
	
	//it should be application context
	private Context mApplicationContext = null;
	private Service mService = null;
	private Hashtable<String, Handler> mObserver = new Hashtable<String, Handler>();

	/**
	 * Listener object for changing in the SharedPreferences. It just for
	 * logging
	 */
	private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = null;
	private boolean mHasListened = false;
	

	/**
	 * Jun 12, 2012
	 */
	public UtilsPreference(Context mContext) {
		GKIMLog.lf(mContext, 1, TAG + "=> UtilsPreference (" + this
				+ ").");
		this.mApplicationContext = mContext.getApplicationContext();
	}

	public void addHandler(String sObserver, Handler handler) {
		if (!mObserver.containsKey(sObserver)) {
			mObserver.put(sObserver, handler);
		}
		if (!mHasListened) {
			registerSharedPreferenceChangeListener();
		}
	}

	public OnSharedPreferenceChangeListener getOnSharedPreferenceChangeListener() {
		if (mOnSharedPreferenceChangeListener == null) {
			mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

				@Override
				public void onSharedPreferenceChanged(
						SharedPreferences sharedPreferences, String key) {
					GKIMLog.lf(mApplicationContext, 4, TAG
							+ "=> onSharedPreferenceChanged, key: " + key);
					if (mObserver != null && mObserver.containsKey(key)) {
						mObserver.get(key).sendEmptyMessageDelayed(0, CONST_DELAY_MESSAGE);
					}
				}
			};
		}
		return mOnSharedPreferenceChangeListener;
	}

	public boolean hasKey(String prefName) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplicationContext);
		if (sp.contains(prefName)) {
			return true;
		}
		return false;
	}
	
	/**
	 * @Description: Get String data from preferenceName in Application's
	 *               sharepreference object.
	 * @param String
	 *            prefName
	 * @return String data or empty string if failed.
	 */
	public String getStringPref(String prefName) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplicationContext);
		if (sp.contains(prefName)) {
			if (GKIMLog.DEBUG_ON)
				GKIMLog.lf(null, 1, TAG + "=> getStringPref(" + prefName
						+ "):=" + sp.getString(prefName, EMPTY_STRING));
			return sp.getString(prefName, EMPTY_STRING);
		}
		return EMPTY_STRING;
	}

	/**
	 * @Description: Give String data into a Application's sharepreference
	 *               object.
	 * @param prefName
	 * @param value
	 */
	public void setStringPref(String prefName, String value) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplicationContext);
		sp.edit().putString(prefName, value).commit();
	}

	/**
	 * @Description: Get integer data from preferenceName in Application's
	 *               sharepreference object.
	 * @param String
	 *            prefName
	 * @return integer value or -1 if failed.
	 */
	public int getIntPref(String prefName) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplicationContext);
		if (sp.contains(prefName)) {
			if (GKIMLog.DEBUG_ON)
				GKIMLog.lf(null, 1, TAG + "=> getIntPref(" + prefName
						+ "):=" + String.valueOf(sp.getInt(prefName, -1)));
			return sp.getInt(prefName, -1);
		}
		return -1;
	}
	
	/**
	 * @Description: Give integer data into a Application's sharepreference
	 *               object.
	 * @param String
	 *            prefName
	 * @param integer
	 *            value
	 */
	public void setIntPref(String prefName, int value) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplicationContext);
		sp.edit().putInt(prefName, value).commit();
	}
	
	/**
	 * @Description: Get Float data from preferenceName in Application's
	 *               sharepreference object.
	 * @param String
	 *            prefName
	 * @return integer value or -1 if failed.
	 */
	public float getFloatPref(String prefName) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplicationContext);
		if (sp.contains(prefName)) {
			if (GKIMLog.DEBUG_ON)
				GKIMLog.lf(null, 1, TAG + "=> getFloatPref(" + prefName
						+ "):=" + String.valueOf(sp.getFloat(prefName, -1)));
			return sp.getFloat(prefName, -1f);
		}
		return -1;
	}

	/**
	 * @Description: Give Float data into a Application's sharepreference
	 *               object.
	 * @param String
	 *            prefName
	 * @param float value
	 */
	public void setFloatPref(String prefName, float value) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplicationContext);
		sp.edit().putFloat(prefName, value).commit();
	}

	/**
	 * @Description:
	 * @param String
	 *            prefName
	 * @return boolean with failed value is false
	 */
	public boolean getBooleanPref(String prefName) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplicationContext);
		if (sp.contains(prefName)) {
			if (GKIMLog.DEBUG_ON)
				GKIMLog
						.lf(null,
								1,
								TAG
										+ "=> getBooleanPref("
										+ prefName
										+ "):="
										+ String.valueOf(sp.getBoolean(
												prefName, false)));
			return sp.getBoolean(prefName, false);
		}
		return false;
	}

	/**
	 * @Description: Give boolean data into a Application's sharepreference
	 *               object.
	 * @param StringprefName
	 * @param boolean value
	 */
	public void setBooleanPref(String prefName, boolean value) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplicationContext);
		sp.edit().putBoolean(prefName, value).commit();
	}

	public Handler removeHandler(String sObserver) {
		Handler handle = null;
		if (mObserver.containsKey(sObserver)) {
			handle = mObserver.remove(sObserver);
			if (mObserver.isEmpty() && mHasListened) {
				unregisterSharedPreferenceChangeListener();
			}
		}
		return handle;
	}

	public void registerSharedPreferenceChangeListener() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplicationContext);
		sp.registerOnSharedPreferenceChangeListener(getOnSharedPreferenceChangeListener());
		mHasListened  = true;
		GKIMLog.lf(mApplicationContext, 4, TAG
				+ "=> registerSharedPreferenceChangeListener.");
	}

	public void setService(Service service) {
		mService = service;
	}

	public void unregisterSharedPreferenceChangeListener() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplicationContext);
		sp.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
		mHasListened = false;
		GKIMLog.lf(mApplicationContext, 4, TAG
				+ "=> unregisterSharedPreferenceChangeListener.");
	}

}
