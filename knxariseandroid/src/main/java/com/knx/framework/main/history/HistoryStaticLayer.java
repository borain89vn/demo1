package com.knx.framework.main.history;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.RelativeLayout;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.input.GestureDetector;
import com.knx.framework.R;
import com.knx.framework.arcontents.GestureListener;
import com.knx.framework.arcontents.StaticDataManager;
import com.knx.framework.arcontents.old.Static3DModel;
import com.knx.framework.main.IStaticLayerParent;
import com.knx.framework.main.Shared;
import com.knx.framework.task.DownloadPoster;
import com.knx.framework.utils.ARiseGLUtils;
import com.knx.framework.utils.ARiseUtils;


/**
 * This class manages the AR static contents to display in history page
 * @author Le Vu
 */
public class HistoryStaticLayer extends AndroidApplication implements IStaticLayerParent {
	
	private String json;
	private JSONObject jsonObj;
	
	private RelativeLayout staticLayerLayout;
	
	private StaticDataManager arManager = StaticDataManager.getSingletonInstance();
	
	private AssetManager externalAssets = ARiseUtils.createExternalAssetManager();
	private AssetManager internalAssets = ARiseUtils.createInternalAssetManager();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		json = getIntent().getExtras().getString("json");
		try {
			jsonObj = new JSONObject(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		initialize(applicationListener, Shared.getAndroidApplicationConfiguration());

		if (graphics.getView() instanceof SurfaceView) {
			SurfaceView glView = (SurfaceView) graphics.getView();
			// force alpha channel - I'm not sure we need this as the GL surface is already using alpha channel
			glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}
		
		// we don't want the screen to turn off during the long image saving process 
		graphics.getView().setKeepScreenOn(true);
		
		StaticDataManager.destroySingletonInstance();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		StaticDataManager.destroySingletonInstance();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		arManager.getReturnButton().performClick();
	}

	ApplicationListener applicationListener = new ApplicationListener() {

		private final static String TAG = "ARiseStaticLayer.FromHistoryPage.applicationListener";
		
		private TimerTask checkPosterTask;
		private Timer checkPosterTimer;
		private FileHandle posterFileHandle;
		
		private GestureListener gestureListener;
		
		private boolean uiLoaded = false;
		
		private int sceneWidth, sceneHeight;
		private OrthographicCamera cam;
		private Environment environment;
		
		private ModelBatch modelBatch;
		
		private SpriteBatch spriteBatch;
		private Texture posterTexture;

		@Override
		public void resize(int width, int height) {
			sceneWidth = width;
			sceneHeight = height;
			
			cam = new OrthographicCamera(sceneWidth, sceneHeight);
			cam.position.set(0f, 0f, 1000f);
			cam.lookAt(0, 0, -1);
			cam.near = 20f;
			cam.far = 3000f;
			cam.update();
		}

		// Render the screen (including the camera preview and the models)
		@Override
		public void render() {
			
			float t = Gdx.graphics.getDeltaTime();
			
			Gdx.graphics.getGL20().glClearColor(0, 0, 0, 1);
			Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			drawBackgroundPoster();
			
			if (arManager.isLoading3DModel() && externalAssets != null && externalAssets.update() && internalAssets != null && internalAssets.update()) {
				arManager.createStatic3DModels();
			}
			
			if (arManager.getModelList() != null && arManager.getModelList().size() > 0) {
				gestureListener.registerSelfRotatedInstances(arManager.getModelList());
				
				for (Static3DModel instance : arManager.getModelList()) {
					instance.updateWithTime(t);
				}
				
				modelBatch.begin(cam);
				modelBatch.render(arManager.getModelList(), environment);
				modelBatch.end();
			}
			
			Gdx.graphics.requestRendering();
		}
		
		@Override
		public void pause() {
			
		}

		@Override
		public void dispose() { 
			 stopCheckingPoster();
		}
		
		@Override
		public void resume() {
			
		}

		@Override
		public void create() {
			ARiseGLUtils.setOpenGLEnvironment();
			
			Gdx.graphics.setContinuousRendering(false);
			
			loadUI();
			blockThread(); // wait until UI is loaded
			
			modelBatch = new ModelBatch();
			spriteBatch = new SpriteBatch();
			environment = new Environment();
	        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 0.4f));
	        environment.add(new DirectionalLight().set(1f, 1f, 1f, -10f, -10f, -10f));
	        gestureListener = new GestureListener();
			Gdx.input.setInputProcessor(new GestureDetector(gestureListener));
			
	        processJSON();
		}
		
