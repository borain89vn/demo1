package com.knx.framework.camera;

import java.io.ByteArrayOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.helper.LangPref;
import com.knx.framework.main.CameraActivity;
import com.knx.framework.main.Shared;
import com.knx.framework.task.QueryImage;
import com.knx.framework.ui.ARiseDialogOneButton;
import com.knx.framework.utils.ARiseUtils;

public class CameraPreview extends TextureView implements SurfaceTextureListener {
	
	private final String TAG = "ARiseCameraPreview";
	
	private Context context;
	
	private Camera mCamera;
	private ICameraInitializer mCamInit = new DefaultCameraInitializer();
	private PreviewCallback cameraPreviewCallback;
	private Boolean isCapturing = false;
	private boolean hideAtOpening;
	private boolean stopAtOpening;
	private Timer timer;
	private byte[] data;
	private byte[] callbackBuffer;
	
	private ThreadPoolExecutor taskExecutor;
	private ScheduledExecutorService monitorExecutor;
	
//	private ExecutorService taskExecutor;
//	private ScheduledExecutorService monitorExecutor;
	
	private Integer curTimeout;
	
	/**
	 * Constructor method of this class.
	 * @param cxt				: the context uses this class 
	 * @param cpc				: preview callback for this camera
	 */
	public CameraPreview(Context cxt, boolean isHide, boolean isStop) {
		super(cxt);
		try {
			context = cxt;
			cameraPreviewCallback = new Camera.PreviewCallback() {
				public void onPreviewFrame(byte[] frameData, Camera camera) {
					if (frameData.length == camera.getParameters().getPreviewSize().width * camera.getParameters().getPreviewSize().height * 3 / 2) {
						data = frameData;
						Shared.getTrackingEngineManager().setYUVData(frameData);
					}
					camera.addCallbackBuffer(frameData);
				}
			};
			hideAtOpening = isHide;
			stopAtOpening = isStop;
			setSurfaceTextureListener(this);
		} catch (Exception e) {
			Log.e(TAG, "Error when initializing camera", e);
			e.printStackTrace();
		}
	}
	
	public byte[] getYUVFrame() {
		return data;
	}
	
	private boolean safeCameraOpen() {
		boolean qOpened = false;
		
		try {
			mCamera = Camera.open();
			qOpened = (mCamera != null);
		} catch (Exception e) {
			Log.e(TAG, "Error when opening camera", e);
			e.printStackTrace();
		}
		return qOpened;
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		try {
			if (safeCameraOpen()) {
				mCamInit.initCamera(mCamera, getResources().getConfiguration().orientation);
				
				// for YUV21 format, the size of callback buffer is w*h*1.5
				callbackBuffer = new byte[mCamera.getParameters().getPreviewSize().width * mCamera.getParameters().getPreviewSize().height * 3 / 2];
				
				((CameraActivity) context).finishInitializingNewCamera(mCamera);
				
				taskExecutor = new ThreadPoolExecutor(10, 10, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(4));
				monitorExecutor = Executors.newScheduledThreadPool(16);

				curTimeout = ARiseConfigs.MINIMUM_TIMEOUT;
				
				DisplayMetrics displaymetrics = new DisplayMetrics();
				((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
				int screenWidth = displaymetrics.widthPixels;
				int screenHeight = displaymetrics.heightPixels;
				
				setLayoutParams(new FrameLayout.LayoutParams(screenWidth, screenHeight, Gravity.CENTER));
				try {
					mCamera.setPreviewTexture(surface);
				} catch (Exception e) {
					Log.e(TAG, "Error when setting previewTexture surface", e);
					e.printStackTrace();
				}

				startCamera();
				
				if (hideAtOpening)
					setVisibility(View.GONE);
				
				if (stopAtOpening)
					stopCamera();
				
				if (!hideAtOpening && !stopAtOpening) { // normal case
					startQuery();
				}
			} else {
				final ARiseDialogOneButton dialog = new ARiseDialogOneButton(context);
				dialog.setThemeColor(ARiseConfigs.THEME_COLOR);
    			dialog.setTitleText("Error");
    			dialog.setMessageText(LangPref.TXTOPENCAMERAFAIL);
    			dialog.setButtonText(LangPref.TXTCANCEL);
    			dialog.setButtonOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
    			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						((Activity) context).finish();
					}
				});
    			dialog.show();
			}
		} catch (Exception e) {
			Log.e(TAG, "Error in onSurfaceTextureAvailable()", e);
			e.printStackTrace();
		}
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		try {
			stopPreviewAndFreeCamera();
		} catch (Exception e) {
			Log.e(TAG, "Error in onSurfaceTextureDestroyed()", e);
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// Update your view here!
	}
	
