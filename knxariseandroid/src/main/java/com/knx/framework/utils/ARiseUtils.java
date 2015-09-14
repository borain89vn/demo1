package com.knx.framework.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.knx.framework.R;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.helper.LangPref;

public class ARiseUtils {
	
	private static final String TAG = "ARiseUtils";
	
	/********************************************************************
	 * This function is used for calculate the size of a folder
	 */
	public static long dirSize(File dir) {
		if (dir.exists()) {
			long result = 0;
			File[] fileList = dir.listFiles();
			for (File file : fileList) {
				if (file.isDirectory()) {
					result += dirSize(file);
				} else {
					result += file.length();
				}
			}
			return result;
		} else {
			return 0;
		}
	}
	
	/********************************************************************
	 * Convert a byte size into string format
	 * @param bytes	: the size in byte unit
	 * @return the formatted string
	 */
	public static String byteToString(long bytes) {
		DecimalFormat df = new DecimalFormat("0.00");
		double k = 1024d;
		String[] unit = {"Bytes", "KB", "MB", "GB", "TB"};
		if (bytes == 0) {
			return "Zero bytes";
		} else {
			int i = (int) Math.floor(Math.log(bytes) / Math.log(k));
			return df.format(bytes / Math.pow(k, i)) + " " + unit[i];
		}
	}
	
	/********************************************************************
	 * Delete a file or a folder and all children files
	 * @param bytes	: the size in byte unit
	 * @return the formatted string
	 */
	public static void deleteFileOrDirectory(File fileOrDir) {
		// if the file is a directory, delete all files contained inside first
		if (fileOrDir.isDirectory()) {
			for (File file : fileOrDir.listFiles()) {
				if (file.isDirectory()) {
					deleteFileOrDirectory(file);
				} else {
					boolean status = file.delete();
					if (!status) {
						Log.e(TAG, String.format("Error while delete file %s.", file.getName()));
					}
				}
			}
			boolean status = fileOrDir.delete();
			if (!status)
				Log.e(TAG, String.format("Error while delete folder %s.", fileOrDir.getName()));
		} else {
			boolean status = fileOrDir.delete();
			if (!status)				
				Log.e(TAG, String.format("Error while delete file %s.", fileOrDir.getName()));
		}
	}
	
	/********************************************************************
	 * Initializes the language for SDK from a locale abbreviation.
	 * Please refer to the locale abbreviation at
	 * <a href="http://www.opensource.apple.com/source/CF/CF-550.13/CFBundle_Resources.c">
	 * CFBundle_Resources.c</a>
	 * for __CFBundleLocaleAbbreviationsArray and __CFBundleLanguageAbbreviationsArray
	 * @param abbreviation	: the locale abbreviation to initialize the language
	 */
	public static void initLanguageWithString(Context context, String abbreviation) {
		ARiseConfigs.LANGUAGE = abbreviation;
		if (ARiseConfigs.LANGUAGE != null && ARiseConfigs.LANGUAGE != "" && ARiseConfigs.SUPPORTLANGUAGE.contains(ARiseConfigs.LANGUAGE)) {
			Configuration c = new Configuration(context.getResources().getConfiguration());
			Locale locale = new Locale("en");
			String[] abbreviationParts = abbreviation.split("_");
			switch (abbreviationParts.length) {
				case 1:
					locale = new Locale(abbreviationParts[0]);
					break;
				case 2:
					locale = new Locale(abbreviationParts[0], abbreviationParts[1]);
					break;
				case 3:
					locale = new Locale(abbreviationParts[0], abbreviationParts[1], abbreviationParts[2]);
					break;
				default:
					Log.e(TAG, "Invalid argument for language initialization " + abbreviation);	
			}
			c.locale = locale;
			context.getResources().updateConfiguration(c, context.getResources().getDisplayMetrics());
			initLanguage(context);
		} else {
			ARiseConfigs.LANGUAGE = "en";
		}
	}
	
