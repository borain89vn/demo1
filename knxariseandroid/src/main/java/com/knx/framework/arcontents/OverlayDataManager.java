package com.knx.framework.arcontents;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector2;
import com.knx.framework.R;
import com.knx.framework.arcontents.overlay.MyGLSurfaceView;
import com.knx.framework.arcontents.overlay.Overlay3DModel;
import com.knx.framework.arcontents.overlay.Overlay3DModelData;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.helper.LangPref;
import com.knx.framework.main.IOverlayLayerParent;
import com.knx.framework.task.Download3DModelPackage;
import com.knx.framework.videoplayer.MP4VideoPlayer;

public class OverlayDataManager {
	
	private final String TAG = "ARiseOverlayDataManager";
	
	public static final int OVERLAY_NONE = 0;
	public static final int OVERLAY_VIDEO = 1;
	public static final int OVERLAY_3DMODEL = 2;
	
	private Context context;
	private MyGLSurfaceView glSurfaceView;
	
	private ProgressDialog mDialog;
	private boolean dialogShown;
	
	private JSONObject jsonObject;	// latest json object
	
	private AssetManager externalAssets;
	private AssetManager internalAssets;
	
	private ArrayList<Thread> downloadThreadListFor3DModelPackage;
	private ArrayList<Overlay3DModelData> modelDataList;
	private ArrayList<Overlay3DModel> modelList;
	private boolean isLoading3D;
	
	private ImageButton closingOverlayBtn, goToARStaticLayerBtn, fullscreenBtn;
	private LinearLayout uiPanel;
	
	private int mode;
	private final int MARGIN = 50; // half of distance between two buttons in panel
	
	private static OverlayDataManager singletonInstance;
    public static OverlayDataManager getSingletonInstance() {
    	if (singletonInstance == null) {
    		
    		singletonInstance = new OverlayDataManager();
    		
    		singletonInstance.context = null;
    		singletonInstance.glSurfaceView = null;
    		singletonInstance.jsonObject = null;
    		
//    		singletonInstance.externalAssets = ARiseUtils.createExternalAssetManager();
//    		singletonInstance.internalAssets = ARiseUtils.createInternalAssetManager();
    		
    		singletonInstance.externalAssets = null;
    		singletonInstance.internalAssets = null;
    		
    		singletonInstance.modelList = new ArrayList<Overlay3DModel>();
    		singletonInstance.isLoading3D = false;
    	}
    	return singletonInstance;
    }
    
    public static synchronized void createNewInstance(Context cxt, JSONObject jsonObj, AssetManager extAssetManager, AssetManager intAssetManager) {
    	if (singletonInstance == null) {
    		singletonInstance = getSingletonInstance();
    	}
    	
    	if (singletonInstance.checkShouldCreateNewInstance(jsonObj)) { // only create when detecting new class
	    	try {
	    		Log.i(singletonInstance.TAG, "Create new instance of overlay layer");
	    		singletonInstance.context = cxt;
	    		singletonInstance.jsonObject = jsonObj;
	    		
	    		singletonInstance.externalAssets = extAssetManager;
	    		singletonInstance.internalAssets = intAssetManager;
	    		
	    		((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
	    			@Override
	    			public void run() {
	    				if (!singletonInstance.dialogShown) {
	    					singletonInstance.dialogShown = true;
		    				singletonInstance.mDialog = new ProgressDialog(singletonInstance.context, AlertDialog.THEME_HOLO_DARK);
							singletonInstance.mDialog.setMessage(LangPref.TXTLOADING);
							singletonInstance.mDialog.setCancelable(false);
							singletonInstance.mDialog.show();
	    				}
	    			}
	    		});
	    		
	    		singletonInstance.loadUI();
	    		singletonInstance.clearARData();
	    		singletonInstance.processJSONObject();
	    	} catch (Exception e) {
	    		Log.e(singletonInstance.TAG, "Cannot create new instance of AR screen", e);
	    		e.printStackTrace();
	    		singletonInstance.hideProgressDialog();
	    	}
    	}
    }
    
    public static synchronized void destroySingletonInstance() {
    	singletonInstance = null;
    }
    
