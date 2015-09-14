package com.knx.framework.arcontents.overlay;

import java.io.IOException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import com.badlogic.gdx.Gdx;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.main.Shared;
import com.knx.framework.utils.ARiseGLUtils;
import com.knx.framework.utils.ARiseMathUtils;

public class MyGLSurfaceView extends GLSurfaceView {
	
	private final String TAG = "ARiseMyGLSurfaceView";
	
	private MyGLRenderer renderer;
	private MediaPlayer mMediaPlayer;
	private Context context;
	
	private String url;
	private boolean autoplay;
	private boolean callback;
	
	private boolean isPlaying = false;
	private boolean isEnd = false;
	private boolean touchAllowed = false;

	public MyGLSurfaceView(Context cxt) {
		super(cxt);
		
		context = cxt;

		setEGLContextClientVersion(2);
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);

		renderer = new MyGLRenderer(this);
		setRenderer(renderer);
		
		setZOrderOnTop(true);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setKeepScreenOn(true);
	}
	
	public MyGLSurfaceView(Context cxt, AttributeSet attrs) {
		super(cxt, attrs);
		
		context = cxt;

		setEGLContextClientVersion(2);
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);

		renderer = new MyGLRenderer(this);
		setRenderer(renderer);
		
		setZOrderOnTop(true);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setKeepScreenOn(true);
	}

	public MyGLRenderer getRenderer() {
		return renderer;
	}
	
	public void setVideoParams(JSONObject jsonObject, boolean ap, boolean cb) {
		try {
			init();
			
			url				= jsonObject.getString("url");
			int width		= jsonObject.optInt("width", Gdx.graphics.getWidth()) * 320 / Gdx.graphics.getWidth();
			int height		= jsonObject.optInt("height", Gdx.graphics.getHeight() / 2) * 480 / Gdx.graphics.getHeight();
			int offsetX		= jsonObject.optInt("offsetx", 0);
			int offsetY		= jsonObject.optInt("offsety", 0);
			
			renderer.setParams(width, height, offsetX, offsetY);
			
			autoplay = ap;
			callback = cb;
		} catch (Exception e) {
			Log.e(TAG, "Error when parsing overlay video.", e);
			e.printStackTrace();
			renderer.setParams(240, 160, 0, 0);
		} finally {
			startVideo(ARiseGLUtils.createTexture());
		}
	}
	
	/********************************************************************
	 * Returns the URL of overlay video
	 * @return the URL of overlay video
	 */
	public String getURL() {
		return url;
	}
	
	/*******************************
	 * Control surface display
	 */
	public void hideSurface() {
		if (getVisibility() != View.GONE) {
			((Activity) context).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setVisibility(View.GONE);
				}
			});
		}
		if (isPlaying) {
			pause();
		}
		disallowTouch();
	}
	
	public void showSurface() {
		if (getVisibility() != View.VISIBLE) {
			((Activity) context).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setVisibility(View.VISIBLE);
				}
			});
		}
		allowTouch();
	}
	
	/**********************************************
	 * Control behavior when touching the screen
	 */
	private float[] touch = new float[2];
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (touchAllowed) {
			final int action = event.getAction();
			
			if (action == MotionEvent.ACTION_DOWN) {
				touch[0] = event.getX() - Gdx.graphics.getWidth() / 2;
				touch[1] = -(event.getY() - Gdx.graphics.getHeight() / 2);
				
				if (isVideoEnd()) { // end
					try {
						boolean in = ARiseMathUtils.isInside(touch, renderer.getVerticesScreenCoords());
						if (in) {
							startVideo(ARiseGLUtils.createTexture());
							requestRender();
						}
					} catch (Exception e) {
						Log.e(TAG, "Cannot check if touch point is inside");
					}
				} else { // not end
					try {
						boolean in = ARiseMathUtils.isInside(touch, renderer.getVerticesScreenCoords());
						if (in) {
							if (isPlaying) {
								pause();
							} else {
								start();
							}
						}
					} catch (Exception e) {
						Log.e(TAG, "Cannot check if touch point is inside");
					}
				}
			}
		}
		
		return true;
	}
	
	private void allowTouch() {
		touchAllowed = true;
	}
	
	private void disallowTouch() {
		touchAllowed = false;
	}
	
	/*******************************
	 * VIDEO METHODS
	 */
	public MediaPlayer getMediaPlayer() {
		return mMediaPlayer;
	}
	
	public boolean isVideoEnd() {
		return isEnd;
	}
	
	public boolean isVideoPlaying() {
		return isPlaying;
	}
	
	public void start() {
		try {
			mMediaPlayer.start();
			isPlaying = true;
			isEnd = false;
		} catch (Exception e) {
			
		}
	}
	
	public void pause() {
		try {
			isPlaying = false;
			mMediaPlayer.pause();
		} catch (Exception e) {
			
		}
	}
	
	public void stop() {
		try {
			mMediaPlayer.stop();
			isPlaying = false;
			isEnd = true;
		} catch (Exception e) {

		}
	}
	
	public void startVideo(int texture) {
		SurfaceTexture mSurfaceTexture = new SurfaceTexture(texture);
		mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
			
			@Override
			public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//				requestRender();
			}
		});
		renderer.setSurface(mSurfaceTexture);

		Surface s = new Surface(mSurfaceTexture);

		try {
			mMediaPlayer = new MediaPlayer();
			isEnd = false;
			if (url.substring(0, 8).equals("file:///")) { // local file
				String fileName = url.substring(8);
				int lastPeriodPos = fileName.lastIndexOf('.');
				if (lastPeriodPos > 0) {
					fileName = fileName.substring(0, lastPeriodPos);
				}
				mMediaPlayer.setDataSource(context, Uri.parse("android.resource://" + ARiseConfigs.PACKAGE_NAME + "/raw/" + fileName));
			} else {
				mMediaPlayer.setDataSource(context, Uri.parse(url));
			}
			mMediaPlayer.setLooping(false);
			mMediaPlayer.setSurface(s);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.prepareAsync();
			
			mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {	
					if (autoplay) {
						start();
						renderer.resetEntranceScale();
					}
				}
			});
			
			mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					if (isPlaying) { // OnCompletionListener can only be called after video starts
						isEnd = true;
						isPlaying = false;
						
						if (callback) {
							Shared.getOverlayDataManager().goToARStaticLayer();
						}
					}
				}
			});
		} catch (IllegalArgumentException e) {
			hideSurface();
			e.printStackTrace();
		} catch (SecurityException e) {
			hideSurface();
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			hideSurface();
			e.printStackTrace();
		} catch (NullPointerException e) {
			hideSurface();
			e.printStackTrace();
		}
	}
	
	public void init() {
		isEnd = false;
		isPlaying = false;
		
		autoplay = true;
		callback = false;
	}
}