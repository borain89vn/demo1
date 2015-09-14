package com.knx.framework.arise;

import android.content.Context;
import android.content.pm.PackageManager;

public class ARiseHelpMethods {
	
	public static boolean checkARSupportedForDevice(Context ctx) {
		PackageManager packageManager = ctx.getPackageManager();

		if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true; // yes, device has camera
		} else {
			return false; // no, device has not camera
		}
	}

}