		private void loadUI() {
			
			// load dimmed poster
			setBackgroundPoster();
			
			// add close AR button and bookmark button
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					
					addContentView(View.inflate(HistoryStaticLayer.this, R.layout.history_static_layer, null), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					
					staticLayerLayout = (RelativeLayout) View.inflate(HistoryStaticLayer.this, R.layout.static_layer_layout, null);
					addContentView(staticLayerLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					staticLayerLayout.setVisibility(View.GONE);
					
					unblockThread();
				}
			});
			
		}

		private Sprite staticLayerBackgroundSprite;
		private void drawBackgroundPoster() {
			if (posterTexture == null) {
				try {
					posterTexture = new Texture(posterFileHandle);
				} catch (Exception e) {
					
				}
			}
			
			Texture a = Shared.getStaticDataManager().generateStaticLayerBackgroundTexture();
			if (a != null) {
				staticLayerBackgroundSprite = new Sprite(a);
				staticLayerBackgroundSprite.setPosition(0, 0);
				staticLayerBackgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			}
			
			try {
				spriteBatch.begin();
				if (posterTexture != null) {
					Sprite sprite = new Sprite(posterTexture);
					sprite.setSize(sceneWidth, ((float) sprite.getHeight() * sceneWidth) / sprite.getWidth());
					sprite.translateY((sceneHeight - ((float) sprite.getHeight() * sceneWidth) / sprite.getWidth()) / 2);
					sprite.draw(spriteBatch, 0.6f);
				}
				if (a != null) {
					staticLayerBackgroundSprite.draw(spriteBatch);
				}
			} catch (Exception e) {
				Log.e(TAG, "Error occurs while drawing background poster in history entry", e);
				e.printStackTrace();
			} finally {
				if (spriteBatch != null)
					spriteBatch.end();
			}
		}
		
		private void setBackgroundPoster() {
			if (posterTexture != null)
				return;
			
			// poster is null, scheduling to check if the poster image is downloaded
			checkPosterTimer = new Timer();
			checkPosterTask = new TimerTask() {
				@Override
				public void run() {
					try {
						String posterLink = jsonObj.getString("poster_url");
						File file = new File(Shared.getPosterDir(HistoryStaticLayer.this) + "/" + posterLink.hashCode());
						if (file.exists()) {
							posterFileHandle = new FileHandle(file);
							stopCheckingPoster();
						} else {
							(new DownloadPoster(HistoryStaticLayer.this)).execute(posterLink);
						}
					} catch (JSONException e) {
						
					}
				}
			};
			checkPosterTimer.scheduleAtFixedRate(checkPosterTask, 0, 500);
		}
		
		private synchronized void stopCheckingPoster() {
			if (checkPosterTimer != null) {
				checkPosterTimer.cancel();
				checkPosterTimer.purge();
				checkPosterTimer = null;
			}
		}
		
		private synchronized void blockThread() {
			try {
				while (!uiLoaded) {
					wait();
				}
			} catch (InterruptedException e) {
				Log.e(TAG, "Error when blocking thread until finish loading UI", e);
				e.printStackTrace();
			}
		}
		
		private synchronized void unblockThread() {
			uiLoaded = true;
			this.notify();
		}
		
		private void processJSON() {
			if (json == null || jsonObj == null)
				return;
		
			StaticDataManager.createNewInstance(HistoryStaticLayer.this, jsonObj, externalAssets, internalAssets, staticLayerLayout);
			arManager = StaticDataManager.getSingletonInstance();
			arManager.showARLayout();
		}
	};

	@Override
	public void onStaticLayerReturnButtonClicked() {
		finish();
	}
}