	/********************************************************************
	 * Initializes the language, load string values for corresponding language.
	 * @param context	: the context used to get resources
	 */
	private static void initLanguage(Context context) {
		// Settings page
		LangPref.TXTSETTINGS			 = context.getResources().getString(R.string.txtSettings);
		LangPref.TXTVERSION				 = context.getResources().getString(R.string.txtVersion);
		LangPref.TXTABOUT				 = context.getResources().getString(R.string.txtAbout);
		LangPref.TXTCLEARDATA			 = context.getResources().getString(R.string.txtClearData);
    	LangPref.TXTCONFIRMCLEARDATA	 = context.getResources().getString(R.string.txtConfirmClearData);
		LangPref.TXTGUIDE				 = context.getResources().getString(R.string.txtGuide);
    	LangPref.TXTVIDEO				 = context.getResources().getString(R.string.txtVideo);

    	// History page
    	LangPref.TXTRECENTS				 = context.getResources().getString(R.string.txtRecents);
    	LangPref.TXTBOOKMARK			 = context.getResources().getString(R.string.txtBookmark);
    	LangPref.TXTBOOKMARKED			 = context.getResources().getString(R.string.txtBookmarked);
    	LangPref.TXTBOOKMARKING			 = context.getResources().getString(R.string.txtBookmarking);
    	LangPref.TXTCONFIRMCLEARBOOKMARK = context.getResources().getString(R.string.txtConfirmClearBookmark);
    	LangPref.TXTCONFIRMCLEARHISTORY	 = context.getResources().getString(R.string.txtConfirmClearHistory);
    	
    	LangPref.TXTLOADING				 = context.getResources().getString(R.string.txtLoading);
    	LangPref.TXTUPDATING			 = context.getResources().getString(R.string.txtUpdating);
    	
    	LangPref.TXTGPSDISABLE			 = context.getResources().getString(R.string.txtGPSDisable);
    	LangPref.TXTMSGENABLEGPS		 = context.getResources().getString(R.string.txtMsgEnableGPS);
    	LangPref.TXTYOUTUBEERROR		 = context.getResources().getString(R.string.txtYoutubeError);
    	LangPref.TXTYES					 = context.getResources().getString(R.string.txtYes);
    	LangPref.TXTNO					 = context.getResources().getString(R.string.txtNo);
    	LangPref.TXTSLOWCONNECTION		 = context.getResources().getString(R.string.txtSlowConnection);
    	LangPref.TXTSHOWING				 = context.getResources().getString(R.string.txtShowing);
    	LangPref.TXTITEM				 = context.getResources().getString(R.string.txtItem);
    	LangPref.TXTITEMS				 = context.getResources().getString(R.string.txtItems);
    	LangPref.TXTCLEAR				 = context.getResources().getString(R.string.txtClear);
    	LangPref.TXTDELETE				 = context.getResources().getString(R.string.txtDelete);
    	LangPref.TXTENABLE				 = context.getResources().getString(R.string.txtEnable);
    	LangPref.TXTNETWORKERROR		 = context.getResources().getString(R.string.txtNetworkError);
    	LangPref.TXTOPENCAMERAFAIL		 = context.getResources().getString(R.string.txtOpenCameraFail);
    	LangPref.TXTSELECTAR			 = context.getResources().getString(R.string.txtSelectAR);
    	LangPref.TXTPLEASEWAIT			 = context.getResources().getString(R.string.txtPleaseWait);
    	LangPref.TXTRUNINBACKGROUND		 = context.getResources().getString(R.string.txtRunInBackground);
    	LangPref.TXTCANCEL				 = context.getResources().getString(R.string.txtCancel);
    	
    	LangPref.TXTINSTRUCTIONTEXT		 = context.getResources().getString(R.string.txtInstructionText);
    	
    	LangPref.TXTARLISTTITLE			 = context.getResources().getString(R.string.txtARListTitle);
    	LangPref.TXTSDKNAME				 = context.getResources().getString(R.string.txtSDKName);
	}
	
