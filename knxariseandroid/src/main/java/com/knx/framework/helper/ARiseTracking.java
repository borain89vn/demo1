package com.knx.framework.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

public class ARiseTracking {
	
	private static final String TAG = "ARiseTracking";
	
	private static final String IMPRESSION_TRACKING_METHOD = "pageview";
	private static final String INTERACTION_TRACKING_METHOD = "interaction";
	
    public static final int TRACKING_IMPRESSION = 10;
    public static final int TRACKING_INTERACTION = 20;    
    
    /**
     * One main method of this class, used to send an impression tracking.
     * @param context
     * @param baseURL
     * @param type
     * @param isnapId
     */
    public static void trackImpression(Context context, String baseURL, int type, String isnapId) {
    	String version			= getVersion(context);
        String deviceInfoBase64 = buildDeviceInfoBase64(context);
        
        // build impression tracking URL
        URL impressionTrackingUrl = null;
        try {
        	Log.i(TAG, "impression BaseURL: " + baseURL);
        	URL impressionTrackingServiceBaseUrl = new URL(baseURL);
        	Uri.Builder builder = new Uri.Builder().scheme(impressionTrackingServiceBaseUrl.getProtocol())
					.authority(impressionTrackingServiceBaseUrl.getHost());
        	
        	String[] pathSegments = impressionTrackingServiceBaseUrl.getPath().split("/"); 
        	for (int i = 0; i < pathSegments.length; i++) {
        		builder.appendPath(pathSegments[i]);
        	}
			builder.appendPath(IMPRESSION_TRACKING_METHOD);
			
			String fulUrlWithImpressionTrackingParams = builder.build().toString();
			
			fulUrlWithImpressionTrackingParams += "?id=" + URLEncoder.encode(ARiseConfigs.TRACKING_APP_ID, ARiseConfigs.CHARSET) + 
            		"&version=" + URLEncoder.encode(version, ARiseConfigs.CHARSET) + 
            		"&url=" + URLEncoder.encode(isnapId, ARiseConfigs.CHARSET) + 
            		"&extra=" + URLEncoder.encode(deviceInfoBase64, ARiseConfigs.CHARSET) + 
            		"&_=" + URLEncoder.encode(UUID.randomUUID().toString(), ARiseConfigs.CHARSET);
        	
        	impressionTrackingUrl = new URL(fulUrlWithImpressionTrackingParams);
        	
        } catch (MalformedURLException malformedURLException) {
			Log.e(TAG, "Error occurs while building impression tracking URL", malformedURLException);
			malformedURLException.printStackTrace();
			impressionTrackingUrl = null;
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			Log.e(TAG, "Error occurs while building impression tracking URL", unsupportedEncodingException);
			unsupportedEncodingException.printStackTrace();
			impressionTrackingUrl = null;
		}
        
        sendTracking(impressionTrackingUrl);
    }    

    /**
     * One main method of this class, used to send an interaction tracking.
     * @param context
     * @param baseURL
     * @param type
     * @param isnapId
     * @param trigger
     * @param object
     * @param targetURL
     */
    public static void trackInteraction(Context context, String baseURL, int type, String isnapId, String trigger,  String object, String targetURL) {
    	String version			= getVersion(context);
    	String deviceInfoBase64 = buildDeviceInfoBase64(context);
    	
    	// build impression tracking URL
        URL interactionTrackingUrl = null;
        try {
        	Log.i(TAG, "interaction BaseURL: " + baseURL);
        	URL interactionTrackingServiceBaseUrl = new URL(baseURL);
        	Uri.Builder builder = new Uri.Builder().scheme(interactionTrackingServiceBaseUrl.getProtocol())
					.authority(interactionTrackingServiceBaseUrl.getHost());
        	
        	String[] pathSegments = interactionTrackingServiceBaseUrl.getPath().split("/"); 
        	for (int i = 0; i < pathSegments.length; i++) {
        		builder.appendPath(pathSegments[i]);
        	}
			builder.appendPath(INTERACTION_TRACKING_METHOD);
			
			String fulUrlWithImpressionTrackingParams = builder.build().toString();
			
			fulUrlWithImpressionTrackingParams += "?id=" + URLEncoder.encode(ARiseConfigs.TRACKING_APP_ID, ARiseConfigs.CHARSET) + 
            		"&version=" + URLEncoder.encode(version, ARiseConfigs.CHARSET) + 
            		"&url=" + URLEncoder.encode(isnapId, ARiseConfigs.CHARSET) + 
            		"&extra=" + URLEncoder.encode(deviceInfoBase64, ARiseConfigs.CHARSET) + 
            		"&trigger=" + URLEncoder.encode(trigger, ARiseConfigs.CHARSET) + 
            		"&object=" + URLEncoder.encode(object, ARiseConfigs.CHARSET) + 
            		"&response=" + URLEncoder.encode(targetURL, ARiseConfigs.CHARSET) + 
            		"&_=" + URLEncoder.encode(UUID.randomUUID().toString(), ARiseConfigs.CHARSET);
        	
        	interactionTrackingUrl = new URL(fulUrlWithImpressionTrackingParams);
        	
        } catch (MalformedURLException malformedURLException) {
			Log.e(TAG, "Error occurs while building interaction tracking URL", malformedURLException);
			malformedURLException.printStackTrace();
			interactionTrackingUrl = null;
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			Log.e(TAG, "Error occurs while building interaction tracking URL", unsupportedEncodingException);
			unsupportedEncodingException.printStackTrace();
			interactionTrackingUrl = null;
		}
        
        sendTracking(interactionTrackingUrl);
    }
    
