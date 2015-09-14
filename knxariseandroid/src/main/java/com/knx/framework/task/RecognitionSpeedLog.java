package com.knx.framework.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class RecognitionSpeedLog {
	
	private static final String TAG = "ARiseRecognitionSpeed"; 
	
	private final File LOG_FOLDER = new File(android.os.Environment.getExternalStorageDirectory(), "ARise_log");
	
	private long startTime;
	
	private Queue<JSONObject> logQueue;
	
	public RecognitionSpeedLog() {
		startTime = System.currentTimeMillis();
		logQueue = new ConcurrentLinkedQueue<JSONObject>();
	}
	
	public synchronized void addLog(
			long clientStartTime, long clientEndTime,
			long serverStartTime, long serverEndTime,
			String clientToken, String serverToken,
			int timeout) {
		JSONObject timeInfoJson = new JSONObject();
		try {
			timeInfoJson.put("clientStartTime", clientStartTime);
			timeInfoJson.put("clientEndTime", clientEndTime);
			timeInfoJson.put("serverStartTime", serverStartTime);
			timeInfoJson.put("serverEndTime", serverEndTime);
			timeInfoJson.put("clientToken", clientToken);
			timeInfoJson.put("serverToken", serverToken);
			timeInfoJson.put("timeout", timeout);
			logQueue.offer(timeInfoJson);
		} catch (JSONException e) {
			Log.e(TAG, "Error while adding time info json to log queue", e);
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void exportJson() {
		if (!LOG_FOLDER.exists()) {
			LOG_FOLDER.mkdirs();
		}
		
		if (!LOG_FOLDER.exists()) {
			Log.e(TAG, "Cannot create the folder for log files");
			return;
		}
		
		File logFile = new File(LOG_FOLDER, startTime + ".json");
		if (logFile.exists()) {
			Log.e(TAG, "Are you kidding me? Log file is already, I cannot believe!");
			return;
		} else {
			try {
				if (logFile.createNewFile()) {
					WriteLogTask task = new WriteLogTask(logFile);
					task.execute(logQueue);
				} else {
					Log.e(TAG, "Cannot create new log file! So sad :(");
					return;
				}
			} catch (IOException ioe) {
				Log.e(TAG, "IOException occurs while creating new log file", ioe);
				ioe.printStackTrace();
			}
		}
	}
	
	public class WriteLogTask extends AsyncTask<Queue<JSONObject>, Void, Boolean> {

		private File mOutputFile;
		
		public WriteLogTask(File outputFile) {
			mOutputFile = outputFile;
		}
		
		@Override
		protected Boolean doInBackground(Queue<JSONObject>... params) {
			Queue<JSONObject> queue = params[0];
			
			Boolean resultBoolean = Boolean.FALSE;
			
			JSONArray array = new JSONArray();
			while (!queue.isEmpty()) {
				JSONObject timeInfoJson = queue.poll();
				array.put(timeInfoJson);
			}
			
			BufferedWriter bufferedWriter = null;
			try {
				JSONObject outputJson = new JSONObject();
				outputJson.put("numberOfQueries", array.length());
				outputJson.put("queriesTimeInfo", array);
				String jsonString = outputJson.toString(4);
			
				FileWriter fileWriter = new FileWriter(mOutputFile);
				bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(jsonString);
				
				resultBoolean = Boolean.TRUE;
				
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				try {
					if (bufferedWriter != null)
						bufferedWriter.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			
			return resultBoolean;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onCancelled(Boolean result) {
			super.onCancelled(result);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result.booleanValue())
				Log.i(TAG, "Write log to file successfully");
			else
				Log.i(TAG, "Failed to write log");
		}

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "Start writing log to file: " + mOutputFile.getName());
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			
		}
	}
}
