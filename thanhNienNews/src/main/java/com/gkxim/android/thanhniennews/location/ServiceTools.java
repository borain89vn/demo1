package com.gkxim.android.thanhniennews.location;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceTools {
	private static String LOG_TAG = ServiceTools.class.getName();

	public static boolean isServiceRunning(Context context,
			String serviceClassName) {
		final ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		for (RunningServiceInfo runningServiceInfo : services) {
			if (runningServiceInfo.service.getClassName().equals(
					serviceClassName)) {
				return true;
			}
		}
		return false;
	}
}