    private synchronized boolean checkShouldCreateNewInstance(JSONObject jsonObj) {
    	boolean shouldCreateNewInstance = false;
    	
    	// check new JSON
    	boolean isNewJSONValid = false;
    	String newISnapID = "-1";
    	try {
    		newISnapID = jsonObj.getString("isnap_id");
    		isNewJSONValid = true;
    	} catch (JSONException jsonException) {
    		Log.e(TAG, String.format("Error while getting the new isnap_id. Type: %s", "JSONException"));
    		isNewJSONValid = false;
    	} catch (NullPointerException e) {
    		Log.e(TAG, String.format("Error while getting the new isnap_id. Type: %s", "NullPointerException"));
    		isNewJSONValid = false;
    	} catch (Exception e) {
    		Log.e(TAG, String.format("Error while getting the new isnap_id. Type: %s", "Unknown"));
    		e.printStackTrace();
    		isNewJSONValid = false;
    	}
    	
    	if (isNewJSONValid) { // new JSON is valid
    		String oldISnapID = "";
    		boolean hasOldJSON = false;
    		try {
    			oldISnapID = jsonObject.getString("isnap_id");
    			hasOldJSON = true;
    		} catch (JSONException jsonException) {
    			Log.e(TAG, String.format("Error while getting the old isnap_id. Type: %s", "JSONException"));
        		hasOldJSON = false;
        	} catch (NullPointerException e) {
        		Log.e(TAG, String.format("Error while getting the old isnap_id. Type: %s", "NullPointerException"));
        		hasOldJSON = false;
        	} catch (Exception e) {
        		Log.e(TAG, String.format("Error while getting the old isnap_id. Type: %s", "Unknown"));
        		e.printStackTrace();
        		hasOldJSON = false;
        	}
    		
    		if (!hasOldJSON) {
    			shouldCreateNewInstance = true;
    		} else {
    			if (oldISnapID.equals(newISnapID)) { // new isnap_id is same as the latest received json
    				shouldCreateNewInstance = false;
    			} else {
    				shouldCreateNewInstance = true;
    			}
    		}
    	} else {
    		shouldCreateNewInstance = false;
    	}
    	    	
    	return shouldCreateNewInstance;
    }
    
    public ArrayList<Overlay3DModel> get3DModelList() {
    	return modelList;
    }
    
    /**
     * Processes the JSON object and loads overlay contents if found
     */
    private synchronized void processJSONObject() {
    	
    	try {
    		downloadThreadListFor3DModelPackage = new ArrayList<Thread>();
    		modelDataList = new ArrayList<Overlay3DModelData>();
    		JSONArray jsonArray = jsonObject.getJSONArray("assets");
    		for (int i = 0; i < jsonArray.length(); i++) {
    			try {
					JSONObject obj = jsonArray.getJSONObject(i);
					int type = Integer.valueOf(obj.getString("type"));
					
					if (type == ARiseConfigs.AR_TYPE_OVERLAY_VIDEO) {
						mode = OVERLAY_VIDEO;
						createControllerPanel();
						glSurfaceView.setVideoParams(obj, true, false);
						hideProgressDialog();
					} else if (type == ARiseConfigs.AR_TYPE_OVERLAY_3D_MODEL) {
						mode = OVERLAY_3DMODEL;
						createControllerPanel();
			    		try {
			    			String modelUrl = obj.getString("asset");
			    			float scale = Float.valueOf(obj.optString("scale", "1.0"));
			    			int offsetx = obj.optInt("offsetx");
			    			int offsety = obj.optInt("offsety");
			    			
                    		// initialize modelData
			    			Overlay3DModelData modelData = new Overlay3DModelData(context);
			    			modelData.setURL(modelUrl);
				    		modelData.setScale(scale);
				    		modelData.setOffset(new Vector2(offsetx, offsety));
				    		modelDataList.add(modelData);
	                    	
	                    	if (modelUrl.startsWith("file:///")) { // in-app model
	                    		// not download anything
	                    	} else {
		                    	// check existence for download
		                    	File modelFile = new File(modelData.getModelPath());
		                    	if (!modelFile.exists()) {		                        	
		                    		// prepare thread for downloading
		                        	Thread thread = new Thread(new Download3DModelPackage(context, modelUrl));
		                        	downloadThreadListFor3DModelPackage.add(thread);
		                        }
	                    	}
                        } catch (JSONException e) {
                        	Log.e(TAG, "Error while parsing 3D overlay object", e);
                        	e.printStackTrace();
                        }
					}
					
    			} catch (Exception e) {
    				
    			}
    		}
    		
    		// start download
            for (Thread downloadThread : downloadThreadListFor3DModelPackage) {
            	downloadThread.start();
            }
            
            // wait until all download threads finish
            for (Thread downloadThread : downloadThreadListFor3DModelPackage) {
            	downloadThread.join();
            }
            
			for (Overlay3DModelData modelData : modelDataList) {
				loadModelDataToAssets(modelData);
			}
			
			isLoading3D = true;
    		
			(new Timer()).schedule(new TimerTask() {
				public void run() {
					Log.i(TAG, "Allow display");
					setAllowDisplay(true);
				}
			}, 3000);
			
    	} catch (Exception e) {
    		
    	}
    }
    
