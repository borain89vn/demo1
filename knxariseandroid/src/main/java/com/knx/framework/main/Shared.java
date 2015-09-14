package com.knx.framework.main;

import java.io.File;

import android.content.Context;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.knx.framework.arcontents.OverlayDataManager;
import com.knx.framework.arcontents.StaticDataManager;

public class Shared {

	/************************************************************************************
	 ************************************************************************************/

	private static String assetDir;

	public static String getAssetDir(Context context) {
		File mAssetDir = new File(context
				.getDir("assets", Context.MODE_PRIVATE).getAbsolutePath()
				+ "/arise");
		if (!(mAssetDir.exists())) {
			mAssetDir.mkdirs();
		}

		assetDir = context.getDir("assets", Context.MODE_PRIVATE)
				.getAbsolutePath() + "/arise";

		return assetDir;
	}

	/************************************************************************************
	 ************************************************************************************/

	private static String posterDir;

	public static String getPosterDir(Context context) {
		File mAssetDir = new File(context
				.getDir("assets", Context.MODE_PRIVATE).getAbsolutePath()
				+ "/arise_poster");
		if (!(mAssetDir.exists())) {
			mAssetDir.mkdirs();
		}

		posterDir = context.getDir("assets", Context.MODE_PRIVATE)
				.getAbsolutePath() + "/arise_poster";

		return posterDir;
	}

	/************************************************************************************
	 ************************************************************************************/

	private static AndroidApplicationConfiguration configs;

	public static AndroidApplicationConfiguration getAndroidApplicationConfiguration() {
		if (configs == null) {
			configs = new AndroidApplicationConfiguration();
			configs.r = 8;
			configs.g = 8;
			configs.b = 8;
			configs.a = 8;
		}
		return configs;
	}

	/************************************************************************************
	 ************************************************************************************/

	public static LocalTrackingManager getTrackingEngineManager() {
		return LocalTrackingManager.getSingletonInstance();
	}

	public static OverlayDataManager getOverlayDataManager() {
		return OverlayDataManager.getSingletonInstance();
	}

	public static StaticDataManager getStaticDataManager() {
		return StaticDataManager.getSingletonInstance();
	}

	public static CameraActivityUI getCameraActivityUI() {
		return CameraActivityUI.getSingletonInstance();
	}
}
