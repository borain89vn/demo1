package com.knx.framework.main;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.opengl.Matrix;
import android.util.Log;

import com.astar.i2r.mimas.ExSnap2TellException;
import com.astar.i2r.mimas.Snap2Tell;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.utils.ARiseMathUtils;
import com.knx.framework.utils.ARiseUtils;

public class LocalTrackingManager {
	
	private final String TAG = "ARiseLocalTracking";

	private Context context;
	
	// for MIMAS engine
	public Snap2Tell snap2TellEngine;
	private int prevWidth, prevHeight;
	private float focalLength;
	
	private byte[] yuvData;
	
	private boolean stopFlag;
	private boolean pauseFlag;
	private boolean yuvProcessed;
	
	private boolean entryAnimation = true;
	
	private int imgHandle;
	private boolean isTracked;
	private double[] poseMatrix;
	
	private Vector3 position;
	private Matrix4 rotMatrix;
	private Matrix4 lostTracked3DModelMatrix;
	
	private Runnable mRunnable;
	private Thread mThread;
	
	private Matrix4 lostTrackedVideoMatrix;
	private Matrix4 currentMatrix;
	private int step = 0;
	private final int NUM_STEPS = 12;
	
	private int setModelStatus = -1;
	
	private static LocalTrackingManager singletonInstance;
	public static LocalTrackingManager getSingletonInstance() {
		if (singletonInstance == null) {
			singletonInstance = new LocalTrackingManager();
			singletonInstance.snap2TellEngine = new Snap2Tell();
			singletonInstance.prevWidth = 0;
			singletonInstance.prevHeight = 0;
			singletonInstance.stopFlag = false;
			singletonInstance.pauseFlag = false;
			
			singletonInstance.poseMatrix = new double[12];
			singletonInstance.isTracked = false;
			
			singletonInstance.lostTrackedVideoMatrix = (new Matrix4()).idt();
			singletonInstance.lostTracked3DModelMatrix = (new Matrix4()).idt();
			
			singletonInstance.stopPositionTrackingThread();
			
			singletonInstance.mRunnable = new Runnable() {
				@Override
				public void run() {
					try {
						while (!singletonInstance.stopFlag) {
							
							
							if (singletonInstance.yuvData != null) {
								singletonInstance.imgHandle = -1;
								singletonInstance.imgHandle = singletonInstance.snap2TellEngine.setSnapImageCamera(singletonInstance.yuvData,
																													singletonInstance.prevWidth * singletonInstance.prevHeight * 3,
																													singletonInstance.prevWidth,
																													singletonInstance.prevHeight,
																													true);
								
								if (singletonInstance.shouldSetTrackingModel) { // there is a request of start local tracking process
									if (singletonInstance.imgHandle % 12 == 0) { // not try to do setModel at every frame
										if (!singletonInstance.isTracked || singletonInstance.setModelStatus != 1) { // condition for setModel
											double[] trackingData = singletonInstance.queryServerOfTrackingEngine(singletonInstance.yuvData);
											if (trackingData != null) {
												try {
													singletonInstance.setModelStatus = singletonInstance.snap2TellEngine.setModel(singletonInstance.imgHandle, trackingData);
												} catch (ExSnap2TellException e) {
													Log.e(singletonInstance.TAG, "Error while setting model");
												}
											}
										}
									}
								}
								
								if (singletonInstance.setModelStatus > 0) {
									singletonInstance.snap2TellEngine.imageQueryTrack(singletonInstance.imgHandle, 22);
									singletonInstance.snap2TellEngine.getPoseMatrix(singletonInstance.poseMatrix);
									singletonInstance.validatePoseMatrix();
								}
								
								singletonInstance.snap2TellEngine.releaseSnapImage(singletonInstance.imgHandle);
							}
							
							singletonInstance.yuvProcessed = true;
							
							synchronized (singletonInstance.mRunnable) {
								if (singletonInstance.yuvProcessed) {
									singletonInstance.mRunnable.wait();
								}
							}
							
							synchronized (singletonInstance.mRunnable) {
								if (singletonInstance.pauseFlag) {
									singletonInstance.mRunnable.wait();
								}
							}
							
							
						}
						singletonInstance.snap2TellEngine.imageStopTrack();
					} catch (Exception e) {
						Log.e(singletonInstance.TAG, "Error", e);
						e.printStackTrace();
					}
				}
			};
		}
		return singletonInstance;
	}
	
