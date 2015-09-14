package com.knx.framework.camera;

import java.security.InvalidParameterException;

import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;

/**
 * This is the default Camera Initializer.
 * <br>Users can create their own custom initializer by implementing ICameraInitializer
 * and set it into the MimasCamera.setCustomCameraInitializer().
 * @author I2R
 *
 */
public class DefaultCameraInitializer implements ICameraInitializer {

	private static String TAG = "CameraInitializer";

	@Override
	public void initCamera(Camera camera, int orientation) {

		Log.i(TAG, "/******** ARise Camera Initializer ********/");

		Camera.Parameters p = camera.getParameters();

		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			camera.setDisplayOrientation(90);
			p.set("orientation", "portrait");
			p.set("rotation", 90);
			Log.i(TAG, String.format("Orientation: portrait"));
		}
		
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			camera.setDisplayOrientation(0);
			p.set("orientation", "landscape");
			p.set("rotation", 0);
			Log.i(TAG, String.format("Orientation: landscape"));
		}
		
		Log.i(TAG, "Supported preview formats");
		for (int format : p.getSupportedPreviewFormats()){
			Log.i(TAG, String.format("\t+ %d", format));
		}
		
		Log.i(TAG, "Supported picture formats");
		for (int format : p.getSupportedPictureFormats()){
			Log.i(TAG, String.format("\t+ %d", format));
		}
		
		setPictureFormat(ImageFormat.JPEG, camera);
		p.set("jpeg-quality", 100);

		Camera.Size optimalPictureSize = getOptimalPictureResolution(640, 480, camera);
		p.setPictureSize(optimalPictureSize.width, optimalPictureSize.height);
		Log.i(TAG, String.format("Selected picture size: (%d, %d)", p.getPictureSize().width, p.getPictureSize().height));
		
		p.setPreviewFormat(ImageFormat.NV21);
		Camera.Size optimalPreviewSize = getOptimalResolution(640, 480, camera);
		p.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
		Log.i(TAG, String.format("Selected preview size: (%d, %d)", p.getPreviewSize().width, p.getPreviewSize().height));

		Log.i(TAG, "Supported white balance:");
		for (String whiteBalType : p.getSupportedWhiteBalance()) {
			if (whiteBalType.compareToIgnoreCase("auto") == 0) {
				p.set("white-balance", "auto");
				Log.i(TAG, String.format("\t+ whitebalanceType: %s (selected)", whiteBalType));
			} else {
				Log.i(TAG, String.format("\t+ whitebalanceType: %s", whiteBalType));
			}
		}
		
		for (String focusMode:p.getSupportedFocusModes()) {
			if (focusMode.compareToIgnoreCase(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) == 0) {
				p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				Log.i(TAG, "Video focus supported!");
			}
		}
		
		for (String focusMode:p.getSupportedFocusModes()) {
			if (focusMode.compareToIgnoreCase(Camera.Parameters.FOCUS_MODE_AUTO) == 0) {
				Log.i(TAG, "Auto focus supported!");
			}
		}
		
		camera.setParameters(p);
//		Log.d(TAG, "Camera parameters: " + camera.getParameters().flatten());

		try {
			Log.i(TAG," # PRE captured - preview- type["+p.getPreviewFormat()+"] NV21="+ImageFormat.NV21+" width["+p.getPreviewSize().width+"]height["+p.getPreviewSize().height+"] prefererd w["+p.getPreferredPreviewSizeForVideo().width+"]h["+p.getPreferredPreviewSizeForVideo().height+"]");
			Log.i(TAG," # PRE captured - picture- type["+p.getPictureFormat()+"] RGB="+ImageFormat.RGB_565+" width["+p.getPictureSize().width+"]height["+p.getPictureSize().height+"] prefererd w["+p.getPreferredPreviewSizeForVideo().width+"]h["+p.getPreferredPreviewSizeForVideo().height+"]");
		} catch (NullPointerException npe) {
			Log.i(TAG, "# camera exception:", npe);
		}
		
		Log.i(TAG, "/******** End camera initializer ********/");
	}
	
	private void setPictureFormat(int format, Camera camera) throws InvalidParameterException{
		boolean formatAccepted = false;
		for (int _format : camera.getParameters().getSupportedPictureFormats()) {
			if (_format == format) {
				formatAccepted = true;
				Camera.Parameters p = camera.getParameters();
				p.setPictureFormat(format);
				camera.setParameters(p);
				Log.d("mimas", "   - Found Picture format["+format+"] supported");
				break;
			}
		}
		if (!formatAccepted) {
			throw new InvalidParameterException("Picture format["+format+"] not supported");
		}
	}
	
	private Camera.Size getOptimalResolution(int pxWidth, int pxHeight, Camera camera) {
        Camera.Size result = null;
        float dr = Float.MAX_VALUE;
        float ratio = (float)pxWidth / (float)pxHeight;
        Camera.Size smallestSize = null;

        for (Camera.Size size : camera.getParameters().getSupportedPreviewSizes()) {
            float r = (float)size.width / (float)size.height;
            if ((Math.abs(r - ratio) < dr) && size.width <= pxWidth && size.height <= pxHeight ) {
                dr = Math.abs(r - ratio);
            	result = size;
            }
            
            if (smallestSize == null) { // just update the smallest
            	smallestSize = size;
            } else {
            	if ((smallestSize.width * smallestSize.height) > (size.width * size.height)) {
            		smallestSize = size;
            	}
            }
        }
        
        if (result == null) { // no size is smaller than Size(pxWidth, pxHeight)
        	result = smallestSize;
        }

        return result;
	}
	
	private Camera.Size getOptimalPictureResolution(int pxWidth, int pxHeight, Camera camera){
        Camera.Size result=null;
        float dr = Float.MAX_VALUE;
        float ratio = (float) pxWidth / (float) pxHeight;
        Camera.Size smallestSize = null;

        for (Camera.Size size : camera.getParameters().getSupportedPictureSizes()) {
            float r = (float) size.width / (float) size.height;
            if (Math.abs(r - ratio) < dr && size.width <= pxWidth && size.height <= pxHeight) {
                dr = Math.abs(r - ratio);
                result = size;
            }
            
            if (smallestSize == null) { // just update the smallest
            	smallestSize = size;
            } else {
            	if ((smallestSize.width * smallestSize.height) > (size.width * size.height)) {
            		smallestSize = size;
            	}
            }
        }
        
        if (result == null) { // no size smaller than Size(pxWidth, pxHeight)
        	result = smallestSize;
        }

        return result;
	}
}
