/**
 * Copyright @ Timon Trinh
 * Website http://trinhquochungnotes.blogspot.com
 */
package com.handmark.pulltorefresh.library.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * @author Timon Trinh
 *
 */
public class GKIMLog {
	
	public static final boolean DEBUG_ON = false;
	private static final boolean DEBUG_LOGFILE = false;
	private static final String TAG = "TimonLabs";
	private static final String LABS_LOGFILE = "Timonlog.txt";
	private static String mPathFile;
	private static File mLogFile;
	private static int mCanWriteToFile = -1;
	private static Context mLastContext = null;

	/**
	 * Write log in mode. All will be turn off if DEBUG_ON is false.
	 * @param mode
	 * 			- 0 - debug log	(Log.d)
	 * 			- 1 - information log (Log.i)
	 * 			- 2 - warning log (Log.w)
	 * 			- 3 - error log	(Log.e)
	 * @param message
	 */
	public static void l(int mode, String message) {
		if (DEBUG_ON) {
			switch (mode) {
			case 4: 
				Log.e(TAG, message);
				break;
			case 3:
				Log.w(TAG, message);
				break;
			case 2:
				Log.i(TAG, message);
				break;
			case 1:
				Log.d(TAG, message);
				break;
			default:
				Log.v(TAG, message);
				break;
			}
		}
	}
	
	/**
	 * @Description: Write log Android log mode and specified log file. (GKIM_LOGFILE) 
	 * @param context
	 * @param mode
	 * @param message
	 */
	public static void lf(Context context, int mode, String message) {
		l(mode, message);
		if (DEBUG_LOGFILE) {
			if (mLogFile == null) {
				if (mCanWriteToFile  == -1) {
					String state = Environment.getExternalStorageState();
					if (state.equals(Environment.MEDIA_MOUNTED)) {
						mCanWriteToFile = 1;
						mPathFile = Environment.getExternalStorageDirectory() + File.separator + TAG;
					}else {
						if (context != null) {
							mLastContext = context;
							mCanWriteToFile = 2;
							mPathFile = context.getCacheDir() + File.separator + TAG;
						}else if (mLastContext != null) {
							mCanWriteToFile = 2;
							mPathFile = mLastContext.getCacheDir() + File.separator + TAG;
						}else {
							mCanWriteToFile = 0;
						}
					}
					File dextrFolder = new File(mPathFile);
					if (!dextrFolder.exists()) {
						dextrFolder.mkdir();
					}
					mPathFile += File.separator + LABS_LOGFILE;
				}
			}
			
			if (mCanWriteToFile > 0 ) {
				Log.v(TAG, "Writing log into(" + mCanWriteToFile + "): " + mPathFile);
				try {
					if (mLogFile == null) {
						mLogFile = new File(mPathFile);
					}
					BufferedWriter bw = new BufferedWriter(new FileWriter(mLogFile, true));
					if (bw != null) {
						String messageout = (String) DateFormat.format(((CharSequence) "MM/dd/yy hh:mm:ssaa"), System.currentTimeMillis()) + " " + message;
						messageout += "\r\n";
						bw.write(messageout);
						bw.flush();
					}
					bw.close();
				} catch (IOException e) {
					Log.e(TAG, "Logger has failed to write: " + message + " into file with error: " + e.getMessage());
				}
			}
		}
	}
}
