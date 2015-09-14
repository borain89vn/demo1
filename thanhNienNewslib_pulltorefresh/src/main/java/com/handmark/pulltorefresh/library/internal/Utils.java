package com.handmark.pulltorefresh.library.internal;

public class Utils {

	static final String LOG_TAG = "Utils";

	public static void warnDeprecation(String depreacted, String replacement) {
		GKIMLog.lf(null, 0, LOG_TAG+"=>warnDeprecation: You're using the deprecated " + depreacted + " attr, please switch over to " + replacement);
	}

}