    private boolean isShowing = false;
    private void showOverlayLayer() {
    	((Activity) context).runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			overlayLayerUIContainer.setVisibility(View.VISIBLE);
    			glSurfaceView.setVisibility(View.VISIBLE);
    			isShowing = true;
    		}
    	});
    }
    
    public void hideOverlayLayer() {
    	((Activity) context).runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			glSurfaceView.setVisibility(View.GONE);
    			overlayLayerUIContainer.setVisibility(View.GONE);
    			isShowing = false;
    		}
    	});
    }
    
    public boolean isShowing() {
    	return isShowing;
    }
    
    public synchronized void createOverlay3DModel() {
    	Log.i(TAG, "Start creating overlay 3D model...");
    	for (Overlay3DModelData modelData : modelDataList) {
			try {
				Model builtModel = createModelWithModelData(modelData);
				if (builtModel != null) {
					Overlay3DModel modelInstance = new Overlay3DModel(builtModel);
					modelInstance.setPosition(modelData.getOffset().x, modelData.getOffset().y);
					modelInstance.setScaleFactor(modelData.getScale());
					modelList.add(modelInstance);
				}
	    	} catch (Exception e) {
	    		Log.e(TAG, "Error when creating overlay 3d model", e);
	    		e.printStackTrace();
	    	}
    	}
    	hideProgressDialog();
    	isLoading3D = false;
    }
    
    public boolean isLoading3D() {
    	return isLoading3D;
    }
    
    public AssetManager getExtAssetManager() {
    	return externalAssets;
    }
    
    public AssetManager getIntAssetManager() {
    	return internalAssets;
    }
    
    /********************************************************************
     * Clears all overlay AR contents
     */
    public synchronized void clearARData() {
    	try {
	    	modelList.clear();
	    	isLoading3D = false;
	    	mode = OVERLAY_NONE;
	    	
	    	allowDisplay = false;
	    	
    	} catch (Exception e) {
    		
    	}
    }
    
    private RelativeLayout overlayLayerUIContainer;
    private void loadUI() {
    	
    	overlayLayerUIContainer = (RelativeLayout) ((Activity) context).findViewById(R.id.overlayLayerUIContainer);
    	
    	((Activity) context).runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			
    			closingOverlayBtn = (ImageButton) ((Activity) context).findViewById(R.id.overlayExitBtn);
    			
    			// behavior when clicking close-overlay-layer button
    	    	closingOverlayBtn.setOnClickListener(new View.OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					((IOverlayLayerParent) context).onOverlayLayerReturnButtonClicked();
    					deleteLatestJSONObject();
    				}
    			});
    		}
    	});
    }
    
    private void deleteLatestJSONObject() {
    	jsonObject = null;
    }
    
    private void createControllerPanel() {
    	((Activity) context).runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			
    			uiPanel = (LinearLayout) ((Activity) context).findViewById(R.id.overlayLayerUIPanel);
    			uiPanel.removeAllViews();
    			uiPanel.getLayoutParams().height = Gdx.graphics.getHeight() / 10;
    			
    			goToARStaticLayerBtn = addButton(R.drawable.go_button);
    			goToARStaticLayerBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
    					((IOverlayLayerParent) context).onOverlayLayerGoToStaticLayerButtonClicked();
    					deleteLatestJSONObject();
					}
				});
    			
    			if (mode == OVERLAY_3DMODEL) { // 3D mode: only 1 button for going to static layer
    				
    			}
    			
    			if (mode == OVERLAY_VIDEO) {
    				fullscreenBtn = addButton(R.drawable.fullscreen_button);
        			fullscreenBtn.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							Bundle args = new Bundle();
                            args.putString("url", glSurfaceView.getURL());
                            args.putBoolean("autoplay", true);
                            args.putBoolean("callback", true);
                            args.putInt("delay", -1);
                            
                            Intent intent = new Intent(context, MP4VideoPlayer.class);
                            intent.putExtras(args);
                            ((Activity) context).startActivity(intent);
						}
					});
    			}
    			
    			// calculate the width of UI Panel
    			int numOfChildView = uiPanel.getChildCount();
    			uiPanel.getLayoutParams().width = 0;
    			for (int i = 0; i < numOfChildView; i++) {
    				uiPanel.getLayoutParams().width += uiPanel.getChildAt(i).getLayoutParams().width;
    			}
    		}
    	});
    }
    
    /********************************************************************
     * Returns the render mode of overlay data manager (NONE, VIDEO, 3D).
     * @return the render mode of overlay data manager (NONE, VIDEO, 3D).
     */
    public int getMode() {
    	return mode;
    }
    
    private void hideProgressDialog() {
    	try {
    		((Activity) context).runOnUiThread(new Runnable() {
    			@Override
    			public void run() {
    				if (mDialog != null && dialogShown) {
    					mDialog.cancel();
    					dialogShown = false;
    				}
    			}
    		});
    		showOverlayLayer();
    	} catch (Exception e) {
    		Log.e(TAG, "Error when hiding progress dialog.", e);
    		e.printStackTrace();
    	}
    }
    
    /********************************************************************
     * Goes to AR static layer
     */
    public void goToARStaticLayer() {
    	try {
    		((Activity) context).runOnUiThread(new Runnable() {
    			@Override
    			public void run() {
    				goToARStaticLayerBtn.performClick();
    			}
    		});
    	} catch (Exception e) {
    		
    	}
    }
    
    /********************************************************************
     * Gets the GLSurfaceView used for overlay video.
     * @return the GLSurfaceview used for overlay video
     */
    public MyGLSurfaceView getGLSurfaceView() {
    	return glSurfaceView; 
    }
    
    /********************************************************************
     * Sets the GLSurfaceView used for overlay video.
     * @param view	: the GLSurfaceView to be set
     */
    public void setGLSurfaceView(MyGLSurfaceView view) {
    	glSurfaceView = view;
    }
    
    /********************************************************************
     * Adds a button with background from drawable resource.
     * @param resId	: the Drawable resource id
     * @return Returns the image button if created successfully; otherwise, returns null.
     */
    private ImageButton addButton(int resId) {
    	ImageButton addedButton;
    	try {
    		addedButton = new ImageButton(context);
    		
    		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    		layoutParams.weight = 1;
    		layoutParams.gravity = Gravity.CENTER;
			
			addedButton.setLayoutParams(layoutParams);
			addedButton.setBackgroundResource(resId);
			int w = context.getResources().getDrawable(resId).getIntrinsicWidth();
			int h = context.getResources().getDrawable(resId).getIntrinsicHeight();
			int buttonWidth = w * uiPanel.getLayoutParams().height / h;
			layoutParams.width = buttonWidth + 2 * MARGIN;
			layoutParams.setMargins(MARGIN, 0, MARGIN, 0);
			uiPanel.addView(addedButton, layoutParams);
			return addedButton;
    	} catch (Exception e) {
    		Log.e(TAG, "Error when adding button into panel", e);
    		e.printStackTrace();
    		return null;
    	}
    }
    
    private boolean allowDisplay = false;
    public void setAllowDisplay(boolean b) {
    	allowDisplay = b;
    }
    
    public boolean getAllowDisplay() {
    	return allowDisplay;
    }
    
    /**
     * Loads the model into assets manager, using some certain data.
     * @param modelData	: the data used for loading process.
     */
    private void loadModelDataToAssets(Overlay3DModelData modelData) {
    	Log.i(TAG, "Start loading model data to asset manager: " + modelData.getModelPath());
    	try {
			if (modelData.getURL().startsWith("file:///")) { // in-app model
				internalAssets.load(modelData.getModelPath(), Model.class);
			} else {
				// check if model file is already there
				File modelFile = new File(modelData.getModelPath());
				if (modelFile.exists()) {
					externalAssets.load(modelData.getModelPath(), Model.class);
				} else {
					// somehow, the result from download is broken or missing
					Log.e(TAG, String.format("Model file is broken or missing"));
					modelDataList.remove(modelData);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, String.format("Error in loading into asset manager %s.", modelData.getModelName()), e);
			e.printStackTrace();
		}
    	Log.i(TAG, "Finish loading model data to asset manager");
    }
    
    /**
     * Creates a model from certain data.
     * @param modelData	: the object contains data of the 3D model.
     * @return	the model from certain data; NULL if failed. 
     */
    private Model createModelWithModelData(Overlay3DModelData modelData) {
    	
    	Log.i(TAG, "Start creating model from " + modelData.getModelPath());
    	
    	Model curModel = null;
    	
    	if (modelData.isInApp()) {
    		if (internalAssets.isLoaded(modelData.getModelPath(), Model.class)) {
    			curModel = internalAssets.get(modelData.getModelPath(), Model.class);
    		} else {
    			Log.e(TAG, String.format("Model %s is not loaded", modelData.getModelName()));
    		}
    	} else {
    		if (externalAssets.isLoaded(modelData.getModelPath(), Model.class)) {
    			curModel = externalAssets.get(modelData.getModelPath(), Model.class);
    		} else {
    			Log.e(TAG, String.format("Model %s is not loaded", modelData.getModelName()));
    		}
    	}
    	
    	Log.i(TAG, "Finish creating model.");
    	
    	return curModel;
    }
}
