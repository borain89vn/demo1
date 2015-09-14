package com.knx.framework.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.main.Shared;

public class DownloadAssetImage extends AsyncTask<HashMap<String, String>, Void, Void> {

	private final String TAG = "ARiseDownloadAssetTask";
	
	private Context context;
	
	private String assetFilename;
	private HttpURLConnection urlConnection;
	private ImageView imgViewForAsset;
	
	/**
	 * Constructor
	 */
	public DownloadAssetImage(Context cxt, ImageView imgView) {
		context = cxt;
		imgViewForAsset = imgView;
	}
	
	@Override
	protected Void doInBackground(HashMap<String, String>... data) {
		HashMap<String, String> asset = data[0];
        String assetURL = asset.get("assetURL");
        int type = Integer.parseInt(asset.get("type"));
        downloadAsset(type, assetURL, imgViewForAsset);
		return null;
	}
	
	protected void onPostExecute(Void unused) {
		final File downloadedFile = new File(Shared.getAssetDir(context) + "/" + assetFilename);
		if (downloadedFile.exists()) {
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					Log.i(TAG, "Load image into asset");
					Bitmap bitmap = BitmapFactory.decodeFile(downloadedFile.getPath());
					imgViewForAsset.setImageBitmap(bitmap);
				}
			});
		} else {
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					Log.i(TAG, "Image does not exist. Hide asset");
					imgViewForAsset.setVisibility(View.GONE);
				}
			});
		}
	}
	
	private void downloadAsset(int type, String assetURL, ImageView imgView) {
		try {
			assetFilename = (new File(assetURL)).getName();
			
			// type validation
			if (type != ARiseConfigs.AR_TYPE_BANNER && type != ARiseConfigs.AR_TYPE_BUTTON && type != ARiseConfigs.AR_TYPE_VIDEO) return;
			
			Log.i(TAG, "Start downloading asset image: " + assetURL);
			
	        URL url = new URL(assetURL);
	        urlConnection = (HttpURLConnection) url.openConnection();
	        urlConnection.setRequestMethod("GET");
	        urlConnection.setUseCaches(false);
	        urlConnection.setAllowUserInteraction(false);
	        urlConnection.connect();

	        InputStream inputStream = urlConnection.getInputStream();

        	File file = new File(Shared.getAssetDir(context) + "/" + assetFilename);
            
            if (!file.exists())
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
