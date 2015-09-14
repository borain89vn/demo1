/**
 * File: DataDownloader.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 06-11-2012
 * 
 */
package com.gkxim.android.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;

/**
 * @author Timon Trinh
 */
public class DataDownloader {
	private static final String TAG = "DataDownloader";
	// private static final int POST_COMPLETED_DELAY = 100;
	public static final boolean DEBUG = GKIMLog.LOCAL_TEST_ON;
	public static final String HTTPMETHOD_GET = "GET";
	public static final String HTTPMETHOD_POST = "POST";
	private static int REQUEST_TIMEOUT = 30000;

	private boolean mExitTasksEarly = false;
	protected boolean mPauseWork = false;
	private static String sHTTPMethod = HTTPMETHOD_GET;
	private final Object mPauseWorkLock = new Object();

	// Dual thread executor for main AsyncTask
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "DataDownloader#" + mCount.getAndIncrement());
		}
	};
	public static final Executor DUAL_THREAD_EXECUTOR = Executors
			.newFixedThreadPool(2, sThreadFactory);

	private AsynDownloadTask task = null;
	private OnDownloadCompletedListener listener = null;
	private String[] mPostFiles;
	private boolean mHasPostFiles = false;

	/**
	 * @Description: setup default method for DataDownloader utility.
	 * @param method
	 *            0 is GET, others is POST
	 */
	public static void setHTTPDefaultMethod(String method) {
		sHTTPMethod = method;
	}

	public DataDownloader(OnDownloadCompletedListener pListener) {
		listener = pListener;
		mHasPostFiles = false;
	}

	public void setPauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
			mPauseWork = pauseWork;
			if (!mPauseWork) {
				mPauseWorkLock.notifyAll();
			}
		}
	}

	/**
	 * Setting this to true will signal the working tasks to exit processing at
	 * the next chance. This helps finish up pending work when the activity is
	 * no longer in the foreground and completing the tasks is no longer useful.
	 * 
	 * @param exitTasksEarly
	 */
	public void setExitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly = exitTasksEarly;
	}

	/**
	 * AsynDownloadTask for download JSON data.<br>
	 * //XXX: if you do need to have a serialized tasks, then you should create
	 * them as in "..params"
	 */
	public class AsynDownloadTask extends AsyncTask<Object, Integer, String> {
		private static final String TAG = "AsynDownloadTask";
		// private static final int POST_COMPLETED_DELAY = 100;

		private Object dataIn;

		@Override
		protected void onPreExecute() {
			// if (!TNPreferenceManager.isConnectionAvailable()) {
			// GKIMLog.lf(
			// null,
			// 5,
			// TAG
			// +
			// "=> onPreExecute: canceled due to Connnection is NOT AVAILABLE");
			//
			// this.cancel(true);
			// }
			super.onPreExecute();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(Object... params) {
			dataIn = params[0];
			String dataString = String.valueOf(dataIn);

			GKIMLog.l(0, TAG + "=> doInBackground - starting work with: "
					+ dataString);
			String result = null;

			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			// process method (as implemented by a subclass)
			if (result == null && !isCancelled() && !mExitTasksEarly) {
				if (!mHasPostFiles) {
					result = processingDownload(params[0]);
				} else {
					result = processingPostFile(params[0], mPostFiles);
				}
			}
			int resultlength = 0;
			if (result != null) {
				resultlength = result.length();
			}
			GKIMLog.l(0, TAG
					+ "=> doInBackground - finished work, result length= "
					+ resultlength);
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (isCancelled() || mExitTasksEarly) {
				result = null;
			}
			GKIMLog.lf(null, 0, TAG + "=>onPostExecute");
			if (listener != null) {
				listener.onCompleted(dataIn, result);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			synchronized (mPauseWorkLock) {
				mPauseWorkLock.notifyAll();
			}
		}

	}

	public interface OnDownloadCompletedListener {
		void onCompleted(Object key, String result);

		String doInBackgroundDebug(Object... params);
	}

	public String processingDownload(Object key) {
		if (key == null) {
			return null;
		}
		String result = null;
		RequestData reqData = (RequestData) key;
		// check from cache and expired time
		String strURL = reqData.getURLString();
		String keyCacher = reqData.getKeyCacher();
		GKIMLog.l(1, TAG + " keyCacher  TNPreferenceManager strURL :" + strURL
				+ " keyCacher:" + keyCacher);
		// boolean bCheckCache = TNPreferenceManager.checkCache(strURL);
		boolean bCheckCache = false;
		if (keyCacher != null && keyCacher != "") {
			bCheckCache = TNPreferenceManager.checkCache(keyCacher);
		}
		GKIMLog.lf(null, 1, TAG + "=> processingDownload cache : bCheckCache :"
				+ bCheckCache + " reqData.forceUpdate:" + reqData.forceUpdate);
		if (bCheckCache) {
			if (TNPreferenceManager.isConnectionAvailable()
					&& reqData.forceUpdate) {
				GKIMLog.lf(null, 1, TAG + "=> processingDownload: " + strURL);
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					if (doRequestToStream(reqData, baos)) {
						if (baos.size() > 0) {
							result = baos.toString("utf-8");
						}
					}
				} catch (UnsupportedEncodingException e) {
					GKIMLog.lf(
							null,
							4,
							TAG + "=> UnsupportedEncodingException: "
									+ e.getMessage());
				} catch (Exception e) {
					GKIMLog.lf(null, 4, TAG + "=> Exception: " + e.getMessage());
				}
			} else {
				// result = TNPreferenceManager.getContentFromCache(strURL);
				GKIMLog.lf(null, 1, TAG + "=> trying from cache for (" + strURL
						+ ")=" + result);
				result = TNPreferenceManager.getContentFromCache(reqData
						.getKeyCacher());
				if (result == null || result.length() <= 0) {
					try {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						if (doRequestToStream(reqData, baos)) {
							if (baos.size() > 0) {
								result = baos.toString("utf-8");
							}
						}
					} catch (UnsupportedEncodingException e) {
						GKIMLog.lf(
								null,
								4,
								TAG + "=> UnsupportedEncodingException: "
										+ e.getMessage());
					} catch (Exception e) {
						GKIMLog.lf(null, 4,
								TAG + "=> Exception: " + e.getMessage());
					}
				}
			}

		} else {
			if (TNPreferenceManager.isConnectionAvailable()) {
				GKIMLog.lf(null, 1, TAG + "=> processingDownload: " + strURL);
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					if (doRequestToStream(reqData, baos)) {
						if (baos.size() > 0) {
							result = baos.toString("utf-8");
						}
					}
				} catch (UnsupportedEncodingException e) {
					GKIMLog.lf(
							null,
							4,
							TAG + "=> UnsupportedEncodingException: "
									+ e.getMessage());
				} catch (Exception e) {
					GKIMLog.lf(null, 4, TAG + "=> Exception: " + e.getMessage());
				}
			} else {
				GKIMLog.l(3, TAG + "=> Don't have connection NULLLLLLLLLLLLL");
				return null;
			}
		}
		return result;
	}

	public String processingPostFile(Object key, String[] postfiles) {
		if (key == null) {
			return null;
		}
		String result = null;
		RequestData reqData = (RequestData) key;
		GKIMLog.lf(null, 1, TAG + "=> posting: " + reqData.getURLString());

		HttpURLConnection urlConnection = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		try {
			final URL url = new URL(reqData.getURLString());
			urlConnection = (HttpURLConnection) url.openConnection();
			if (HTTPMETHOD_POST.equalsIgnoreCase(reqData.method)) {
				String BOUNDARY = "" + System.currentTimeMillis();
				urlConnection.setDoOutput(true);
				urlConnection.setDoInput(true);
				urlConnection.setUseCaches(false);
				urlConnection.setRequestMethod(HTTPMETHOD_POST);
				urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
				urlConnection.setRequestProperty("Connection", "Keep-Alive");
				urlConnection.setRequestProperty("Content-Type",
						"multipart/form-data;charset=utf-8; boundary="
								+ BOUNDARY);
				ByteArrayOutputStream bufer = new ByteArrayOutputStream();
				DataOutputStream dataOut = new DataOutputStream(
						urlConnection.getOutputStream());
				try {
					FileInputStream afis = null;
					File afile = null;
					byte[] buf = new byte[512];
					for (String strFilepath : postfiles) {
						afile = new File(strFilepath);
						if (afile.exists() && afile.isFile()) {
							GKIMLog.lf(null, 0, TAG + "=> sending file: "
									+ strFilepath);
							dataOut.writeBytes("--" + BOUNDARY + "\r\n");
							if (!isVideo) {
								dataOut.writeBytes(String
										.format("Content-Disposition: form-data; name=\"attachments[]\"; filename=\"%1s\"\r\n",
												afile.getAbsolutePath()));
							} else {
								dataOut.writeBytes(String
										.format("Content-Disposition: form-data; name=\"attachments\"; filename=\"%1s\"\r\n",
												afile.getAbsolutePath()));
								dataOut.writeBytes("Content-Type: video/mp4");
								isVideo = false;
							}
							dataOut.writeBytes(String.format(
									"Content-Type: %1s\r\n\r\n",
									TNPreferenceManager.getFileType(afile)));
							afis = new FileInputStream(afile);
							for (int readNum; (readNum = afis.read(buf)) != -1;) {
								dataOut.write(buf, 0, readNum); // no doubt here
							}
							dataOut.writeBytes("\r\n");
						} else {
							GKIMLog.lf(null, 0, TAG
									+ "=>processingPostFile: file "
									+ strFilepath
									+ " doesn't existed or not a file.");
						}
					}
					dataOut.writeBytes("--" + BOUNDARY + "\r\n");
				} catch (IOException ex) {
					ex.printStackTrace();
				} finally {
					if (bufer != null) {
						try {
							bufer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			int responseCode = urlConnection.getResponseCode();
			if (responseCode != 200) {
				GKIMLog.l(4, TAG + "=> response code:" + responseCode);
				return null;
			}
			in = new BufferedInputStream(urlConnection.getInputStream());
			out = new BufferedOutputStream(outputStream);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			out.flush();
			result = outputStream.toString();
		} catch (final IOException e) {
			GKIMLog.l(4, TAG + "=> Error in processingPostFile - " + e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
				GKIMLog.l(4, TAG + "=> Error in closing connection - " + e);
			}
		}
		return result;
	}

	/**
	 * Download a JSON String from a RequestData object and write the content to
	 * an output stream.
	 * 
	 * @param RequestData
	 *            key
	 * @param outputStream
	 *            The outputStream to write to
	 * @return true if successful, false otherwise
	 */
	private boolean doRequestToStream(RequestData key, OutputStream outputStream) {
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		try {
			final URL url = new URL(key.host);
			urlConnection = (HttpURLConnection) url.openConnection();
			if (HTTPMETHOD_POST.equalsIgnoreCase(sHTTPMethod)) {
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod(HTTPMETHOD_POST);
				urlConnection.setReadTimeout(REQUEST_TIMEOUT);
				OutputStream os = urlConnection.getOutputStream();
				os.write(key.params.getBytes("utf-8"));
				os.flush();
				os.close();
			}

			in = new BufferedInputStream(urlConnection.getInputStream());
			out = new BufferedOutputStream(outputStream);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			out.flush();
			return true;
		} catch (final IOException e) {
			GKIMLog.l(4, TAG + "=> Error in doRequestToStream - " + e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
				GKIMLog.l(4, TAG + "=> Error in closing connection - " + e);
			}
		}
		return false;
	}

	/**
	 * Download a JSON String from a URL and write the content to an output
	 * stream.
	 * 
	 * @param urlString
	 *            The URL to fetch
	 * @param outputStream
	 *            The outputStream to write to
	 * @return true if successful, false otherwise
	 */
	public boolean downloadUrlToStream(String urlString,
			OutputStream outputStream) {
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		try {
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream());
			out = new BufferedOutputStream(outputStream);

			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			out.flush();
			return true;
		} catch (final IOException e) {
			GKIMLog.l(4, TAG + "=> Error in download - " + e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
			}
		}
		return false;
	}

	public void addDownload(RequestData contentData) {
		task = new AsynDownloadTask();

		if (UIUtils.hasHoneycomb()) {
			task.executeOnExecutor(DUAL_THREAD_EXECUTOR, contentData);
		} else {
			task.execute(contentData);
		}
	}

	public void ExitTask() {
		if (task != null) {
			task.cancel(true);
			task = null;
		}
	}

	public void addDownload(boolean forceUpdate, RequestData contentData) {
		if (contentData != null) {
			contentData.forceUpdate = forceUpdate;
		}
		addDownload(contentData);
	}

	public void addDownload(RequestData... contentDatas) {
		if (contentDatas == null || contentDatas.length < 1) {
			return;
		}
		task = new AsynDownloadTask();
		if (UIUtils.hasHoneycomb()) {
			task.executeOnExecutor(DUAL_THREAD_EXECUTOR,
					(Object[]) contentDatas);
		} else {
			task.execute((Object[]) contentDatas);
		}
	}

	public void addPOSTFiles(RequestData contentData, String[] arrayFilePaths) {
		if (arrayFilePaths == null || arrayFilePaths.length <= 0) {
			addDownload(contentData);
			return;
		}
		mPostFiles = new String[arrayFilePaths.length];
		mHasPostFiles = true;
		System.arraycopy(arrayFilePaths, 0, mPostFiles, 0,
				arrayFilePaths.length);
		task = new AsynDownloadTask();
		if (UIUtils.hasHoneycomb()) {
			task.executeOnExecutor(DUAL_THREAD_EXECUTOR, contentData);
		} else {
			task.execute(contentData);
		}
	}

	private boolean isVideo;

	public void addPOSTFileVideos(final RequestData contentData,
			final String[] arrayFilePaths) {
		isVideo = true;
		if (arrayFilePaths == null || arrayFilePaths.length <= 0) {
			addDownload(contentData);
			return;
		}
		mPostFiles = new String[arrayFilePaths.length];
		mHasPostFiles = true;
		System.arraycopy(arrayFilePaths, 0, mPostFiles, 0,
				arrayFilePaths.length);
		task = new AsynDownloadTask();
		if (UIUtils.hasHoneycomb()) {
			task.executeOnExecutor(DUAL_THREAD_EXECUTOR, contentData);
		} else {
			task.execute(contentData);
		}
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				BigFileUpload bigFileUpload = new BigFileUpload();
//				bigFileUpload.sendFileToServer(arrayFilePaths[0], contentData.getURLString(), "abcd.mp4");
//			}
//		}).start();
		
	
	}
	
	
	public class BigFileUpload {

		public static final String TAG = "filevideoupload";
		private static final char PARAMETER_DELIMITER = '&';
		private static final char PARAMETER_EQUALS_CHAR = '=';

		public String sendFileToServer(String filename, String targetUrl, String videoName)
		{
		    String response = "error";
		    String postParameters;
		    Log.e(TAG, filename);
		    Log.e(TAG, targetUrl);

		    long start = System.currentTimeMillis();
		    HttpURLConnection connection = null;
		    DataOutputStream outputStream = null;
		    // DataInputStream inputStream = null;

		    String pathToOurFile = filename;
		    String urlServer = targetUrl;
		    String lineEnd = "\r\n";
		    String twoHyphens = "--";
		    String boundary = "*****";
		    // DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");

		    int bytesRead, bytesAvailable, bufferSize;
		    byte[] buffer;
		    int maxBufferSize = 1 * 1024;

		    try
		    {
		        FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile));

		        URL url = new URL(urlServer);
		        connection = (HttpURLConnection) url.openConnection();

		        // Allow Inputs & Outputs
		        connection.setDoInput(true);
		        connection.setDoOutput(true);
		        connection.setUseCaches(false);
		        connection.setChunkedStreamingMode(1024);

		        connection.setRequestMethod("POST");

		        connection.setRequestProperty("Connection", "Keep-Alive");
		        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

		        outputStream = new DataOutputStream(connection.getOutputStream());
		        outputStream.writeBytes(twoHyphens + boundary + lineEnd);

		        String connstr = null;
		        connstr = "Content-Disposition: form-data; name=\"attachments\";filename=\"" + videoName + "\"" + lineEnd;
		        Log.i(TAG, "connstr->" + connstr);

		        outputStream.writeBytes(connstr);
		        outputStream.writeBytes(lineEnd);

		        bytesAvailable = fileInputStream.available();
		        bufferSize = Math.min(bytesAvailable, maxBufferSize);
		        buffer = new byte[bufferSize];

		        // Read file
		        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		        Log.e(TAG, "bytesAvailable " + bytesAvailable + "");
		        try
		        {
		            while (bytesRead > 0)
		            {
		                try
		                {
		                    outputStream.write(buffer, 0, bufferSize);
		                }
		                catch (OutOfMemoryError e)
		                {
		                    e.printStackTrace();
		                    response = "outofmemoryerror";

		                    Log.e(TAG, "115  OOM ");
		                    return response;
		                }
		                bytesAvailable = fileInputStream.available();
		                bufferSize = Math.min(bytesAvailable, maxBufferSize);
		                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		            }
		        }
		        catch (Exception e)
		        {
		            Log.e(TAG, "bytesAvailable error in try  124" + bytesAvailable + "");

		            e.printStackTrace();
		            response = "error";
		            return response;
		        }
		        outputStream.writeBytes(lineEnd);
		        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

		        Log.e(TAG, "112 --> end of wrte");

		        // Responses from the server (code and message)
		        int serverResponseCode = connection.getResponseCode();
		        Log.i(TAG, "server response code " + serverResponseCode);
		        Log.i(TAG, "server response msg " + connection.getResponseMessage());

		        if (serverResponseCode == 200)
		        {
		            response = "true";
		        }
		        InputStream is;
		        if (connection.getResponseCode() != 200)
		        {
		            is = connection.getErrorStream();
		        }
		        else
		        {
		            is = connection.getInputStream();
		        }
		        // s.useDelimiter("\\Z");

		        Log.i(TAG, "server response content " + getStringFromInputStream(is));

		        fileInputStream.close();
		        outputStream.flush();
		        outputStream.close();
		        outputStream = null;
		    }
		    catch (Exception ex)
		    {
		        // Exception handling
		        response = "error";
		        Log.e(TAG, ex.getMessage() + "");
		        ex.printStackTrace();
		    }

		    Log.e(TAG, "Time for uplaod " + (System.currentTimeMillis() - start) / 1000 + " sekund:)");
		    return response;
		}

		private String getStringFromInputStream(InputStream is)
		{

		    BufferedReader br = null;
		    StringBuilder sb = new StringBuilder();

		    String line;
		    try
		    {

		        br = new BufferedReader(new InputStreamReader(is));
		        while ((line = br.readLine()) != null)
		        {
		            sb.append(line);
		        }

		    }
		    catch (IOException e)
		    {
		        e.printStackTrace();
		    }
		    finally
		    {
		        if (br != null)
		        {
		            try
		            {
		                br.close();
		            }
		            catch (IOException e)
		            {
		                e.printStackTrace();
		            }
		        }
		    }

		    return sb.toString();

		}
		}
	
	
	

}
