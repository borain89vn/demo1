package com.knx.framework.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.knx.framework.main.Shared;

public class DownloadPoster extends AsyncTask<String, Void, Void> {

	private final String TAG = "ARiseDownloadPosterTask";
	
	private Context context;
	
	public DownloadPoster(Context cxt) {
		context = cxt;
	}
	
	@Override
	protected Void doInBackground(String... data) {
		String posterURL = data[0];
		downloadPoster(posterURL);
		return null;
	}
	
	protected void onPostExecute(Void unused) {
		
	}
	
	private void downloadPoster(String posterURL) {
	    try {
	        URL url = new URL(posterURL);
	        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	        urlConnection.setRequestMethod("GET");
	        urlConnection.setUseCaches(false);
	        urlConnection.setAllowUserInteraction(false);
	        urlConnection.connect();

	        InputStream inputStream = urlConnection.getInputStream();
	        
	    	File file = new File(Shared.getPosterDir(context) + "/" + posterURL.hashCode());
	    	
	    	if (!file.getParentFile().exists()) {
	    		file.getParentFile().mkdirs();
	    	}
	    	
	    	if (file.createNewFile()) {
	    		FileOutputStream fileOutput = new FileOutputStream(file);
		        byte[] buffer = new byte[1024];
		        int bufferLength = 0;
		        while ((bufferLength = inputStream.read(buffer)) > 0) {
		            fileOutput.write(buffer, 0, bufferLength);
		        }
		        fileOutput.close();
	    	}
	    } catch (MalformedURLException e) {
	    	Log.e(TAG, String.format("Error while downloading poster. Type: %s", "MalformedURLException"));
	    } catch (IOException e) {
	    	Log.e(TAG, String.format("Error while downloading poster. Type: %s", "IOException"));
	    } catch (Exception e) {
	    	Log.e(TAG, String.format("Error while downloading poster. Type: %s", "Unknown"));
	        e.printStackTrace();
	    }
	}
}