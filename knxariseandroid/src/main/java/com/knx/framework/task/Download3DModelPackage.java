package com.knx.framework.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.util.Log;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import com.knx.framework.main.Shared;

public class Download3DModelPackage implements Runnable {
	
	private final String TAG = "ARiseDownload3DModelPackage";
	
	private Context context;
	
	private String assetURL;
	
	public Download3DModelPackage(Context cxt, String url) {
		context = cxt;
		assetURL = url;
	}

	@Override
	public void run() {
		try {
			// download model package
			URL url = new URL(assetURL);
	        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	        urlConnection.setConnectTimeout(10000);
	        urlConnection.setRequestMethod("GET");
	        urlConnection.setUseCaches(false);
	        urlConnection.setAllowUserInteraction(false);
	        urlConnection.connect();
	        
	        InputStream inputStream = urlConnection.getInputStream();
	        
	        // location set up
	        String model3DName = (new File(assetURL)).getName();
	        model3DName = model3DName.substring(0, model3DName.length() - 7);
            String rootResourcePath = Shared.getAssetDir(context) + "/" + model3DName;
            (new File(rootResourcePath)).mkdirs();
            
            (new File(rootResourcePath + "/" + model3DName)).mkdirs();
            
            // extraction
            TarInputStream tis = new TarInputStream(new GZIPInputStream(inputStream));
            TarEntry tarEntry;
            while ((tarEntry = tis.getNextEntry()) != null) {
                String resourcePath = rootResourcePath + "/" + tarEntry.getName();
                if (tarEntry.isDirectory()) {
                	Log.i(TAG, "Dir entry: " + tarEntry.getName());
                    (new File(resourcePath)).mkdirs();
                } else {
                	Log.i(TAG, "File entry: " + tarEntry.getName());
                    tis.copyEntryContents(new FileOutputStream(new File(resourcePath)));
                }
            }
            tis.close();
	    } catch (MalformedURLException e) {
	    	Log.e(TAG, String.format("Error while downloading asset 3D model. Type: %s", "MalformedURLException"));
	    } catch (IOException e) {
	    	Log.e(TAG, String.format("Error while downloading asset 3D model. Type: %s", "IOException"));
	    } catch (Exception e) {
	    	Log.e(TAG, String.format("Error while downloading asset 3D model. Type: %s", "Unknown"));
	    	e.printStackTrace();
	    }
	}
}