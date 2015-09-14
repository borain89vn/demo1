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

import com.knx.framework.arcontents.StaticDataManager;
import com.knx.framework.main.Shared;

public class DownloadBackgroundImage extends AsyncTask<Void, Void, Void> {

	private final String TAG = "ARiseDownloadAssetTask";
	
	private Context context;
	
	private String backgroundImageURL;
	private String filename;
	private StaticDataManager staticDataManager;
	
	private HttpURLConnection urlConnection;
	
	public DownloadBackgroundImage(Context cxt, String url, StaticDataManager sdm) {
		context = cxt;
		backgroundImageURL = url;
		staticDataManager = sdm;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		downloadImageForBackground(backgroundImageURL);
		return null;
	}
	
	@Override
	protected void onPostExecute(Void unused) {
		final File downloadedFile = new File(Shared.getAssetDir(context) + "/" + filename);
		if (downloadedFile.exists()) {
			staticDataManager.setBackgroundPath(downloadedFile.getAbsolutePath());
			Log.i(TAG, "Finish download background");
		} else {
			staticDataManager.setBackgroundPath(null);
			Log.i(TAG, "Not Finish download background");
		}
	}
	
	private void downloadImageForBackground(String imgURL) {
		try {
			String path = (new URL(backgroundImageURL)).getPath();
			filename = path.substring(path.lastIndexOf("/") + 1);
			
			File file = new File(Shared.getAssetDir(context) + "/" + filename);
			
			Log.i(TAG, "Start downloading background image: " + imgURL);
			
	        URL url = new URL(imgURL);
	        urlConnection = (HttpURLConnection) url.openConnection();
	        urlConnection.setRequestMethod("GET");
	        urlConnection.setUseCaches(false);
	        urlConnection.setAllowUserInteraction(false);
	        urlConnection.connect();

	        InputStream inputStream = urlConnection.getInputStream();
            
            file.createNewFile();
            
            FileOutputStream fileOutput = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer

            //now, read through the input buffer and write the contents to the file
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            fileOutput.close();
	    } catch (MalformedURLException e) {
	        Log.e(TAG, String.format("Error while downloading asset image. Type: %s", "MalformedURLException"));
	    } catch (IOException e) {
	    	Log.e(TAG, String.format("Error while downloading asset image. Type: %s", "IOException"));
	    } catch (Exception e) {
	    	Log.e(TAG, String.format("Error while downloading asset image. Type: %s", "Unknown"));
	    	e.printStackTrace();
	    } finally {
	    	if (urlConnection != null) {
	    		urlConnection.disconnect();
	    	}
	    }
	}
}
