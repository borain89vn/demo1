package com.knx.framework.camera;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;

public interface CameraPreviewHandler extends PreviewCallback {
	
	public boolean setHandlerEnabled(boolean flag);
	public boolean isHandlerEnabled();
	
	public void init(int width, int height, Camera.Parameters cParams);
	
	/**
	 * Starts the process to grabbed and process each
	 * video frame from camera for IR.
	 */
	public void startIntervalCapture();
	
	/**
	 * Stops the frame grabbing and IR process
	 */
	public void stopIntervalCapture();
}
