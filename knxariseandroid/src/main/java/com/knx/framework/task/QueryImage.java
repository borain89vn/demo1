package com.knx.framework.task;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.main.CameraActivity;
import com.knx.framework.main.RecognitionTokenGenerator;

public class QueryImage implements Runnable {
    
	private final String TAG = "ARiseQueryImageTask";
	
	private Context context;
	private int timeout = ARiseConfigs.MINIMUM_TIMEOUT;
    private Bitmap screenshot;
    
    private String recognitionToken = "";
    
    public QueryImage(Context cxt, Bitmap bitmap, int t) {
    	context = cxt;
        timeout = t;
        screenshot = bitmap;
        
        recognitionToken = RecognitionTokenGenerator.getCurrentSession().increaseAndGet();
    }

    public void run() {
    	
        // make full URL including query params for Image Recognition
    	URL imageRecognitionURL = null;
        try {
			URL serviceBaseURL = new URL(ARiseConfigs.SERVICE_BASE_URL);
			Uri.Builder builder = new Uri.Builder().scheme(serviceBaseURL.getProtocol())
					.authority(serviceBaseURL.getHost());
			String[] pathSegments = serviceBaseURL.getPath().split("/"); 
        	for (int i = 0; i < pathSegments.length; i++) {
        		builder.appendPath(pathSegments[i]);
        	}
        	builder.appendPath("QueryRemote");
			
			String fullUrlWithQueryParams = builder.build().toString();
			fullUrlWithQueryParams += "?w=" + URLEncoder.encode(String.valueOf(ARiseConfigs.DEVICE_WIDTH), ARiseConfigs.CHARSET) +
					"&h=" + URLEncoder.encode(String.valueOf(ARiseConfigs.DEVICE_HEIGHT), ARiseConfigs.CHARSET) +
					"&telcoName=" + URLEncoder.encode(ARiseConfigs.MOBILE_NETWORK_OPERATOR_NAME, ARiseConfigs.CHARSET) +
					"&telcoCountryCode=" + URLEncoder.encode(ARiseConfigs.MOBILE_COUNTRY_CODE, ARiseConfigs.CHARSET) +
					"&telcoNetworkCode=" + URLEncoder.encode(ARiseConfigs.MOBILE_NETWORK_CODE, ARiseConfigs.CHARSET) +
					"&model=" + URLEncoder.encode(ARiseConfigs.DEVICE_MODEL, ARiseConfigs.CHARSET) +
					"&longitude=" + URLEncoder.encode(String.valueOf(ARiseConfigs.LONGITUDE), ARiseConfigs.CHARSET) +
					"&latitude=" + URLEncoder.encode(String.valueOf(ARiseConfigs.LATITUDE), ARiseConfigs.CHARSET) +
					"&sdk=" + URLEncoder.encode("android-" + ARiseConfigs.SDK_VERSION, ARiseConfigs.CHARSET) +
					"&cameraType=" + URLEncoder.encode("63", ARiseConfigs.CHARSET) +
					"&isFOV=" + URLEncoder.encode(String.valueOf(true), ARiseConfigs.CHARSET) +
					"&recognitionToken=" + URLEncoder.encode(recognitionToken, ARiseConfigs.CHARSET);
			
			// client code
			// pass the client code to server for getting private poster
			// used in S2T and D2L app
			SharedPreferences prefs = context.getApplicationContext().getSharedPreferences("KnorexPref", Context.MODE_PRIVATE);
			if (prefs.contains(ARiseConfigs.CLIENT_CODE_KEY)) {
				String clientCode = prefs.getString(ARiseConfigs.CLIENT_CODE_KEY, "");
				Log.i(TAG, "Found client code: " + clientCode);
				if (clientCode != null && clientCode.length() > 0) {
					fullUrlWithQueryParams += "&clientCode=" + URLEncoder.encode(clientCode, ARiseConfigs.CHARSET);
				}
			}
			
			imageRecognitionURL = new URL(fullUrlWithQueryParams);
			
			Log.i(TAG, "Full url with query params: " + fullUrlWithQueryParams);
			
		} catch (MalformedURLException malformedURLException) {
			Log.e(TAG, "Error occurs while extracting service base URL for image recognition", malformedURLException);
			malformedURLException.printStackTrace();
			imageRecognitionURL = null;
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			Log.e(TAG, "Error occurs while extracting service base URL for image recognition", unsupportedEncodingException);
			unsupportedEncodingException.printStackTrace();
			imageRecognitionURL = null;
		}
        
        // server response will be stored here
    	String sResponse = null;
        
        // start requesting server
        if (imageRecognitionURL != null) {
        	String boundary = "KNXNK";
        	HttpURLConnection connection = null;
            try {
            	
            	// note the start time
            	long start = System.currentTimeMillis();
            	
            	connection = (HttpURLConnection) imageRecognitionURL.openConnection();
                
            	ByteArrayOutputStream bos = new ByteArrayOutputStream();
                screenshot.compress(Bitmap.CompressFormat.JPEG, 50, bos);
                byte[] data = bos.toByteArray();

                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes("--" + boundary + "\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"myFile\";filename=\"capturedFrame.jpg\"" + "\r\n");
                outputStream.writeBytes("Content-Type: application/octet-stream\r\n");
                outputStream.writeBytes("Content-Length: " + data.length + "\r\n");
                outputStream.writeBytes("\r\n");
                outputStream.write(data);
                outputStream.writeBytes("\r\n");
                outputStream.writeBytes("--" + boundary + "--\r\n");
                outputStream.flush();
                outputStream.close();
                
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();

                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                is.close();
                sResponse = response.toString();
                
                long end = System.currentTimeMillis();
                
                DecimalFormat df = new DecimalFormat("00000");
            	
            	// only process server response if it's not null and not empty
                if (sResponse == null) {
                	Log.i(TAG, String.format("Time: %s ms. Null server response!", df.format(end-start)));
                } else if (sResponse.length() == 0) {
                	Log.i(TAG, String.format("Time: %s ms. Empty server response!", df.format(end-start)));
                } else if (sResponse.length() > 0) {
                	Log.i(TAG, String.format("Time: %s ms. Response: %s", df.format(end-start), sResponse));
//                	((CameraActivity) context).preprocessJSONResponse(sResponse);
                	((CameraActivity) context).preprocessJSONResponse(sResponse, start, end, recognitionToken, timeout);
                }
            } catch (SocketTimeoutException e) {
            	Log.e(TAG, String.format("Error while querying image to server. Type: %s", "SocketTimeoutException"), e);
            	e.printStackTrace();
            	
            	// do nothing
            	
            } catch (ConnectException e) {
            	Log.e(TAG, String.format("Error while querying image to server. Type: %s", "ConnectException"), e);
            	e.printStackTrace();

            	// do nothing
            	
            } catch (Exception e) {
            	Log.e(TAG, String.format("Error while querying image to server. Type: %s", "Unknown"), e);
                e.printStackTrace();
                
             // do nothing
                
            } finally {
            	screenshot.recycle();
            	if (connection != null) {
            		connection.disconnect();
            	}
            }
        }
    }
}