    /**
     * Helper method to send tracking request from the built tracking URL
     * @param builtTrackingURL The built URL for tracking request
     */
    private static void sendTracking(URL builtTrackingURL) {
    	if (builtTrackingURL != null) {
        	HttpURLConnection trackingConnection = null;
        	try {
            	trackingConnection = (HttpURLConnection) builtTrackingURL.openConnection();
            	InputStream is = trackingConnection.getInputStream();
            	BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            	String line;
            	StringBuffer responseString = new StringBuffer();
            	
            	while ((line = rd.readLine()) != null) {
            		responseString.append(line);
            		responseString.append('\r');
            	}
            	
            	rd.close();
            	is.close();
            	
            	Log.i(TAG, "======== TRACKING ========");
            	Log.i(TAG, "\tTracking request: "+ builtTrackingURL);
            	Log.i(TAG, "\tTracking response: " + responseString.toString());
            	Log.i(TAG, "==========================");
        	} catch (IOException ioe) {
        		Log.e(TAG, "Error when sending tracking", ioe);
        		ioe.printStackTrace();
        	} finally {
        		if (trackingConnection != null) {
        			trackingConnection.disconnect();
        		}
        	}
        }
    }
    
    /**
     * Helper method for building base64 string from device info.
     * @param context The context used to get some device info
     * @return The base64 string of device info, or empty string if error happens
     */
    private static String buildDeviceInfoBase64(Context context) {
    	try {
    		String version = getVersion(context);
            
    		// build JSON
    		JSONObject extras = new JSONObject();
            extras.put("version",			version);
            extras.put("mac",				(new DeviceUUIDFactory(context)).getDeviceUuid().toString());
            extras.put("w",					ARiseConfigs.DEVICE_WIDTH);
            extras.put("h",					ARiseConfigs.DEVICE_HEIGHT);
            extras.put("telcoName",			ARiseConfigs.MOBILE_NETWORK_OPERATOR_NAME);
            extras.put("telcoCountryCode",	ARiseConfigs.MOBILE_COUNTRY_CODE);
            extras.put("telcoNetworkCode",	ARiseConfigs.MOBILE_NETWORK_CODE);
            extras.put("model",				ARiseConfigs.DEVICE_MODEL);
            extras.put("longitude",			ARiseConfigs.LONGITUDE);
            extras.put("latitude",			ARiseConfigs.LATITUDE);
            extras.put("sdk",				"android-" + ARiseConfigs.SDK_VERSION);
            
            return Base64.encodeToString(extras.toString().getBytes(), Base64.URL_SAFE)
            		.replace("\n", "")
            		.replace("=", ""); // necessary?
    	} catch (JSONException jsonException) {
    		Log.e(TAG, "Error while building base64 from device info", jsonException);
    		jsonException.printStackTrace();
    		
    		return "";
    	}
    }
    
    /**
     * Helper method for getting current version
     * @param context
     * @return
     */
    private static String getVersion(Context context) {
    	PackageInfo pInfo;
    	String version = "";
    	try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Error while getting version name from PackageInfo", e);
			e.printStackTrace();
			version = "";
		} catch (NullPointerException e) {
			Log.e(TAG, "Error while getting version name from PackageInfo", e);
			e.printStackTrace();
			version = "";
		}
    	return version;
    }
}

// OLD CODE FOR BUILDING URL AND SENDING TRACKING
//String server = baseURL + "/pageview" + 
//"?id=" + URLEncoder.encode(Config.TRACKING_APP_ID, Config.CHARSET) + 
//"&version=" + URLEncoder.encode(version, Config.CHARSET) + 
//"&url=" + URLEncoder.encode(isnapId, Config.CHARSET) + 
//"&extra=" + URLEncoder.encode(base64String, Config.CHARSET) + 
//"&_=" + URLEncoder.encode(randomUUIDString, Config.CHARSET);
//
//URLConnection connection = new URL(server).openConnection();
//InputStream response = connection.getInputStream();
//Log.i(TAG, "trackImpression-->Response: " + response.toString());