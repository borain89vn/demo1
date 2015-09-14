package com.knx.framework.camera;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class CameraTexture {
	
	private final String TAG = "ARiseCameraTexture";
	
	private byte[] yuvData;
	private Texture	cameraTexture;

	private Pixmap pxMap;
	private Mat mYuv;

	private MatOfByte matJpeg = new MatOfByte();
	private MatOfInt  params90 = new MatOfInt(Highgui.IMWRITE_JPEG_QUALITY, 90);
	
	// dimension
	private int mYUVRow, mYUVCol;
	
	public CameraTexture(int pWidth, int pHeight) {
		mYUVRow = pHeight + pHeight / 2;
		mYUVCol = pWidth;
	}
	
	public synchronized void texturizeCameraPreview(final byte[] data) {
		
		// store data
		yuvData = data;
		
		if (mYUVRow <= 0 || mYUVCol <= 0) { // invalid dimenson
			cameraTexture = null;
			return;
		}
		
		if (yuvData == null) { // no data
			return;
		}

		if (pxMap != null && cameraTexture != null) {
			pxMap.dispose();
			cameraTexture.dispose();
		}

		try {
			if (mYuv != null) {
				mYuv.release();
			}
			mYuv = new Mat(mYUVRow, mYUVCol, CvType.CV_8UC1);
			mYuv.put(0, 0, yuvData);
			Mat mRgba = new Mat();
			
			Imgproc.cvtColor( mYuv, mRgba, Imgproc.COLOR_YUV2BGR_NV21, 3 );
			Highgui.imencode(".jpg", mRgba, matJpeg, params90);

			byte[] jpegData = matJpeg.toArray();
							
			pxMap = new Pixmap(jpegData, 0, jpegData.length);
			cameraTexture = new Texture(pxMap);
			
			matJpeg.release();
			mRgba.release();
		} catch (Exception e) {
			Log.e(TAG,"texture error",e);
			e.printStackTrace();
		}
	}
	
	public Texture getCameraTexture() {
		return cameraTexture;
	}
	
	public static Texture texturizeLastFrame(byte[] data, int previewWidth, int previewHeight) {
		int yuvRow = previewHeight + previewHeight / 2;
		int yuvCol = previewWidth;
		if (yuvCol <= 0 || yuvRow <= 0) return null;
		if (data == null) return null;
		Mat mat = new Mat(yuvRow, yuvCol, CvType.CV_8UC1);
		mat.put(0, 0, data);
		Mat mRgba = new Mat();
		
		MatOfByte imatJpeg = new MatOfByte();
		MatOfInt  iparams90 = new MatOfInt(Highgui.IMWRITE_JPEG_QUALITY, 90);
		
		Imgproc.cvtColor(mat, mRgba, Imgproc.COLOR_YUV2BGR_NV21, 3 );
		Highgui.imencode(".jpg", mRgba, imatJpeg, iparams90);

		byte[] jpegData = imatJpeg.toArray();
						
		Pixmap ipxMap = new Pixmap(jpegData, 0, jpegData.length);
		Texture lastFrameTexture = new Texture(ipxMap);
		
		imatJpeg.release();
		mRgba.release();
		
		return lastFrameTexture;
	}
}