	public static boolean performMD5ValidationOnFile(String absolutePath) {
		try {
			String filename = absolutePath.substring(absolutePath.lastIndexOf('/') + 1);
			String filenameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));
			FileInputStream in = new FileInputStream(absolutePath);
			
			MessageDigest md = MessageDigest.getInstance("MD5");
	
	        byte[] dataBytes = new byte[1024];
	
	        int nread = 0;
	        while ((nread = in.read(dataBytes)) != -1) {
	            md.update(dataBytes, 0, nread);
	        }
	        byte[] mdbytes = md.digest();
	        in.close();
	        //convert the byte to hex format method 1
	        StringBuffer sb = new StringBuffer();
	        for (int ic = 0; ic < mdbytes.length; ic++) {
	            sb.append(Integer.toString((mdbytes[ic] & 0xff) + 0x100, 16).substring(1));
	        }
	        String md5String = sb.toString();
	        if (md5String.equals(filenameWithoutExt)) {
	        	return true;
	        } else {
	        	Log.e(TAG, "MD5 validation failed for file: " + absolutePath);
	        	return false;
	        }
		} catch (IOException e) {
			Log.e(TAG, "Error occurs while performing MD5 validation on file: " + absolutePath, e);
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			Log.e(TAG, "Error occurs while performing MD5 validation on file: " + absolutePath, e);
			e.printStackTrace();
			return false;
		}
	}
	
	/********************************************************************
	 * Create an asset manager (libGDX) for external resources (using absolute path).
	 * @return	the external asset manager
	 */
	public static AssetManager createExternalAssetManager() {
		return new AssetManager(new FileHandleResolver() {
			public FileHandle resolve(String filename) {
				return Gdx.files.absolute(filename);
			}
		});
	}
	
	/********************************************************************
	 * Create an asset manager (libGDX) for internal resources (using relative path to asset folder).
	 * @return	the internal asset manager
	 */
	public static AssetManager createInternalAssetManager() {
		return new AssetManager(new FileHandleResolver() {
			public FileHandle resolve(String filename) {
				return Gdx.files.internal(filename);
			}
		});
	}
	
	/********************************************************************
	 * Resizes a bitmap from its own size into new size.
	 * @param bm		: the resized bitmap 
	 * @param newHeight	: the desired height
	 * @param newWidth	: the desired width
	 * @return	the bitmap after resizing; null if resizing process fails.
	 */
	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        try {
			int width = bm.getWidth();
	        int height = bm.getHeight();
	
	        float scaleWidth = ((float) newWidth) / width;
	        float scaleHeight = ((float) newHeight) / height;
	
	        Matrix matrix = new Matrix();
	        matrix.postScale(scaleWidth, scaleHeight);
	
	        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	        return resizedBitmap;
        } catch (Exception e) {
        	Log.e(TAG, "Error when resizing bitmap image", e);
        	e.printStackTrace();
        	return null;
        }
    }
	
	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}
	
	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}
	
	public static byte[] rotateCameraByteArray(byte[] bData, int bWidth, int bHeight) {
		byte[] rotatedData = new byte[bData.length];
		for (int y = 0; y < bHeight; y++) {
			for (int x = 0; x < bWidth; x++) {
				rotatedData[x * bHeight + bHeight - y - 1] = bData[x + y * bWidth];
			}
		}
		return rotatedData;
	}
	
	public static int getStatusBarHeightInContext(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
	
	public static boolean isYoutubeURL(String absoluteURL) {
		try {
			URL url = new URL(absoluteURL);
			if (url.getHost().equalsIgnoreCase("www.youtube.com")) {
				return true;
			} else {
				Log.i(TAG, "Not from youtube but: " + url.getHost());
				return false;
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error occurred while checking if this link is from youtube: " + absoluteURL);
			e.printStackTrace();
			return false;
		}
	}
	
	public static String extractYoutubeId(String youtubeURL) {
		return youtubeURL.substring(youtubeURL.lastIndexOf("/") + 1);
	}
}