	public void startCamera() {
		synchronized (mCamera) {
			try {
				mCamera.setPreviewCallbackWithBuffer(null);
				mCamera.setPreviewCallbackWithBuffer(cameraPreviewCallback);
				mCamera.addCallbackBuffer(callbackBuffer);
				
				mCamera.startPreview();
				isCapturing = true;
				
				if (lastFrameTexture != null)
					lastFrameTexture = null;
				
				Shared.getCameraActivityUI().setCameraIndicatorAnimate(true);
			} catch (Exception e) {
				Log.e(TAG, "Cannot start camera.", e);
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void stopCamera() {
		synchronized (mCamera) {
			try {
				mCamera.stopPreview();
				isCapturing = false;
				
				Shared.getCameraActivityUI().setCameraIndicatorAnimate(false);
			} catch (Exception e) {
				Log.e(TAG, "Cannot stop camera.", e);
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void startQuery() {
		if (timer == null) {
			timer = new Timer();
			
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					byte[] curData = null;
					int curWidth = 0;
					int curHeight = 0;
					
					try {
						curData = data;
						curWidth = mCamera.getParameters().getPreviewSize().width;
						curHeight = mCamera.getParameters().getPreviewSize().height;
					} catch (Exception e) {
						Log.e(TAG, "Error occurs while taking values to prepare for image querying task", e);
						e.printStackTrace();
						
						curData = null;
						curWidth = 0;
						curHeight = 0;
					}
					
					if (curData == null || curWidth == 0 || curHeight == 0 || !isCapturing)
						return;
					
					if (!checkInternetState()) { // no internet access
						Log.e(TAG, "\t* * * * No internet access * * * *");
						Shared.getCameraActivityUI().displayToastWithText(LangPref.TXTNOCONNECTION, Toast.LENGTH_LONG);
					} else {
						YuvImage yuvImage = new YuvImage(curData, ImageFormat.NV21, curWidth, curHeight, null);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						yuvImage.compressToJpeg(new Rect(0, 0, curWidth, curHeight), 80, baos);
						byte[] jpegByteArray = baos.toByteArray();
						Bitmap bmp = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length);
						final Bitmap resizedBmp = CameraPreview.this.resizeFrameForRemoteQuery(bmp);
                        final QueryImage newQuery = new QueryImage(context, resizedBmp, curTimeout);
                        final int curQueryTimeout = curTimeout;
                        try {
                        	if (!taskExecutor.isShutdown() && !monitorExecutor.isShutdown()) {
		                        final Future<?> handler = taskExecutor.submit(newQuery);
		                        monitorExecutor.schedule(new Runnable() {
		                        	public void run() {
		                        		if (!handler.isDone()) { // slow connection
		                        			Log.i(TAG, "Cancel the Future object ===>");
		                        			handler.cancel(true);
		                        			
		                        			if (curTimeout < ARiseConfigs.MINIMUM_TIMEOUT) {
		                        				// hide bad connection status
		                        				Shared.getCameraActivityUI().hideToast();
		                        			} else if (curTimeout >= ARiseConfigs.MINIMUM_TIMEOUT && curTimeout < ARiseConfigs.SLOW_CONNECTION_THRESHOLD) {
		                        				// hide bad connection status
		                        				Shared.getCameraActivityUI().hideToast();
		                        			} else if (curTimeout >= ARiseConfigs.SLOW_CONNECTION_THRESHOLD && curTimeout < ARiseConfigs.MAXIMUM_TIMEOUT) {
		                        				Log.i(TAG, "Slow connection detected");
		                        				Shared.getCameraActivityUI().displayToastWithText(LangPref.TXTSLOWCONNECTION, Toast.LENGTH_LONG);
		                        			} else if (curTimeout >= ARiseConfigs.MAXIMUM_TIMEOUT) {
		                        				Log.i(TAG, "Too long connection -> error");
		                        				Shared.getCameraActivityUI().displayToastWithText(LangPref.TXTNETWORKERROR, Toast.LENGTH_LONG);
		                        			}
		                        			updateTimeout(curQueryTimeout);
		                        		} else {
		                        			resetTimeout();
		                        			Shared.getCameraActivityUI().hideToast();
		                        		}
		                        	}
		                        }, curQueryTimeout, TimeUnit.MILLISECONDS);
                        	}
                        } catch (RejectedExecutionException ree) {
                        	Log.e(TAG, "Cannot schedule new task for execution", ree);
                        	ree.printStackTrace();
                        } catch (NullPointerException npe) {
                        	Log.e(TAG, "Null task", npe);
                        	npe.printStackTrace();
                        }
                        bmp.recycle();
					}
				}
			}, 1000, 1000);
		} else {
			Log.e(TAG, "Old timer is still in use");
		}
	}
	
	public synchronized void stopQuery() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}
	
	private void updateTimeout(int curQueryTimeout) {
		synchronized (curTimeout) {
			if (curTimeout <= curQueryTimeout + 3000) {
				curTimeout = curQueryTimeout + 3000;
				Log.i(TAG, "Update timeout into " + curTimeout);
			}
		}
	}
	
	private void resetTimeout() {
		synchronized (curTimeout) {
			if (curTimeout != ARiseConfigs.MINIMUM_TIMEOUT) {
				Log.i(TAG, "Reset timeout from " + curTimeout);
				curTimeout = ARiseConfigs.MINIMUM_TIMEOUT;
			}
		}
	}
	
	private Bitmap resizeFrameForRemoteQuery(Bitmap originalBmp) {
		int newHeight = 0;
        int newWidth = 0;
        if (originalBmp.getWidth() < originalBmp.getHeight()) {
        	if (originalBmp.getWidth() > ARiseConfigs.UPLOAD_IMAGE_WIDTH) {
        		newWidth = ARiseConfigs.UPLOAD_IMAGE_WIDTH;
        	} else {
        		newWidth = originalBmp.getWidth();
        	}
        } else {
        	if (originalBmp.getHeight() > ARiseConfigs.UPLOAD_IMAGE_WIDTH) {
        		newHeight = ARiseConfigs.UPLOAD_IMAGE_WIDTH;
        	} else {
        		newHeight = originalBmp.getHeight();
        	}
        }
        
        if (newHeight == ARiseConfigs.UPLOAD_IMAGE_WIDTH) {
        	newWidth = ARiseConfigs.UPLOAD_IMAGE_WIDTH * originalBmp.getWidth() / originalBmp.getHeight();
        } else if (newWidth == ARiseConfigs.UPLOAD_IMAGE_WIDTH) {
        	newHeight = ARiseConfigs.UPLOAD_IMAGE_WIDTH * originalBmp.getHeight() / originalBmp.getWidth();
        }
        
        Bitmap resizedBmp = ARiseUtils.getResizedBitmap(originalBmp, newHeight, newWidth);
        
        return resizedBmp;
	}
	
	public android.hardware.Camera getCamera() {
		return mCamera;
	}
	
	public void stopPreviewAndFreeCamera() {
		try {
			if (mCamera != null) {
				Log.i(TAG, "Stop and release camera");
				
				taskExecutor.shutdownNow();
				monitorExecutor.shutdownNow();
				
				mCamera.setPreviewCallback(null);
				stopQuery();
				stopCamera();				
				mCamera.release();
				mCamera = null;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error when stopping preview and releasing camera", e);
			e.printStackTrace();
		}
	}
	
	private boolean checkInternetState() {
		try {
	        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	        if (wifiInfo.isConnected() || mobileInfo.isConnected()) {
	            return true;
	        }
	    } catch (Exception e) {
	    	Log.e(TAG, "Error while detecting internet state", e);
	    	e.printStackTrace();
	    }
	    return false;
	}
	
	private com.badlogic.gdx.graphics.Texture lastFrameTexture = null;
	public com.badlogic.gdx.graphics.Texture generateLastFrameTexture() {
		if (mCamera == null) return null;
		if (lastFrameTexture == null) {
			lastFrameTexture = CameraTexture.texturizeLastFrame(data, mCamera.getParameters().getPreviewSize().width, mCamera.getParameters().getPreviewSize().height);
		}
		return lastFrameTexture;
	}
}