package com.knx.framework.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector3;
import com.knx.framework.R;
import com.knx.framework.arcontents.GestureListener;
import com.knx.framework.arcontents.OverlayDataManager;
import com.knx.framework.arcontents.StaticDataManager;
import com.knx.framework.arcontents.old.Static3DModel;
import com.knx.framework.arcontents.overlay.MyGLSurfaceView;
import com.knx.framework.arcontents.overlay.Overlay3DModel;
import com.knx.framework.camera.CameraPreview;
import com.knx.framework.camera.CameraTexture;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.helper.ARiseTracking;
import com.knx.framework.helper.GPSTracker;
import com.knx.framework.main.history.HistoryPage;
import com.knx.framework.main.setting.SettingPage;
import com.knx.framework.task.AddHistory;
import com.knx.framework.task.RecognitionSpeedLog;
import com.knx.framework.ui.ARiseDialogOneButton;
import com.knx.framework.utils.ARiseGLUtils;
import com.knx.framework.utils.ARiseUtils;
import com.knx.framework.videoplayer.MP4VideoPlayer;
import com.knx.framework.videoplayer.YouTubeVideoPlayer;

public class CameraActivity extends AndroidApplication implements IStaticLayerParent, IOverlayLayerParent {

	private static final String TAG = "ARiseCameraActivity";
	
	private boolean stopCheckGPS = false;
	private SharedPreferences pref;
    private SharedPreferences.Editor editor;
	
	private static final ExecutorService pool = Executors.newFixedThreadPool(20);
	public static void run(Runnable runnable) {
		pool.execute(runnable);
	}

	private CameraPreview mCameraPreview;
	private CameraTexture mCameraTexture;
	
	private RelativeLayout cameraUILayout;
	private RelativeLayout staticLayerLayout;
	private MultiARView multiARLayout;
	
	private JSONObject latestJSONObject = null;
	public RelativeLayout arStaticLayout;
	
	private AssetManager externalAssets;
	private AssetManager internalAssets;
	
	private RecognitionSpeedLog recognitionSpeedLog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		saveConfig();
        
