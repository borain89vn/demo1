/**
 * File: Utils.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 06-11-2012
 * 
 */
package com.gkxim.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.gkim.thanhniennews.R;

/**
 * @author Timon Trinh
 */
public class Utils {

	private static final String TAG = "Utils";
	private Context applicationContext = null;
	private UtilsPreference mPrefUtil = null;

	public Utils(Context context) {
		applicationContext = context.getApplicationContext();
		mPrefUtil = new UtilsPreference(context);
	}

	public Context getApplicationContext() {
		return applicationContext;
	}

	public Resources getResource() {
		return applicationContext.getResources();
	}

	@SuppressWarnings("unused")
	private void setContext(Context context) {
		applicationContext = context.getApplicationContext();
		if (mPrefUtil != null) {
			mPrefUtil = null;
		}
		mPrefUtil = new UtilsPreference(context);
	}

	public boolean isConnectionAvailable() {
		if (applicationContext == null) {
			return false;
		}
		ConnectivityManager cm = (ConnectivityManager) applicationContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// if no network is available networkInfo will be null
		// otherwise check if we are connected
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public String getJSONStringFromAssetFile(String testFilename) {
		if (applicationContext == null) {
			return null;
		}
		String result = null;
		StringBuffer sb = new StringBuffer();
		String line;
		try {
			AssetManager am = applicationContext.getAssets();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					am.open(testFilename), "utf-8"));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			result = sb.toString();
		} catch (IOException e) {
			GKIMLog.lf(null, 4, TAG + "=> IOException: " + e.getMessage());
		}
		return result;
	}

	public UtilsPreference getUtilPreference() {
		if (mPrefUtil == null && applicationContext != null) {
			mPrefUtil = new UtilsPreference(applicationContext);
		}
		return mPrefUtil;
	}

	public boolean hasKey(String key) {
		if (mPrefUtil != null) {
			return mPrefUtil.hasKey(key);
		}
		return false;
	}

	public String getStringPref(String prefName) {
		if (mPrefUtil != null && mPrefUtil.hasKey(prefName)) {
			return mPrefUtil.getStringPref(prefName);
		}
		return "";
	}

	public int getIntPref(String prefName) {
		if (mPrefUtil != null && mPrefUtil.hasKey(prefName)) {
			return mPrefUtil.getIntPref(prefName);
		}
		return -1;
	}

	public float getFloatPref(String prefName) {
		if (mPrefUtil != null && mPrefUtil.hasKey(prefName)) {
			return mPrefUtil.getFloatPref(prefName);
		}
		return -1;
	}

	public boolean getBooleanPref(String prefName) {
		if (mPrefUtil != null && mPrefUtil.hasKey(prefName)) {
			return mPrefUtil.getBooleanPref(prefName);
		}
		return false;
	}

	public InputStream getInputStream(final String urlString) {
		if (isConnectionAvailable()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						URL url = new URL(urlString);
						url.getContent();
					} catch (Exception e) {
						GKIMLog.lf(null, 4, TAG
								+ "=> failed to getInputStream: " + urlString
								+ " because: " + e.getMessage());
					}
				}
			}).start();

		}
		return null;
	}

	public void setIntPref(String prefKeyname, int size) {
		if (mPrefUtil != null) {
			mPrefUtil.setIntPref(prefKeyname, size);
		}
	}

	public void setBoolPref(String prefKeyname, boolean state) {
		if (mPrefUtil != null) {
			mPrefUtil.setBooleanPref(prefKeyname, state);
		}
	}

	public void setFloatPref(String prefKeyname, float size) {
		if (mPrefUtil != null) {
			mPrefUtil.setFloatPref(prefKeyname, size);
		}
	}

	/**
	 * @param prefKeynameStoriesRead
	 * @param string
	 */
	public void setStringPref(String prefKeyname, String value) {
		if (mPrefUtil != null) {
			mPrefUtil.setStringPref(prefKeyname, value);
		}
	}

	public String getDeviceIMEI() {
		if (applicationContext != null) {
			TelephonyManager tm = (TelephonyManager) applicationContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			String imei = tm.getDeviceId();
			if (imei != null) {
				return imei;
			}
		}
		return "";
	}

	public static String arrayToString(String[] items, String seperator) {
		if ((items == null) || (items.length == 0)) {
			return "";
		} else {
			StringBuffer buffer = new StringBuffer(items[0]);
			for (int i = 1; i < items.length; i++) {
				buffer.append(seperator);
				buffer.append(items[i]);
			}
			return buffer.toString();
		}
	}

	public void addShortcut() {
		// Adding shortcut for MainActivity
		// on Home screen

		Intent shortcutIntent = new Intent(Intent.CATEGORY_LAUNCHER);
		shortcutIntent.setClassName(applicationContext,
				"com.gkxim.android.thanhniennews.SplashActivity");
		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, applicationContext
				.getResources().getString(R.string.app_name));
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(applicationContext,
						R.drawable.ic_launcher));
		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		addIntent.putExtra("duplicate", false);

		applicationContext.sendBroadcast(addIntent);
	}

	public void removeShortcut() {

		// Deleting shortcut for MainActivity
		// on Home screen
		Intent shortcutIntent = new Intent(Intent.CATEGORY_LAUNCHER);

		shortcutIntent.setClassName(applicationContext,
				"com.gkxim.android.thanhniennews.SplashActivity");

		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, applicationContext
				.getResources().getString(R.string.app_name));

		addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
		applicationContext.sendBroadcast(addIntent);
	}

	public static boolean checkGPSProviderEnabled(Context mContext) {
		if (mContext != null) {
			LocationManager service = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
			boolean enabled = service
					.isProviderEnabled(LocationManager.GPS_PROVIDER) || service
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			// check if enabled and if not send user to the GSP settings
			// Better solution would be to display a dialog and suggesting to
			// go to the settings
			// if (!enabled && SettingsFragment.getGPSProvider(mContext)) {
			// if (!enabled) {
			// Intent intent = new Intent(
			// Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// mContext.startActivity(intent);
			// }
			return enabled;
		}
		return false;
	}

	public static void gotoGPSProvider(Context mContext) {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}

}
