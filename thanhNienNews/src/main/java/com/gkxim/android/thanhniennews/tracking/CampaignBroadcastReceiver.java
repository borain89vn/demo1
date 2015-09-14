package com.gkxim.android.thanhniennews.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

public class CampaignBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "CampaignBroadcastReceiver";

	@Override
	public void onReceive(Context arg0, Intent intent) {
		HashMap<String, String> values = new HashMap<String, String>();
		String key = "";
		String value = "";
		try {
			if (intent.hasExtra("referrer")) {
				String[] referrers = intent.getStringExtra("referrer").split(
						"&");
				for (String referrerValue : referrers) {
					String[] keyValue = referrerValue.split("=");
					if (keyValue[0].equals("utm_source")) {
						key = keyValue[1];
					}
					if (keyValue[0].equals("utm_medium")) {
						value = keyValue[1];
					}
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "Exception: " + e.toString());
		}

		Log.d(TAG, "referrer: key:" + key + "  values:" + values);
		values.put(key, value);
		Tracking.sendEvent("app_store_install", values);
		// notify for AffleDonwloadTracker as in
		// AffleMultipleInstallReceivers.java
		// (new AffleAppDownloadTracker()).onReceive(arg0, intent);
	}

}
