package com.knx.framework.arcontents.overlay;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.knx.framework.R;
import com.knx.framework.main.Shared;
import com.knx.framework.utils.ARiseGLUtils;

/*
 * This class helps rendering the video texture on any OpenGL surface
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
	
	private static final String TAG = "ARiseMyGLRenderer";
    
    private MyGLSurfaceView myGlSurfaceView;
    private SurfaceTexture mSurface;
    public OverlayVideo mVideoDisplay;
    private OverlayPlayButton mPlayButton;
    public boolean lostTrackMode = true;
    
    private float[] mvpMatrix = new float[16];
    
    private int width, height, offsetx, offsety;
    
    private float entranceScale = 0;
    
    private float[][] vScreenCoords;
    
    public MyGLRenderer(MyGLSurfaceView sv) {
    	myGlSurfaceView = sv;
    	
    	// default params
    	width = 240;
    	height = 160;
    	offsetx = 0;
    	offsety = 0;
    }
    
    public void setSurface(SurfaceTexture surface) {
    	mSurface = surface;
	}
    
    public void setParams(int w, int h, int r, int c) {
    	width = w;
    	height = h;
    	offsetx = r;
    	offsety = c;
    }
    
    public void resetEntranceScale() {
    	entranceScale = 0;
    }
    
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        
    	// Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        mVideoDisplay = new OverlayVideo(myGlSurfaceView.getContext());
        mPlayButton = new OverlayPlayButton(myGlSurfaceView.getContext(), R.drawable.play_button);
        if (vScreenCoords == null)
        	vScreenCoords = new float[4][2];
        myGlSurfaceView.startVideo(ARiseGLUtils.createTexture());
    }

    @Override
    public void onDrawFrame(GL10 unused) {
    	
    	GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    	mSurface.updateTexImage();
    	
    	if (entranceScale < 1 && Shared.getOverlayDataManager().getAllowDisplay()) {
    		entranceScale += 1f / 12f;
    		Log.i(TAG, "Current entrance scale: " + entranceScale);
    	}
        
    	if (mvpMatrix != null) {
    		
    		float[] videoMatrix = new float[16];
    		float[] playBtnMatrix = new float[16];
    		
    		Matrix.scaleM(mvpMatrix, 0, entranceScale, entranceScale, 1);
    		
    		// calculate matrix for video and play button
    		if (Shared.getTrackingEngineManager().isTracked()) {
    			Matrix.translateM(mvpMatrix, 0, offsetx * 1f / entranceScale, offsety * 1f / entranceScale, 0f);
    			Matrix.scaleM(videoMatrix, 0, mvpMatrix, 0, height, width, 1);
    			Matrix.scaleM(playBtnMatrix, 0, mvpMatrix, 0, Math.min(height, width) / 2, Math.min(height, width) / 2, 1);
    		} else {
    			Matrix.scaleM(videoMatrix, 0, mvpMatrix, 0, 120, 160, 1);
    			Matrix.scaleM(playBtnMatrix, 0, mvpMatrix, 0, 60, 60, 1);
    			
    			float[] videoWidth = {0.5f, 0.5f, 0f, 1f};
    			float[] tmp = new float[4];
	    		Matrix.multiplyMV(tmp, 0, videoMatrix, 0, videoWidth, 0);
	    		float currentWidth = (tmp[1] / tmp[2]) * Gdx.graphics.getHeight();
	    		float fixRatio = Gdx.graphics.getWidth() / currentWidth;
	    		
	    		if (fixRatio != 1) {
	    			float fixWidth = 160 * fixRatio;
	    			float fixHeight = fixWidth * height / width;
	    			if (fixHeight > Gdx.graphics.getHeight())
	    				fixHeight = Gdx.graphics.getHeight();
	    			Matrix.scaleM(videoMatrix, 0, mvpMatrix, 0, fixHeight, fixWidth, 1);
	    			Matrix.scaleM(playBtnMatrix, 0, mvpMatrix, 0, Math.min(fixWidth, fixHeight) / 2f, Math.min(fixWidth, fixHeight) / 2f, 1);
	    		}
    		}
    		
    		// get state of video, determine hidden state of play button
    		if (myGlSurfaceView.isVideoPlaying()) {
    			mPlayButton.show();
    		} else {
    			mPlayButton.hide();
    		}
    		
    		mVideoDisplay.draw(videoMatrix);
    		mPlayButton.draw(playBtnMatrix);
        } else {
        	mVideoDisplay.draw(null);
    		mPlayButton.draw(null);
        }
    }
    
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);
    }
    
    public void setMVPMatrix(float[] mvpMat) {
    	if (mvpMat != null)
    		mvpMatrix = Arrays.copyOf(mvpMat, 16);
    	else
    		mvpMatrix = null;
    }
    
    public float[][] getVerticesScreenCoords() {
    	try {
	    	for (int i = 0; i < mVideoDisplay.getVerticesCoords().length; i++) {
	    		float[] tmp = new float[4];
	    		Matrix.multiplyMV(tmp, 0, mVideoDisplay.getLatestMVPMatrix(), 0, mVideoDisplay.getVerticesCoords()[i], 0);
	    		vScreenCoords[i][0] = (tmp[0] / tmp[2]) * Gdx.graphics.getWidth() / 2f;
	    		vScreenCoords[i][1] = (tmp[1] / tmp[2]) * Gdx.graphics.getHeight() / 2f;
	    	}
    	} catch (Exception e) { 
    		Log.e(TAG, "Error while calculating vertices screen coords", e);
    		e.printStackTrace();
    	}
    	return vScreenCoords;
    }
}