	public static void destroySingletonInstance() {
		if (singletonInstance == null) {
			return;
		} else {
			singletonInstance.stopPositionTrackingThread();
		}
		singletonInstance = null;
	}
	
	public synchronized byte[] getYUVData() {
		return yuvData;
	}
	
	public synchronized int getImgHandle() {
		return imgHandle;
	}
	
	public void clearModel() {
		snap2TellEngine.releaseModel();
	}
	
	public void setYUVData(final byte[] data) {
		synchronized (mRunnable) {
			if (!stopFlag && yuvProcessed) {
				yuvData = data.clone();
				yuvProcessed = false;
				mRunnable.notify();
			}
		}
	}
	
	public void startNewPositionTrackingThreadForISnapId(Context cxt, String iSnapId) {
		synchronized (this) {
			entryAnimation = true;
			step = NUM_STEPS;
			
			shouldSetTrackingModel = true;
			chosenISnapId = iSnapId;
			
			stopFlag = false;
			
			context = cxt;
			
			if (mThread == null) {
				mThread = new Thread(mRunnable, "ARisePositionTrackingThread");
				(new Thread(mRunnable)).start();
			} else {
				Log.e(TAG, "Position tracking thread is executing. Cannot start new thread.");
			}
		}
	}
	
	public void stopPositionTrackingThread() {
		synchronized (this) {
			stopFlag = true;
			shouldSetTrackingModel = false;
			setModelStatus = -1;
			chosenISnapId = null;
			
			if (mThread != null) {
				try {
					mThread.join();
				} catch (InterruptedException e) {
					Log.e(TAG, "Error while waiting for position tracking thread to finish", e);
					e.printStackTrace();
				} finally {
					Log.i(TAG, "Position tracking thread stops");
					mThread = null;
				}
			}
		}
	}
	
	public boolean isTracked() {
		return isTracked;
	}
	
	private boolean shouldSetTrackingModel = false;
	private String chosenISnapId = null;
	
	private double[] queryServerOfTrackingEngine(byte[] yuvData) {
		Log.i(TAG, "Start querying server of tracking engine");
		YuvImage yuvimage = new YuvImage(yuvData, ImageFormat.NV21, prevWidth, prevHeight, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, prevHeight, prevHeight), 100, baos);
        
        byte[] sByteArr = baos.toByteArray();
        Bitmap bmp = BitmapFactory.decodeByteArray(sByteArr, 0, sByteArr.length);
        
        int newHeight = 0, newWidth = 0;
        float resizeRatio = 1;
        
        // resizing the shorter length into 240
        try {
            resizeRatio = 240f / (float) Math.min(bmp.getWidth(), bmp.getHeight());
            newHeight = (int) (bmp.getHeight() * resizeRatio);
            newWidth= (int) (bmp.getWidth() * resizeRatio);
        } catch (Exception e) {
        	resizeRatio = 1;
        	newHeight = 0;
        	newWidth = 0;
        	Log.e(TAG, "Error when resizing to-be-uploaded image", e);
        	e.printStackTrace();
        }
        
        Bitmap newBmp = ARiseUtils.getResizedBitmap(bmp, newHeight, newWidth);
        
        String localTrackingJSONResponse = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            newBmp.compress(Bitmap.CompressFormat.JPEG, 50, bos);
            
            /** hungnm - date: 20131023 - begin **/             
            String boundary = "KNXNK";
            String params = "?w=" + ARiseConfigs.DEVICE_WIDTH + "&h=" + ARiseConfigs.DEVICE_HEIGHT +
            		"&telcoName=" + URLEncoder.encode(ARiseConfigs.MOBILE_NETWORK_OPERATOR_NAME, ARiseConfigs.CHARSET) +
            		"&telcoCountryCode=" + ARiseConfigs.MOBILE_COUNTRY_CODE +
            		"&telcoNetworkCode=" + ARiseConfigs.MOBILE_NETWORK_CODE +
            		"&model=" + URLEncoder.encode(ARiseConfigs.DEVICE_MODEL, ARiseConfigs.CHARSET) +
            		"&longitude=" + URLEncoder.encode(String.valueOf(ARiseConfigs.LONGITUDE), ARiseConfigs.CHARSET) +
            		"&latitude=" + URLEncoder.encode(String.valueOf(ARiseConfigs.LATITUDE), ARiseConfigs.CHARSET) +
            		"&sdk=" + URLEncoder.encode("android-"+ARiseConfigs.SDK_VERSION, ARiseConfigs.CHARSET) +
            		"&cameraType=" + URLEncoder.encode(String.valueOf(63), ARiseConfigs.CHARSET) +
            		"&isFOV=" + URLEncoder.encode(String.valueOf(true), ARiseConfigs.CHARSET);
            /** hungnm - date: 20131023 - end **/
            