        pref = getApplicationContext().getSharedPreferences(ARiseConfigs.PREF_FILENAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        stopCheckGPS = pref.getBoolean("stop_check_gps", false);
        
//        Shared.getTrackingEngineManager().startTrackingEngine();

		initialize(applicationListener, Shared.getAndroidApplicationConfiguration());

		if (graphics.getView() instanceof SurfaceView) {
			SurfaceView glView = (SurfaceView) graphics.getView();
			// force alpha channel - I'm not sure we need this as the GL surface is already using alpha channel
			glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}
		
		// we don't want the screen to turn off during the long image saving process 
		graphics.getView().setKeepScreenOn(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		
		if (mCameraPreview != null) {
			mCameraPreview.stopPreviewAndFreeCamera();
		}
		
		if (Shared.getOverlayDataManager().getGLSurfaceView() != null) {
			Shared.getOverlayDataManager().getGLSurfaceView().pause();
		}
		
		if (recognitionSpeedLog != null)
			recognitionSpeedLog.exportJson();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		checkGPS();
		
		RecognitionTokenGenerator.endCurrentSession();
		RecognitionTokenGenerator.startNewSession();
		recognitionSpeedLog = new RecognitionSpeedLog();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		StaticDataManager.destroySingletonInstance();
		OverlayDataManager.destroySingletonInstance();
		CameraActivityUI.destroySingletonInstance();
		LocalTrackingManager.destroySingletonInstance();
	}
	
	@Override
	public void onBackPressed() {
		if (Shared.getStaticDataManager().isShowing()) {
			Shared.getStaticDataManager().getReturnButton().performClick();
			return;
		}
		
//		if (isShowingCardLayout && cardLayout != null) {
//			cardLayout.hideCard();
//			return;
//		}
		
		if (multiARLayout != null && multiARLayout.getVisibility() == View.VISIBLE) {
			runOnUiThread(new Runnable() {
				public void run() {
					Log.d(TAG, "Hide multi AR view");
//					multiARLayout.setVisibility(View.GONE);
					multiARLayout.hideMultiARLayout();
					multiARLayout.clearJSONList();
					mCameraPreview.startCamera();
					mCameraPreview.startQuery();
				}
			});
			return;
		} 
		
		super.onBackPressed();
		finish();
	}
	
	/** Sets the values to Config class */
	private void saveConfig() {
		ARiseConfigs.PACKAGE_NAME			 = getApplicationContext().getPackageName(); 
		ARiseConfigs.SERVICE_BASE_URL		 = CameraActivity.this.getIntent().getExtras().getString("SERVICE_BASE_URL");
		ARiseConfigs.TRACKING_URL			 = CameraActivity.this.getIntent().getExtras().getString("TRACKING_URL");
		ARiseConfigs.TRACKING_APP_ID		 = CameraActivity.this.getIntent().getExtras().getString("TRACKING_APP_ID");
		ARiseConfigs.THEME_COLOR			 = CameraActivity.this.getIntent().getExtras().getInt("THEME_COLOR", Color.rgb(131, 13, 189));
		ARiseConfigs.LOGO					 = CameraActivity.this.getIntent().getExtras().getString("LOGO");
		ARiseConfigs.LOGO_ALPHA			 	 = CameraActivity.this.getIntent().getExtras().getFloat("LOGO_ALPHA", 1.0f);
		ARiseConfigs.GUIDE_IMAGE			 = CameraActivity.this.getIntent().getExtras().getString("GUIDE_IMAGE");
		ARiseConfigs.GUIDE_VIDEO_URL		 = CameraActivity.this.getIntent().getExtras().getString("GUIDE_VIDEO_URL");
		ARiseConfigs.TOOLTIP_IMAGE			 = CameraActivity.this.getIntent().getExtras().getString("TOOLTIP_IMAGE");
		ARiseConfigs.TOOLTIP_TEXT			 = CameraActivity.this.getIntent().getExtras().getString("TOOLTIP_TEXT");
		ARiseConfigs.BARCODE_SCANNING_ENABLE = CameraActivity.this.getIntent().getExtras().getBoolean("BARCODE_SCANNING_ENABLE");
		ARiseConfigs.LANGUAGE				 = CameraActivity.this.getIntent().getExtras().getString("LANGUAGE");
		ARiseConfigs.CLIENT_CODE_KEY		 = CameraActivity.this.getIntent().getExtras().getString("CLIENT_CODE_KEY", "");
		
		/************************
		 * CONFIG
		 ************************/
		DisplayMetrics metrics = new DisplayMetrics();
        CameraActivity.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ARiseConfigs.DEVICE_HEIGHT = metrics.heightPixels;
        ARiseConfigs.DEVICE_WIDTH  = metrics.widthPixels;

        TelephonyManager telephonyManager = (TelephonyManager) CameraActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
        ARiseConfigs.MOBILE_NETWORK_OPERATOR_NAME = telephonyManager.getNetworkOperatorName();
        
        String networkOperator = telephonyManager.getNetworkOperator();
        if (networkOperator != null && networkOperator.length() >= 3) {
        	ARiseConfigs.MOBILE_COUNTRY_CODE = networkOperator.substring(0, 3);
        	ARiseConfigs.MOBILE_NETWORK_CODE = networkOperator.substring(3);
        }
        
        ARiseUtils.initLanguageWithString(CameraActivity.this, CameraActivity.this.getIntent().getExtras().getString("LANGUAGE"));
        
        // call this to log the configs
        ARiseConfigs.logConfigs();
	}

	ApplicationListener applicationListener = new ApplicationListener() {

		private final static String TAG = "ARiseCameraActivity.applicationListener";

		private boolean appInitialized = false;
		
		private OrthographicCamera orthographicCamera;
		private PerspectiveCamera perspectiveCamera;
		
		private Environment environment;
		private GestureListener gestureListener;
		private SpriteBatch mSpriteBatch;
		private ModelBatch modelBatch;

		@Override
		public void resize(int width, int height) {
			
			// set up perspective camera, used for Augmented Reality part
			perspectiveCamera = new PerspectiveCamera(ARiseConfigs.FOV, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			perspectiveCamera.position.set(0f, 0f, 0f);
			perspectiveCamera.lookAt(0, 0, -1);
			perspectiveCamera.near = 20f;
			perspectiveCamera.far = 3000f;
			perspectiveCamera.update();
			
			// set up orthographic camera, used for AR static layer
			orthographicCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			orthographicCamera.position.set(0f, 0f, 1000f);
			orthographicCamera.lookAt(0, 0, -1);
			orthographicCamera.near = 20f;
			orthographicCamera.far = 3000f;
			orthographicCamera.update();
		}
		
		@Override
		public void render() {
			
			float t = Gdx.graphics.getDeltaTime();
			
			Gdx.graphics.getGL20().glClearColor(0, 0, 0, 1);
			Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			if (appInitialized) {
				// draw camera at background
				if (mCameraPreview != null && mCameraPreview.getVisibility() == View.GONE) {
					if (staticLayerLayout.getVisibility() == View.VISIBLE) {
						Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
						drawLastFrame();
					} else {
						drawCameraPreview();
						updateAverageFPS();
					}
				} else {
					resetAverageFPSBenchmark();
				}
				
				// render self-rotated 3D model
				if (staticLayerLayout.getVisibility() == View.VISIBLE) {
					renderSelfRotated3DModel(t);
				}
				
				// render overlay content
				renderOverlayContent();
			}
			
			Gdx.graphics.requestRendering(); // end of frame, render next frame
		}
		
		@Override
		public void pause() {
			
		}

		@Override
		public void dispose() {
 
		}
		
		@Override
		public void resume() {
			runOnUiThread(new Runnable() {
				public void run() {
					((ViewGroup) graphics.getView().getParent()).removeView(mCameraPreview);
					
					boolean shouldStop = false;
					if (Shared.getStaticDataManager().isShowing()) {
						shouldStop = true;
					}
					
					if (multiARLayout.getVisibility() == View.VISIBLE) {
						shouldStop = true;
					}
					
					if (Shared.getOverlayDataManager().getMode() != OverlayDataManager.OVERLAY_NONE || Shared.getStaticDataManager().isShowing()) {
						mCameraPreview = new CameraPreview(CameraActivity.this, true, shouldStop);
					} else {
						mCameraPreview = new CameraPreview(CameraActivity.this, false, shouldStop);
					}
					
					((ViewGroup) graphics.getView().getParent()).addView(mCameraPreview, 1);
					
					CameraActivity.this.checkAndRecreateViewsIfNecessary();
				}
			});
		}

		@Override
		public void create() {
			externalAssets = ARiseUtils.createExternalAssetManager();
			internalAssets = ARiseUtils.createInternalAssetManager();

			if (mCameraPreview == null) {
				loadUI();
			}
			
			mSpriteBatch = new SpriteBatch();

			sceneSetup();
			ARiseGLUtils.setOpenGLEnvironment();
			
			// libGDX renders frame by frame
			Gdx.graphics.setContinuousRendering(false);
		}	

		private void loadUI() {
			runOnUiThread(new Runnable() {
				public void run() {
					FrameLayout.LayoutParams btmFLayout = new FrameLayout.LayoutParams(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
					btmFLayout.gravity = Gravity.CENTER;

					mCameraPreview = new CameraPreview(CameraActivity.this, false, false);
					
					addContentView(mCameraPreview, btmFLayout);
					
					// overlay video
					Shared.getOverlayDataManager().setGLSurfaceView(new MyGLSurfaceView(CameraActivity.this));
					addContentView(Shared.getOverlayDataManager().getGLSurfaceView(), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					Shared.getOverlayDataManager().getGLSurfaceView().setVisibility(View.GONE);
					
					cameraUILayout = (RelativeLayout) View.inflate(CameraActivity.this, R.layout.camera_ui_layout, null);
					addContentView(cameraUILayout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					arStaticLayout = (RelativeLayout) findViewById(R.id.cameraPreviewScreen);
					
					staticLayerLayout = (RelativeLayout) View.inflate(CameraActivity.this, R.layout.static_layer_layout, null);
					addContentView(staticLayerLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					staticLayerLayout.setVisibility(View.GONE);
					
					multiARLayout = new MultiARView(CameraActivity.this);
					addContentView(multiARLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					multiARLayout.setVisibility(View.GONE);
					
					CameraActivityUI.createNewInstance(CameraActivity.this);
					
					appInitialized = true;
				}
			});
		}
		
		/********************************************************************************
		 * Sets up the scene:
		 * 		+ modelBatch		: the modelBatch used to render the 3D models
		 * 		+ environment		: the environment in the scene (lights)
		 * 		+ gestureListener	: the listener for touch input from user
		 */
		private void sceneSetup() {
			modelBatch = new ModelBatch();
			environment = new Environment();
	        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 0.8f));
	        environment.add(new DirectionalLight().set(1f, 1f, 1f, -10f, -10f, -10f));
	        
	        gestureListener = new GestureListener();
			Gdx.input.setInputProcessor(new GestureDetector(gestureListener));
		}
		
		Sprite lastNotNullFrameSprite = null;
		Sprite staticLayerBackgroundSprite = null;
		private void drawLastFrame() {
			if (mCameraPreview == null)
				return;
			
			Texture lastFrameTexture = mCameraPreview.generateLastFrameTexture();
			if (lastFrameTexture != null) {
				lastNotNullFrameSprite = new Sprite(lastFrameTexture);
				lastNotNullFrameSprite.setPosition((mCameraPreview.getCamera().getParameters().getPreviewSize().height - mCameraPreview.getCamera().getParameters().getPreviewSize().width) / 2,
						Gdx.graphics.getHeight() + (-mCameraPreview.getCamera().getParameters().getPreviewSize().width) + (mCameraPreview.getCamera().getParameters().getPreviewSize().width - mCameraPreview.getCamera().getParameters().getPreviewSize().height) / 2); // X(+Right,-Left) Y(-Down,+Up)
				lastNotNullFrameSprite.setSize(Gdx.graphics.getHeight(), Gdx.graphics.getWidth());
				lastNotNullFrameSprite.rotate(270f);
			}
			
			Texture a = Shared.getStaticDataManager().generateStaticLayerBackgroundTexture();
			if (a != null) {
				staticLayerBackgroundSprite = new Sprite(a);
				staticLayerBackgroundSprite.setPosition(0, 0);
				staticLayerBackgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			} 
			
			try {
				mSpriteBatch.begin();
				if (lastNotNullFrameSprite != null) {
					lastNotNullFrameSprite.draw(mSpriteBatch, 0.6f);
				}
				if (a != null) {
					staticLayerBackgroundSprite.draw(mSpriteBatch);
				}
			} catch (Exception e) {
				Log.e(TAG, "Error when drawing camera preview", e);
				e.printStackTrace();
			} finally {
				if (mSpriteBatch != null) {
					mSpriteBatch.end();
				}
			}
		}
		
		/********************************************************************************
		 * This method draws the texturized camera frame.
		 */
		private void drawCameraPreview() {
			if (mCameraTexture == null) return;
			if (mCameraPreview == null) return;
			
			mCameraTexture.texturizeCameraPreview(mCameraPreview.getYUVFrame());
			
			if (mCameraPreview != null && mCameraTexture.getCameraTexture() != null) {
				try {
					mSpriteBatch.begin();
					Sprite sprite = new Sprite(mCameraTexture.getCameraTexture());
					sprite.setPosition((mCameraPreview.getCamera().getParameters().getPreviewSize().height - mCameraPreview.getCamera().getParameters().getPreviewSize().width) / 2,
							Gdx.graphics.getHeight() + (-mCameraPreview.getCamera().getParameters().getPreviewSize().width) + (mCameraPreview.getCamera().getParameters().getPreviewSize().width - mCameraPreview.getCamera().getParameters().getPreviewSize().height) / 2); // X(+Right,-Left) Y(-Down,+Up)
					sprite.setSize(Gdx.graphics.getHeight(), Gdx.graphics.getWidth());
					sprite.rotate(270f);
					
					sprite.draw(mSpriteBatch);
				} catch (Exception e) {
					Log.e(TAG, "Error when drawing camera preview", e);
					e.printStackTrace();
				} finally {
					mSpriteBatch.end();
				}
			}
		}
		
		/********************************************************************************
		 * This method renders the self-rotated 3D models AR static screen.
		 */
		private void renderSelfRotated3DModel(float deltaTime) {
			if (Shared.getStaticDataManager().isLoading3DModel() &&
					externalAssets != null &&
					externalAssets.update() &&
					internalAssets != null &&
					internalAssets.update()) {
				Shared.getStaticDataManager().createStatic3DModels();
			}
			
			if (Shared.getStaticDataManager().getModelList() != null && Shared.getStaticDataManager().getModelList().size() > 0) {
				gestureListener.registerSelfRotatedInstances(Shared.getStaticDataManager().getModelList());
				
				for (Static3DModel instance : Shared.getStaticDataManager().getModelList()) {
					instance.updateWithTime(deltaTime);
				}
				
				modelBatch.begin(orthographicCamera);
				modelBatch.render(Shared.getStaticDataManager().getModelList(), environment);
				modelBatch.end();
			}
		}
		
		/********************************************************************************
		 * This method renders the overlay 3D models on the target image.
		 */
		private void renderOverlayContent() {
			try {
				
				if (Shared.getOverlayDataManager().getMode() == OverlayDataManager.OVERLAY_VIDEO) {
					try {
						Shared.getOverlayDataManager().getGLSurfaceView().showSurface();
						try {
							float[] tmp = new float[16];
							if (Shared.getTrackingEngineManager().isTracked()) {
								tmp = Shared.getTrackingEngineManager().getOverlayVideoMatrix();
								Shared.getOverlayDataManager().setAllowDisplay(true);
							} else {
								if (Shared.getOverlayDataManager().getAllowDisplay())
									tmp = Shared.getTrackingEngineManager().getOverlayVideoMatrix();
								else
									tmp = null;
							}
							Shared.getOverlayDataManager().getGLSurfaceView().getRenderer().setMVPMatrix(tmp);
						} catch (Exception e) {
							
						}
							
						Shared.getOverlayDataManager().getGLSurfaceView().requestRender();
					} catch (Exception e) {
						
					}
				} else {
					// hide surface
					try {
						Shared.getOverlayDataManager().getGLSurfaceView().hideSurface();
					} catch (Exception e) {
						
					}
					
					if (Shared.getOverlayDataManager().getMode() == OverlayDataManager.OVERLAY_3DMODEL) {
						if (Shared.getOverlayDataManager().isLoading3D() && externalAssets.update() && internalAssets.update())
							Shared.getOverlayDataManager().createOverlay3DModel();
						
						if (Shared.getOverlayDataManager().get3DModelList().size() > 0) {
							gestureListener.registerOverlayInstances(Shared.getOverlayDataManager().get3DModelList());
							
							for (Overlay3DModel instance : Shared.getOverlayDataManager().get3DModelList()) {
								instance.update();
								Shared.getTrackingEngineManager().update();
								Vector3 offsetTranslation = (new Vector3(instance.getPosition().x, instance.getPosition().y, 0)).rot(Shared.getTrackingEngineManager().getRotationMatrix());
								Log.i(TAG, "offsetTranslation: " + offsetTranslation.toString());
								instance.transform.mul(Shared.getTrackingEngineManager().getRotationMatrix()).trn(Shared.getTrackingEngineManager().getTranslationVector()).trn(offsetTranslation.x, offsetTranslation.y, 0);
							}
							modelBatch.begin(perspectiveCamera);
							try {
								modelBatch.render(Shared.getOverlayDataManager().get3DModelList(), environment);
							} catch (Exception e) {
								Log.e(TAG, "Exception occurs while rendering 3D overlay asset", e);
								e.printStackTrace();
							}
							modelBatch.end();
						}
					}
					
					if (Shared.getOverlayDataManager().getMode() == OverlayDataManager.OVERLAY_NONE) {
						try {
							Shared.getOverlayDataManager().getGLSurfaceView().hideSurface();
						} catch (Exception e) {
							
						}
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "Error when rendering overlay content", e);
				e.printStackTrace();
			}
		}
		
		private int frame = 0;
		private float averageFPS = 0;
		private Boolean slowWarningMessage = false;
		private boolean shouldStartBenchmark = false;
		private ARiseDialogOneButton slowMessageDialog; 
		private void updateAverageFPS() {
			if (Gdx.graphics.getFramesPerSecond() < 24) {
				shouldStartBenchmark = true;
			}
			
			if (!shouldStartBenchmark)
				return;
			
			if (shouldStartBenchmark) {
				if (frame >= 0 && frame < 50) {
					frame++;
					averageFPS = (averageFPS * (frame - 1) + Gdx.graphics.getFramesPerSecond()) / (float) frame;
				} else {
					synchronized (slowWarningMessage) {
						if (!slowWarningMessage && Shared.getOverlayDataManager().getMode() != OverlayDataManager.OVERLAY_NONE) {
							runOnUiThread(new Runnable() {
								public void run() {									
									
									// set title and message for different cases
									String title = "Slow camera!";
									String message = "Your device may not be optimal to use this feature.";
									
									if (0 <= averageFPS && averageFPS <= 6) {
										title = "Extremely slow camera!";
										message = "Your device may not be optimal to use this feature.";
									} else if (averageFPS > 6 && averageFPS <= 16) {
										title = "Slow camera!";
										message = "Your device may not be optimal to use this feature.";
									}
									
									// start showing the dialog of warning
									if (slowMessageDialog == null || !slowMessageDialog.isShowing()) {
										slowMessageDialog = new ARiseDialogOneButton(CameraActivity.this);
										slowMessageDialog.setThemeColor(ARiseConfigs.THEME_COLOR);
										slowMessageDialog.setTitleText(title);
										slowMessageDialog.setMessageText(message);
										slowMessageDialog.setButtonText("Close");
										slowMessageDialog.setButtonOnClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												slowMessageDialog.dismiss();
											}
										});
										slowMessageDialog.show();
									}
								}
							});
							slowWarningMessage = true;
						}
					}
				}
			}
		}
		
		private void resetAverageFPSBenchmark() {
			frame = 0;
			averageFPS = 0;
			shouldStartBenchmark = false;
			slowWarningMessage = false;
		}
	};

	/**
	 * Pre-processes the response from ARise server before showing the entry.
	 * 		+ Checking if there is something being shown
	 * 		+ Validate the JSON response (able to parse it?)
	 * 		+ Determine old/multi-AR format
	 * @param pRes		: The response received from ARise server  
	 */
//	public synchronized void preprocessJSONResponse(String pRes) {
	public synchronized void preprocessJSONResponse(String pRes, long clientStartTime, long clientEndTime, String recognitionToken, int timeout) {
		
		// some validations to check if there is nothing being displayed
		if (multiARLayout.getVisibility() == View.VISIBLE)
			return;
		if (Shared.getStaticDataManager().isShowing())
			return;
		if (Shared.getOverlayDataManager().getMode() != OverlayDataManager.OVERLAY_NONE)
			return;
		
		// parse JSON
		JSONObject pJSONObj = null;
		try {
			pJSONObj = new JSONObject(pRes);
		} catch (JSONException jsonException) {
			Log.e(TAG, "Error while converting string response into json objection", jsonException);
			jsonException.printStackTrace();
			pJSONObj = null;
		} catch (Exception e) {
			Log.e(TAG, "Unknown error while converting string response into json objection", e);
			e.printStackTrace();
			pJSONObj = null;
		}
		
		// parse failed
		if (pJSONObj == null)
			return;
		
		/**
		 * TODO:
		 * 		Comment this before releasing the sdk
		 */
		if (pJSONObj.has("debug")) {
			JSONObject debugJson = pJSONObj.optJSONObject("debug");
			if (debugJson != null) {
				long serverStartTime = (long) (debugJson.optDouble("start_time", -1.0) * 1000);
				long serverEndTime = (long) (debugJson.optDouble("finish_time", -1.0) * 1000);
				String serverRecognitionToken = debugJson.optString("recognitionToken", "");
				if (recognitionSpeedLog != null) 
					recognitionSpeedLog.addLog(clientStartTime, clientEndTime, serverStartTime, serverEndTime, recognitionToken, serverRecognitionToken, timeout);
			}
		}
		
		// check JSON format: using multi-AR or not?
		boolean isMultipleARFormat = true;
		if (pJSONObj.has("success") && pJSONObj.has("results") && pJSONObj.has("size")) { // multiple AR
			isMultipleARFormat = true;
		} else if (pJSONObj.has("isnap_id")) {
			isMultipleARFormat = false;
		} else {
			return;
		}
		
		if (isMultipleARFormat) {
			if (!pJSONObj.optBoolean("success")) {
				Log.e(TAG, "Error: " + pJSONObj.optString("error"));
			} else {
				int size = Integer.parseInt(pJSONObj.optString("size"));
	       		if (size == 0) { // no AR
	       			
	       		} else if (size == 1) { // single AR
	       			final ArrayList<JSONObject> list = new ArrayList<JSONObject>();
	       			try {
		       			JSONArray entries = pJSONObj.getJSONArray("results");
		       			JSONObject theOnlyJSONObj = (JSONObject) entries.get(0);
		    			list.add(theOnlyJSONObj);
	       			} catch (JSONException jsonException) {
	       				Log.e(TAG, "Error while parsing JSON in multiple ARs", jsonException);
	       				jsonException.printStackTrace();
	       			}
	       			handleMultipleARs(list);
	       		} else if (size > 1) { // multi ARs
	       			final ArrayList<JSONObject> list = new ArrayList<JSONObject>();
	       			try {
		       			JSONArray entries = pJSONObj.getJSONArray("results");
		       			for (int i = 0; i < size; i++) {
		       				JSONObject curJSONObject = entries.getJSONObject(i);
		       				list.add(curJSONObject);
		       			}
	       			} catch (JSONException jsonException) {
	       				Log.e(TAG, "Error while parsing JSON in multiple ARs", jsonException);
	       				jsonException.printStackTrace();
	       			}
	       			handleMultipleARs(list);
	       		}
			}
		} else {
			runOnUiThread(new Runnable() {
				public void run() {
					multiARLayout.setVisibility(View.GONE);
				}
			});
			CameraActivity.this.setJSON(pJSONObj, pJSONObj.toString());
		}
	}
	
	private synchronized void handleMultipleARs(final ArrayList<JSONObject> jsonList) {
		if (jsonList.size() > 1) {
			runOnUiThread(new Runnable() {
				public void run() {
//					multiARLayout.setVisibility(View.VISIBLE);
					multiARLayout.setJSONList(jsonList);
					multiARLayout.setCameraFrame(mCameraPreview.getYUVFrame(),
							mCameraPreview.getCamera().getParameters().getPreviewSize().width,
							mCameraPreview.getCamera().getParameters().getPreviewSize().height);
					mCameraPreview.stopQuery();
					mCameraPreview.stopCamera();
					multiARLayout.showMultiARLayout();
				}
			});
		} else if (jsonList.size() == 1) {
			JSONObject chosenAR = jsonList.get(0);
			runOnUiThread(new Runnable() {
				public void run() {
//					multiARLayout.setVisibility(View.GONE);
					multiARLayout.hideMultiARLayout();
				}
			});
			CameraActivity.this.setJSON(chosenAR, chosenAR.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void setJSON(JSONObject jsonObj, String response) {
		try {
			if (latestJSONObject != null && jsonObj.getString("isnap_id").equals(latestJSONObject.getString("isnap_id"))) {
				// ignore if isnap_id does not change
			} else {
				latestJSONObject = jsonObj;
				
				// add to history
				String iSnapId = latestJSONObject.optString("isnap_id", null);
	            String posterURL = latestJSONObject.optString("poster_url", null);
	            String title = latestJSONObject.optString("name", null);
	            
	        	if (iSnapId != null) {
		            HashMap<String, String> item = new HashMap<String, String>();
		            item.put("id", iSnapId);
		            item.put("posterURL", posterURL);
		            item.put("arContent", response);
		            item.put("title", title);
		            if ((posterURL != null) && (title != null)) {
		                (new AddHistory(CameraActivity.this)).execute(item);
		            }
	        	}
	        	// end add to history
				
				boolean hasOverlayData = false;
				try {
		    		JSONArray jsonArray = latestJSONObject.getJSONArray("assets");
		    		for (int i = 0; i < jsonArray.length(); i++) {
		    			try {
							JSONObject obj = jsonArray.getJSONObject(i);
							int type = Integer.valueOf(obj.getString("type"));
							
							if (type == ARiseConfigs.AR_TYPE_OVERLAY_VIDEO || type == ARiseConfigs.AR_TYPE_OVERLAY_3D_MODEL) {
								hasOverlayData = true;
							}
		    			} catch (Exception e) {
		    				Log.e(TAG, "Error while checking if having overlay data", e);
		    				e.printStackTrace();
		    			}
		    		}
		    	} catch (JSONException e) {
		    		Log.e(TAG, "Error while parsing json of field \"assets\" for checking if having overlay data", e);
		    		e.printStackTrace();
		    	}

				if (!hasOverlayData) {
					Shared.getCameraActivityUI().hide();
					mCameraPreview.stopQuery();
					mCameraPreview.stopCamera();
					processStaticLayer();
				} else {
					Shared.getTrackingEngineManager().startNewPositionTrackingThreadForISnapId(CameraActivity.this, iSnapId);
					runOnUiThread(new Runnable() {
						public void run() {
							try {
								mCameraPreview.stopQuery();
								mCameraPreview.startCamera();
								mCameraPreview.setVisibility(View.GONE);
							} catch (Exception e) {
								Log.e(TAG, "Error while starting camera for AR mode", e);
								e.printStackTrace();
							}
						}
					});
					processOverlayContents();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error in setting JSON", e);
			e.printStackTrace();
			latestJSONObject = null;
		}
	}
	
	private void processOverlayContents() {
		Shared.getCameraActivityUI().hide();		
		OverlayDataManager.createNewInstance(CameraActivity.this, latestJSONObject, externalAssets, internalAssets);
	}
	
	private void processStaticLayer() {
		try {
			
			// ADDED ON 24-04-1014
			Shared.getTrackingEngineManager().stopPositionTrackingThread();
			
			String iSnapId = latestJSONObject.optString("isnap_id", null);
            
        	if (iSnapId != null) {
				
	            StaticDataManager.createNewInstance(CameraActivity.this, latestJSONObject, externalAssets, internalAssets, staticLayerLayout);
	            
	            Log.i(TAG, "\t---- START PARSING DIRECTLINK ----");
	            
	            // parse directLink object
	            JSONObject directLinkObj = null;
	            JSONObject assetForDirectLink = null;
	            int delay = -1;
				boolean callback = false;
				boolean autoplay = true;
	            try {
	            	directLinkObj = latestJSONObject.getJSONObject("directLink");
	            	int dlAssetIndex = directLinkObj.getInt("assetIndex");
	            	int dlAssetId = directLinkObj.getInt("assetId");
	            	delay = directLinkObj.getInt("delay");
	            	callback = directLinkObj.getBoolean("callback");
	            	autoplay = directLinkObj.getBoolean("autoplay");
	            	
	            	JSONArray assetList = latestJSONObject.getJSONArray("assets");
	            	if (((JSONObject) assetList.get(dlAssetIndex)).getInt("assetId") == dlAssetId) { // match asset id
	            		Log.i(TAG, "Matched assetId: " + dlAssetId);
	            		assetForDirectLink = (JSONObject) assetList.get(dlAssetIndex); 
	            	} else {
	            		Log.e(TAG, "Not matched assetId: " + ((JSONObject) assetList.get(dlAssetIndex)).getInt("assetId") + " - " + dlAssetId);
	            	}
	            } catch (JSONException ex) {
	            	Log.e(TAG, "Error occurred while parsing directLink json", ex);
	            	ex.printStackTrace();
	            	assetForDirectLink = null;
	            }
				
	            if (assetForDirectLink != null) {
	            	int type = -1;
	            	try {
	            		type = Integer.valueOf(assetForDirectLink.getString("type"));
	            	} catch (JSONException e) {
	            		Log.e(TAG, "Error while parsing type of asset used for directLink", e);
	            		e.printStackTrace();
	            		type = -1;
	            	} catch (NumberFormatException e) {
	            		Log.e(TAG, "Error while casting type from String to Integer", e);
	            		e.printStackTrace();
	            		type = -1;
	            	}
	            	Log.i(TAG, "Type of asset used for directLink: " + type);
	            	
	            	if (type != -1 && type != ARiseConfigs.AR_TYPE_OVERLAY_VIDEO && type != ARiseConfigs.AR_TYPE_OVERLAY_3D_MODEL) {
	            		displayARStaticLayer();
	            		
	            		// try to parse the URL of asset used for directLink
	            		String link = null;
	            		try {
	            			link = assetForDirectLink.getString("url");
	            		} catch (JSONException e) {
	            			Log.e(TAG, "Error occurred while parsing field \"url\" of asset used for directLink", e);
	            			e.printStackTrace();
	            			link = null;
	            		} finally {
	            			if (link == null) {
	            				try {
	            					link = assetForDirectLink.getString("asset");
	            				} catch (JSONException e) {
	            					Log.e(TAG, "Error occurred while parsing field \"asset\" of asset used for directLink", e);
	            					e.printStackTrace();
	            					link = null;
	            				}
	            			}
	            		}
	            		
	            		if (link != null && link.length() > 0) {
	            			if (type == ARiseConfigs.AR_TYPE_VIDEO || link.endsWith(".mp4")) {
	            				if (ARiseUtils.isYoutubeURL(link)) {
	            					String youtubeVideoId = ARiseUtils.extractYoutubeId(link);
	            					Bundle args = new Bundle();
	                                args.putString("videoId", youtubeVideoId);
	                                args.putBoolean("autoplay", autoplay);
	                                args.putBoolean("callback", callback);
	                                args.putInt("delay", delay);
	                                
	                                Intent i = new Intent(CameraActivity.this, YouTubeVideoPlayer.class);
	                                i.putExtras(args);
	                                startActivity(i);
	            				} else {
	            					Bundle args = new Bundle();
	                                args.putString("url", link);
	                                args.putBoolean("autoplay", autoplay);
	                                args.putBoolean("callback", callback);
	                                args.putInt("delay", delay);
	                                
	                                Intent i = new Intent(CameraActivity.this, MP4VideoPlayer.class);
	                                i.putExtras(args);
	                                startActivity(i);
	            				}
	            				sendInteractionTracking(iSnapId, "click", "video", link);
	            			} else {
	            				if (ARiseUtils.isYoutubeURL(link)) {
	                            	String youtubeVideoId = ARiseUtils.extractYoutubeId(link);
	                                Bundle args = new Bundle();
	                                args.putString("videoId", youtubeVideoId);
	                                args.putBoolean("autoplay", autoplay);
	                                args.putBoolean("callback", callback);
	                                args.putInt("delay", delay);
	                                Intent i = new Intent(CameraActivity.this, YouTubeVideoPlayer.class);
	                                i.putExtras(args);
	                                startActivity(i);
	                            } else {
	                                Bundle args = new Bundle();
	                                args.putString("weblink", link);
	                                args.putBoolean("callback", callback);
	                                args.putInt("delay", delay);
	                                Intent i = new Intent(CameraActivity.this, WebActivity.class);
	                                i.putExtras(args);
	                                startActivity(i);
	                            }
	            			}
	            		} else {
	            			Log.e(TAG, "Link will be opened in directLink is null or empty ===> display AR static layer");
	            			displayARStaticLayer();
	            		}
	            	} else { // type of the asset is not suitable for directLink
	            		Log.e(TAG, "This type of asset cannot be opened automatically  ===> display AR static layer");
	            		displayARStaticLayer();
	            	}
	            } else { // cannot find asset used for directLink
	            	Log.e(TAG, "Cannot find the asset used for directLink ===> display AR static layer");
	            	displayARStaticLayer();
	            }
			}
		} catch (Exception e) {
			Log.e(TAG, "error", e);
			e.printStackTrace();
		}
	}
	
	/** Displays AR static layer. */
	private void displayARStaticLayer() {
		runOnUiThread(new Runnable() {
			public void run() {
				mCameraPreview.setVisibility(View.GONE);
			}
		});
		Shared.getStaticDataManager().showARLayout();
	}
	
	/**
	 * Sends interaction tracking to server.
	 * @param ariseId	: the iSnap id
	 * @param trigger	: the interaction type
	 * @param object	: the type of the interactive contents
	 * @param targetURL	: the URL triggered by the interactive content
	 */
	private void sendInteractionTracking(final String ariseId, final String trigger, final String object, final String targetURL) {
        run(new Runnable() {
        	public void run() {
        		ARiseTracking.trackInteraction(CameraActivity.this, ARiseConfigs.TRACKING_URL, ARiseTracking.TRACKING_INTERACTION, ariseId, trigger, object, targetURL);
        	}
        });
    }
	
	public void deleteLatestJSONObject() {
		latestJSONObject = null;
	}
	
	public void finishInitializingNewCamera(android.hardware.Camera camera) {
		if (camera != null) {
			Shared.getTrackingEngineManager().setPreviewSize(camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height);
			mCameraTexture = new CameraTexture(camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height);
		}
	}
	
	/** Check GPS location */
	private void checkGPS() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!stopCheckGPS) {
					stopCheckGPS = pref.getBoolean("stop_check_gps", false);
				}
				
				GPSTracker gps = new GPSTracker(CameraActivity.this);
				if (gps.canGetLocation()) {
					ARiseConfigs.LATITUDE = gps.getLatitude();
					ARiseConfigs.LONGITUDE = gps.getLongitude();
				} else {
					if (!stopCheckGPS) {
						gps.showSettingsAlert();
						stopCheckGPS = true;
						editor.putBoolean("stop_check_gps", true);
						editor.commit();
					}
				}
			}
		});
	}
	
	public void onStaticLayerReturnButtonClicked() {
		if (staticLayerLayout == null)
			return;
		
		latestJSONObject = null;
		if (multiARLayout != null && multiARLayout.shouldDisplay()) { // back to multi AR selection view
			mCameraPreview.stopQuery();
			mCameraPreview.stopCamera();
			runOnUiThread(new Runnable() {
				public void run() {
					mCameraPreview.setVisibility(View.VISIBLE);
//					multiARLayout.setVisibility(View.VISIBLE);
					multiARLayout.showMultiARLayout();
				}
			});
		} else {
			mCameraPreview.startCamera();
			mCameraPreview.startQuery();
			runOnUiThread(new Runnable() {
				public void run() {
					mCameraPreview.setVisibility(View.VISIBLE);
//					multiARLayout.setVisibility(View.GONE);
					multiARLayout.hideMultiARLayout();
				}
			});
		}
		Shared.getCameraActivityUI().show();
	}
	
	public void onOverlayLayerReturnButtonClicked() {
		
		Shared.getTrackingEngineManager().stopPositionTrackingThread();
		Shared.getOverlayDataManager().hideOverlayLayer();
		Shared.getOverlayDataManager().clearARData();
		Shared.getCameraActivityUI().show();
		
		deleteLatestJSONObject();
		
		if (multiARLayout != null && multiARLayout.shouldDisplay()) { // back to multi AR selection view
			mCameraPreview.stopQuery();
			mCameraPreview.stopCamera();
			runOnUiThread(new Runnable() {
				public void run() {
					mCameraPreview.setVisibility(View.VISIBLE);
//					multiARLayout.setVisibility(View.VISIBLE);
					multiARLayout.showMultiARLayout();
				}
			});
		} else {
			mCameraPreview.startCamera();
			mCameraPreview.startQuery();
			runOnUiThread(new Runnable() {
				public void run() {
					mCameraPreview.setVisibility(View.VISIBLE);
//					multiARLayout.setVisibility(View.GONE);
					multiARLayout.hideMultiARLayout();
				}
			});
		}
	}
	
	public void onOverlayLayerGoToStaticLayerButtonClicked() {
		Shared.getTrackingEngineManager().stopPositionTrackingThread();
		Shared.getOverlayDataManager().hideOverlayLayer();
		Shared.getOverlayDataManager().clearARData();
		
		mCameraPreview.stopQuery();
		mCameraPreview.stopCamera();
		processStaticLayer();
	}
	
	private void checkAndRecreateViewsIfNecessary() {
		if (((ViewGroup) graphics.getView().getParent()).getChildCount() >= 3) {
			if ((((ViewGroup) graphics.getView().getParent()).getChildAt(2) == null)) {
				try {
					((ViewGroup) graphics.getView().getParent()).removeViewAt(2);
					Shared.getOverlayDataManager().setGLSurfaceView(new MyGLSurfaceView(CameraActivity.this));
					addContentView(Shared.getOverlayDataManager().getGLSurfaceView(), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					Shared.getOverlayDataManager().getGLSurfaceView().setVisibility(View.GONE);
				} catch (Exception e) {
					Log.e(TAG, "Error while re-creating camera UI layout", e);
					e.printStackTrace();
				}
			}
		} else {
			try {
				Shared.getOverlayDataManager().setGLSurfaceView(new MyGLSurfaceView(CameraActivity.this));
				addContentView(Shared.getOverlayDataManager().getGLSurfaceView(), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				Shared.getOverlayDataManager().getGLSurfaceView().setVisibility(View.GONE);
			} catch (Exception e) {
				Log.e(TAG, "Error while re-creating camera UI layout", e);
				e.printStackTrace();
			}
		}

		if (((ViewGroup) graphics.getView().getParent()).getChildCount() >= 4) {
			if ((((ViewGroup) graphics.getView().getParent()).getChildAt(3) == null)) {
				try {
					((ViewGroup) graphics.getView().getParent()).removeViewAt(3);
					cameraUILayout = (RelativeLayout) View.inflate(CameraActivity.this, R.layout.camera_ui_layout, null);
					addContentView(cameraUILayout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					arStaticLayout = (RelativeLayout) findViewById(R.id.cameraPreviewScreen);
					CameraActivityUI.createNewInstance(CameraActivity.this);
				} catch (Exception e) {
					Log.e(TAG, "Error while re-creating camera UI layout", e);
					e.printStackTrace();
				}
			}
		} else {
			try {
				cameraUILayout = (RelativeLayout) View.inflate(CameraActivity.this, R.layout.camera_ui_layout, null);
				addContentView(cameraUILayout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				arStaticLayout = (RelativeLayout) findViewById(R.id.cameraPreviewScreen);
				CameraActivityUI.createNewInstance(CameraActivity.this);
			} catch (Exception e) {
				Log.e(TAG, "Error while re-creating camera UI layout", e);
				e.printStackTrace();
			}
		}
		
		if (((ViewGroup) graphics.getView().getParent()).getChildCount() >= 5) {
			if ((((ViewGroup) graphics.getView().getParent()).getChildAt(4) == null)) {
				try {
					((ViewGroup) graphics.getView().getParent()).removeViewAt(4);
					staticLayerLayout = (RelativeLayout) View.inflate(CameraActivity.this, R.layout.static_layer_layout, null);
					addContentView(staticLayerLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					staticLayerLayout.setVisibility(View.GONE);
				} catch (Exception e) {
					Log.e(TAG, "Error while re-creating static layer", e);
					e.printStackTrace();
				}
			}
		} else {
			try {
				staticLayerLayout = (RelativeLayout) View.inflate(CameraActivity.this, R.layout.static_layer_layout, null);
				addContentView(staticLayerLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				staticLayerLayout.setVisibility(View.GONE);
			} catch (Exception e) {
				Log.e(TAG, "Error while re-creating static layer", e);
				e.printStackTrace();
			}
		}
		
		if (((ViewGroup) graphics.getView().getParent()).getChildCount() >= 6) {
			if ((((ViewGroup) graphics.getView().getParent()).getChildAt(5) == null)) {
				try {
					((ViewGroup) graphics.getView().getParent()).removeViewAt(5);
					multiARLayout = new MultiARView(CameraActivity.this);
					addContentView(multiARLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					multiARLayout.setVisibility(View.GONE);
				} catch (Exception e) {
					Log.e(TAG, "Error while re-creating static layer", e);
					e.printStackTrace();
				}
			}
		} else {
			try {
				multiARLayout = new MultiARView(CameraActivity.this);
				addContentView(multiARLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				multiARLayout.setVisibility(View.GONE);
			} catch (Exception e) {
				Log.e(TAG, "Error while re-creating static layer", e);
				e.printStackTrace();
			}
		}
	}
	
	public void onSettingButtonPressed() {
		Intent settingPageIntent = new Intent(CameraActivity.this, SettingPage.class);
		Bundle settingPageBundle = new Bundle();
		settingPageBundle.putByteArray("lastCameraFrame", mCameraPreview.getYUVFrame());
		settingPageBundle.putInt("cameraPreviewWidth", mCameraPreview.getCamera().getParameters().getPreviewSize().width);
		settingPageBundle.putInt("cameraPreviewHeight", mCameraPreview.getCamera().getParameters().getPreviewSize().height);
		settingPageIntent.putExtras(settingPageBundle);
		startActivity(settingPageIntent);
		overridePendingTransition(R.anim.intent_fade_in, R.anim.intent_fade_out);
	}
	
	public void onHistoryButtonPressed() {
		Intent historyPageIntent = new Intent(CameraActivity.this, HistoryPage.class);
		Bundle historyPageBundle = new Bundle();
		historyPageBundle.putByteArray("lastCameraFrame", mCameraPreview.getYUVFrame());
		historyPageBundle.putInt("cameraPreviewWidth", mCameraPreview.getCamera().getParameters().getPreviewSize().width);
		historyPageBundle.putInt("cameraPreviewHeight", mCameraPreview.getCamera().getParameters().getPreviewSize().height);
		historyPageIntent.putExtras(historyPageBundle);
		startActivity(historyPageIntent);
		overridePendingTransition(R.anim.intent_fade_in, R.anim.intent_fade_out);
	}
	
	public void startCamera() {
		mCameraPreview.startCamera();
		mCameraPreview.startQuery();
	}
	
	public void stopCamera() {
		mCameraPreview.stopQuery();
		mCameraPreview.stopCamera();
	}
}