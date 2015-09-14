package com.knx.framework.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

/**
 * Created with IntelliJ IDEA. User: Tam
 * Date: 19/12/12 Time: 1:13 AM To change
 * this template use File | Settings | File Templates.
 */
public class HttpClient {
	public static final String TAG = "HttpClient";

	public String get(String url) throws Exception {
		String jsonString = null;
		HttpURLConnection tc = null;
		try {
			URL newsURL = new URL(url);
			tc = (HttpURLConnection) newsURL.openConnection();
			tc.setRequestMethod("GET");
			// tc.setRequestProperty("User-agent",
			// tc.getRequestProperty("User-agent") + ";" + USER_AGENT);
			BufferedReader in = new BufferedReader(new InputStreamReader(tc.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}// end while

			in.close();
			jsonString = sb.toString();
			return jsonString;
		} catch (Exception e) {
			Log.e(TAG, "get:" + url, e);
			throw e;
		}
	}

}