            // client code
            // pass the client code to server for getting private poster
            // used in S2T and D2L app
            SharedPreferences prefs = context.getApplicationContext().getSharedPreferences("KnorexPref", Context.MODE_PRIVATE);
            if (prefs.contains(ARiseConfigs.CLIENT_CODE_KEY)) {
            	String clientCode = prefs.getString(ARiseConfigs.CLIENT_CODE_KEY, "");
            	Log.i(TAG, "Found client code: " + clientCode);
            	if (clientCode != null && clientCode.length() > 0) {
            		params += "&clientCode=" + URLEncoder.encode("knorex_01", ARiseConfigs.CHARSET);
            	}
         	}
            
            URL url = new URL(ARiseConfigs.SERVICE_BASE_URL + "/QueryRemote" + params);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            
            byte[] data = bos.toByteArray();
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
            localTrackingJSONResponse = response.toString();
            connection.disconnect();
            
        } catch (SocketTimeoutException e) {
        	Log.e(TAG, String.format("Error while querying image to server. Type: %s", "SocketTimeoutException"));
        } catch (ConnectException e) {
        	Log.e(TAG, String.format("Error while querying image to server. Type: %s", "ConnectException"));
        } catch (Exception e) {
        	Log.e(TAG, String.format("Error while querying image to server. Type: %s", "Unknown"));
            e.printStackTrace();
        }

        newBmp.recycle();
        
        if (localTrackingJSONResponse == null || localTrackingJSONResponse.length() == 0)
			return null;
        
        Log.i("LocalTracking", "Position tracking response: " + localTrackingJSONResponse);
        
        JSONObject localTrackingJSONObj = null;
		try {
			localTrackingJSONObj = new JSONObject(localTrackingJSONResponse);
		} catch (JSONException jsonException) {
			Log.e(TAG, "Error while converting string response into json objection", jsonException);
			jsonException.printStackTrace();
			localTrackingJSONObj = null;
		} catch (Exception e) {
			Log.e(TAG, "Unknown error while converting string response into json objection", e);
			e.printStackTrace();
			localTrackingJSONObj = null;
		}
		
       	if (localTrackingJSONObj == null)
       		return null;
       	
       	if (!localTrackingJSONObj.optBoolean("success")) {
       		// do nothing
       	} else {
       		int size = Integer.parseInt(localTrackingJSONObj.optString("size"));
       		if (size == 0) { // no AR
       			return null;
       		} else if (size == 1) { // single AR
       			try {
	       			JSONArray entries = localTrackingJSONObj.getJSONArray("results");
	       			JSONObject obj = entries.getJSONObject(0);
	       			if (obj.optString("isnap_id").equals(chosenISnapId)) {
	       				try {
	       	       			JSONArray resTrackingData = obj.getJSONArray("tracking_data");
	       					double[] trackingData = new double[12];
	       					for (int i = 0; i < 12; i++) {
	       						trackingData[i] = resTrackingData.getDouble(i);
	       					}
	       					return trackingData;
	       	       		} catch (Exception e) {
	       	       			return null;
	       	       		}
	       			} else {
	       				return null;
	       			}
       			} catch (JSONException jsonException) {
       				Log.e(TAG, "Error while parsing JSON in single AR", jsonException);
       				jsonException.printStackTrace();
       				return null;
       			}
       		} else if (size > 1) { // multi ARs
       			try {
	       			JSONArray entries = localTrackingJSONObj.getJSONArray("results");
	       			for (int i = 0; i < size; i++) {
	       				JSONObject obj = entries.getJSONObject(i);
	       				if (obj.optString("isnap_id").equals(chosenISnapId)) {
		       				try {
		       	       			JSONArray resTrackingData = obj.getJSONArray("tracking_data");
		       					double[] trackingData = new double[12];
		       					for (int j = 0; j < 12; j++) {
		       						trackingData[j] = resTrackingData.getDouble(j);
		       					}
		       					return trackingData;
		       	       		} catch (Exception e) {
		       	       			return null;
		       	       		}
		       			}
	           		}
	       			return null;
       			} catch (JSONException jsonException) {
       				Log.e(TAG, "Error while parsing JSON in multiple ARs", jsonException);
       				jsonException.printStackTrace();
       				return null;
       			}
       		}
       	}
		return null;
	}
	
	//**************************************************//
	// OVERLAY CONTENTS POSITION AND ORIENTATION HANDLE // 
	//**************************************************//
	
	public synchronized void update() {
		
		if (position == null)
			position = new Vector3();
		
		if (rotMatrix == null)
			rotMatrix = new Matrix4();
		
		float[] a = new float[16];
		a[0] = (float) poseMatrix[0];	a[4] = (float) poseMatrix[1];	a[8] = (float) poseMatrix[2];	a[12] = (float) 0f;
		a[1] = (float) poseMatrix[3];	a[5] = (float) poseMatrix[4];	a[9] = (float) poseMatrix[5];	a[13] = (float) 0f;
		a[2] = (float) poseMatrix[6];	a[6] = (float) poseMatrix[7];	a[10] = (float) poseMatrix[8];	a[14] = (float) 0f;
		a[3] = 0f;						a[7] = 0f;						a[11] = 0f;						a[15] = 1f;
		float[] rotAngles = ARiseMathUtils.extractAngleFromMatrix(a);
		Matrix4 newRotMatrix = (new Matrix4()).idt()
												.rotate(Vector3.Y, ARiseMathUtils.radToDeg(rotAngles[0]))
												.rotate(Vector3.X, ARiseMathUtils.radToDeg(rotAngles[1]))
												.rotate(Vector3.Z, ARiseMathUtils.radToDeg(rotAngles[2]))
												.rotate(Vector3.Z, -90);
		
		if (isTracked) {
			if (entryAnimation) {
				if (step < NUM_STEPS) { // NOT REACH END OF ENTRY ANIMATION
					step++;
					rotMatrix = newRotMatrix;
					position.set((float) -poseMatrix[10], (float) -poseMatrix[9], (float) - poseMatrix[11]);
				} else { // ENTRY ANIMATION FINISHES
					rotMatrix = newRotMatrix;
					position.set((float) -poseMatrix[10], (float) -poseMatrix[9], (float) - poseMatrix[11]);
					entryAnimation = false;
				}
			} else {
				if (step < NUM_STEPS) { // NOT REACH END OF TRANSITION
					step++;
					rotMatrix = rotMatrix.lerp(newRotMatrix, 1f / (NUM_STEPS - step + 1)).cpy();
					position = position.lerp(new Vector3((float) -poseMatrix[10], (float) -poseMatrix[9], (float) - poseMatrix[11]), 1f / (NUM_STEPS - step + 1)).cpy();
				} else {
					rotMatrix = newRotMatrix;
					position.set((float) -poseMatrix[10], (float) -poseMatrix[9], (float) - poseMatrix[11]);
				}
			}
		} else {
			if (entryAnimation) {
				if (step > 0) {
					step--;
					rotMatrix = lostTracked3DModelMatrix.cpy();
					position.set(0f, 0f, -focalLength);
				} else {
					rotMatrix = lostTracked3DModelMatrix.cpy();
					position.set(0f, 0f, -focalLength);
					entryAnimation = false;
				}
			} else {
				if (step > 0) {
					step--;
					rotMatrix = rotMatrix.lerp(lostTracked3DModelMatrix.cpy(),  1f / (step + 1)).cpy();
					position = position.lerp(new Vector3(0f, 0f, -focalLength), 1f / (step + 1)).cpy();
				} else {
					rotMatrix = lostTracked3DModelMatrix.cpy();
					position.set(0f, 0f, -focalLength);
				}
			}
		}
	}
	
	public Vector3 getTranslationVector() {
		return position;
	}
	
	public Matrix4 getRotationMatrix() {
		return rotMatrix;
	}
	
	
	public synchronized Matrix4 getOverlayVideoMatrix4() {
		try {
			if (isTracked) {
				Matrix4 extMatrix = ARiseMathUtils.convertRTMatrixToExtrinsicMatrix(poseMatrix).cpy();
				if (entryAnimation) {
					if (step < NUM_STEPS) { // NOT REACH END OF ENTRY ANIMATION
						step++;
						currentMatrix = extMatrix;
					} else { // ENTRY ANIMATION FINISHES
						currentMatrix = extMatrix;
						entryAnimation = false;
					}
				} else {
					if (step < NUM_STEPS) { // NOT REACH END OF TRANSITION
						step++;
						currentMatrix = currentMatrix.lerp(extMatrix, 1f / (NUM_STEPS - step + 1)).cpy();
					} else {
						currentMatrix = extMatrix;
					}
				}

			} else {
				
				if (entryAnimation) {
					if (step > 0) {
						step--;
						currentMatrix = lostTrackedVideoMatrix.cpy();
					} else {
						currentMatrix = lostTrackedVideoMatrix.cpy();
						entryAnimation = false;
					}
				} else {
					if (step > 0) {
						step--;
						currentMatrix = currentMatrix.lerp(lostTrackedVideoMatrix.cpy(), 1f / (step + 1)).cpy();
					} else {
						currentMatrix = lostTrackedVideoMatrix.cpy();
					}
				}
				
			}
			return currentMatrix;
		} catch (Exception e) {
			return null;
		}
	}
	
	private float[] videoIntrinsicMatrix;
	public synchronized float[] getOverlayVideoMatrix() {
		try {
			if (isTracked) {
				videoIntrinsicMatrix = ARiseMathUtils.convertIntrinsicToCameraProjectionMatrix4(Shared.getTrackingEngineManager().snap2TellEngine.getCameraIntrinsicParams(),
						Gdx.graphics.getWidth(),
						Gdx.graphics.getHeight(),
						320, 240);
				
				Matrix4 extMatrix = ARiseMathUtils.convertRTMatrixToExtrinsicMatrix(poseMatrix).cpy();
				if (entryAnimation) {
					if (step < NUM_STEPS) { // NOT REACH END OF ENTRY ANIMATION
						step++;
						currentMatrix = extMatrix;
					} else { // ENTRY ANIMATION FINISHES
						currentMatrix = extMatrix;
						entryAnimation = false;
					}
				} else {
					if (step < NUM_STEPS) { // NOT REACH END OF TRANSITION
						step++;
						currentMatrix = currentMatrix.lerp(extMatrix, 1f / (NUM_STEPS - step + 1)).cpy();
					} else {
						currentMatrix = extMatrix;
					}
				}
			} else {
				
				if (entryAnimation) {
					if (step > 0) {
						step--;
						currentMatrix = lostTrackedVideoMatrix.cpy();
					} else {
						currentMatrix = lostTrackedVideoMatrix.cpy();
						entryAnimation = false;
					}
				} else {
					if (step > 0) {
						step--;
						currentMatrix = currentMatrix.lerp(lostTrackedVideoMatrix.cpy(), 1f / (step + 1)).cpy();
					} else {
						currentMatrix = lostTrackedVideoMatrix.cpy();
					}
				}
				
			}
			float[] mvpMatrix = new float[16];
			Matrix.multiplyMM(mvpMatrix, 0, videoIntrinsicMatrix, 0, currentMatrix.val, 0);
			return mvpMatrix;
		} catch (Exception e) {
			return null;
		}
	}
	
	/********************************************************************************
	 * Checks if the pose matrix should be used.
	 * If not, force to change state to UNTRACKED.
	 */
	private synchronized void validatePoseMatrix() {
		if (poseMatrix[11] < 0) {
			isTracked = false;
			return;
		}
		
		if (poseMatrix[11] > (float) (10 * focalLength)) {
			isTracked = false;
			return;
		}
		
		if (Math.abs(poseMatrix[9]) > 500 || Math.abs(poseMatrix[10]) > 500) {
			isTracked = false;
			return;
		}
		
		if ((poseMatrix[0] == 0) && (poseMatrix[1] == 0) && (poseMatrix[2] == 0)) {
			isTracked = false;
			return;
		}
		
		if ((poseMatrix[3] == 0) && (poseMatrix[4] == 0) && (poseMatrix[5] == 0)) {
			isTracked = false;
			return;
		}
		
		if ((poseMatrix[6] == 0) && (poseMatrix[7] == 0) && (poseMatrix[8] == 0)) {
			isTracked = false;
			return;
		}
		
		isTracked = true;
		return;
	}
	
	/********************************************************************************
	 * Saves the preview size in MIMAS tracking engine and uses it to initialize some
	 * constant values for later use. 
	 * @param w	: the preview width
	 * @param h	: the preview height
	 */
	public void setPreviewSize(int w, int h) {
		prevWidth = w;
		prevHeight = h;
		focalLength = ARiseMathUtils.getFocalLength(ARiseConfigs.FOV, 320);
		lostTrackedVideoMatrix = (new Matrix4()).idt().rotate(Vector3.Z, 90f).trn(0f, 0f, -focalLength);
//		lostTracked3DModelMatrix = (new Matrix4()).idt().rotate(Vector3.Z, 90);
		lostTracked3DModelMatrix = (new Matrix4()).idt();
	}
}