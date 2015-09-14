package com.knx.framework.helper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;

public class ARiseConfigs {
	
	public final static String SDK_VERSION = "3.4(3)";
	
	public static final String DEVELOPER_KEY = "AIzaSyCGv4KFY8ke1onseqbRo9FCgvbQPQNKOeQ";

	public static final String PREF_FILENAME   = "knx_arise_pref";
	public static final String PREF_LASTUPDATE = "arise_lastupdate";
	
	public static final String PREF_LAST_SHOWN_TOOLTIP = "arise_last_shown_tooltip";

	public final static int AR_TYPE_BUTTON				= 1;
	public final static int AR_TYPE_FORM				= 2;
	public final static int AR_TYPE_BANNER				= 3;
	public final static int AR_TYPE_VIDEO				= 4;
	public final static int AR_TYPE_WEBSITE				= 5;
	public final static int AR_TYPE_3D      			= 6;
	public final static int AR_TYPE_OVERLAY_VIDEO  		= 7;
	public final static int AR_TYPE_OVERLAY_3D_MODEL	= 8;
	
	public final static int OPEN_BY_IN_APP_BROWSER		= 1;
	public final static int OPEN_BY_DEFAULT_BROWSER		= 2;
	
//	public static final String PACKAGE_NAME = "com.knx";
	public static final float ALPHA    = 0.4f;
	public static final String CHARSET = "UTF-8";
	
	public static String LANGUAGE = "en";
	public static final Set<String> SUPPORTLANGUAGE = new HashSet<String>(Arrays.asList(new String[] {"en", "vi", "th", "zh_CN", "zh_TW"}));
	
	public static String POPUP_INSTRUCTION_NOT_SHOW_FLAG = "arise_popup_instruction_not_show";
	public static String POPUP_INSTRUCTION_LAST_SHOW = "arise_popup_instruction_last_show";
	
	public static int DEVICE_WIDTH  = 0;
	public static int DEVICE_HEIGHT = 0;
	public static final String DEVICE_MODEL = Build.MODEL;
	
	public static String MOBILE_COUNTRY_CODE = "";
	public static String MOBILE_NETWORK_CODE = "";
	public static String MOBILE_NETWORK_OPERATOR_NAME = "";
	
	public static double LONGITUDE = -2000;
	public static double LATITUDE  = -2000;
	
	public static String CLIENT_CODE_KEY = "";
	
	public static int UPLOAD_IMAGE_WIDTH = 240;
	
	public static String	PACKAGE_NAME;
	public static String	SERVICE_BASE_URL;
	public static String	TRACKING_URL;
	public static String	TRACKING_APP_ID;
	public static String	LOGO;
	public static float		LOGO_ALPHA = 1;
	public static String	GUIDE_IMAGE;
	public static String	GUIDE_VIDEO_URL;
	public static String	TOOLTIP_IMAGE;
	public static String	TOOLTIP_TEXT;
	public static boolean	BARCODE_SCANNING_ENABLE = false;
	public static int		THEME_COLOR = Color.argb(255, 131, 13, 189);
	
	public static float FOV = 63f;
	
	public static final int MINIMUM_TIMEOUT					= 3000;
	public static final int SLOW_CONNECTION_THRESHOLD		= 9000;
	public static final int MAXIMUM_TIMEOUT					= 24000;
	
	/**
	 * Log configurations for ARise
	 */
	public static void logConfigs() {
		String tag = "ARiseConfigs";
		
		Log.i(tag, "-------- ARise configs --------");
		Log.i(tag, "\t+ Service base URL: " + SERVICE_BASE_URL);
		Log.i(tag, "\t+ Tracking URL: " + TRACKING_URL);
		Log.i(tag, "\t+ Tracking App ID: " + TRACKING_APP_ID);
		Log.i(tag, "\t+ Logo name: " + LOGO + " - alpha " + LOGO_ALPHA);
		Log.i(tag, "\t+ Guide image: " + GUIDE_IMAGE);
		Log.i(tag, "\t+ Guide video: " + GUIDE_VIDEO_URL);
		Log.i(tag, "\t+ Tooltip image: " + TOOLTIP_IMAGE);
		Log.i(tag, "\t+ Tooltip text: " + TOOLTIP_TEXT);
		Log.i(tag, "\t+ Theme color (rgba): " + Color.red(THEME_COLOR) + ", " + Color.green(THEME_COLOR) + ", " + Color.blue(THEME_COLOR) + ", " + Color.alpha(THEME_COLOR));
		Log.i(tag, "\t+ Language: " + LANGUAGE);
		Log.i(tag, "\t+ Use barcode scanning feature: " + BARCODE_SCANNING_ENABLE);
		Log.i(tag, "\t+ Client code key: " + CLIENT_CODE_KEY);
		Log.i(tag, "-------------------------------");
	}
}