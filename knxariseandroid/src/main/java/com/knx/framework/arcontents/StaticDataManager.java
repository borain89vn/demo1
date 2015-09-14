package com.knx.framework.arcontents;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Rectangle;
import com.knx.framework.R;
import com.knx.framework.arcontents.old.InteractiveContentView;
import com.knx.framework.arcontents.old.Static3DModel;
import com.knx.framework.arcontents.old.Static3DModelData;
import com.knx.framework.helper.ARiseTracking;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.helper.LangPref;
import com.knx.framework.main.CameraActivity;
import com.knx.framework.main.IStaticLayerParent;
import com.knx.framework.main.Shared;
import com.knx.framework.main.WebActivity;
import com.knx.framework.task.DbHelper;
import com.knx.framework.task.Download3DModelPackage;
import com.knx.framework.task.DownloadBackgroundImage;
import com.knx.framework.task.DownloadPoster;
import com.knx.framework.utils.ARiseUtils;


/**
 * This class manages the AR static contents to display
 * @author Le Vu
 */
public class StaticDataManager {
	
	private final String TAG = "ARiseStaticLayer.Manager";
	
	// the context of the activity using this manager
	private Context context;
	
	// the RelativeLayout contains every views in a static layer: dim background, background image / color, interactive contents container, bottom panel / controller
	private RelativeLayout staticLayerLayout;
	
	// the progress dialog
	private ProgressDialog mDialog;
	
	// AR data, parsed from json response from server
	private JSONObject jsonObject;
	private String isnap_id;
	
	// list of 3D models
	private AssetManager externalAssets;
	private AssetManager internalAssets;
	private ArrayList<Thread> downloadThreadListFor3DModelPackage;
	private ArrayList<Static3DModelData> modelDataList;
	private ArrayList<Static3DModel> modelList;
	private AtomicBoolean isLoading3D;
	
	// bottom panel / controller
	private RelativeLayout bottomPanel;
	private ImageButton bookmarkButton, returnButton;
	private TextView entryName;
	
	// the list of all views in AR static layer (only for types AR_TYPE_BUTTON, AR_TYPE_BANNER, AR_TYPE_VIDEO)
	private RelativeLayout interactiveContentContainer;
	
	// display status
	private boolean isShowing;
	private AtomicBoolean shouldInvalidateOtherResponses;
	
	// There should be only ONE static layer to display at a time
	// So singleton should be used
    private static StaticDataManager singletonInstance;
    
    public static synchronized StaticDataManager getSingletonInstance() {
    	if (singletonInstance == null) {
    		
    		singletonInstance = new StaticDataManager();
    		
    		singletonInstance.context = null;
    		singletonInstance.jsonObject = null;
    		singletonInstance.staticLayerLayout = null;
    		
    		singletonInstance.modelList = new ArrayList<Static3DModel>();
    		singletonInstance.isLoading3D = new AtomicBoolean(false);
    		singletonInstance.isShowing = false;
    		
    		singletonInstance.shouldInvalidateOtherResponses = new AtomicBoolean(false);
    	}
    	return singletonInstance;
    }
    
    public static synchronized void createNewInstance(Context cxt, JSONObject jsonObj, AssetManager exAssets, AssetManager inAssets, RelativeLayout layout) {
    	if (singletonInstance == null) {
    		singletonInstance = getSingletonInstance();
    	}
    	
    	try {
    		
    		if (singletonInstance.shouldInvalidateOtherResponses.get()) {
    			// ignore
    		} else {
    			singletonInstance.shouldInvalidateOtherResponses.set(true);
    			
    			singletonInstance.context = cxt;
        		singletonInstance.jsonObject = jsonObj;
        		singletonInstance.externalAssets = exAssets;
        		singletonInstance.internalAssets = inAssets;
        		singletonInstance.staticLayerLayout = layout;
        		singletonInstance.interactiveContentContainer = (RelativeLayout) ((Activity) singletonInstance.context).findViewById(R.id.interactiveContentContainer);
        		singletonInstance.bottomPanel = (RelativeLayout) ((Activity) singletonInstance.context).findViewById(R.id.staticLayerController);
        		singletonInstance.entryName = (TextView) ((Activity) singletonInstance.context).findViewById(R.id.staticLayerEntryName);
        		singletonInstance.bookmarkButton = (ImageButton) ((Activity) singletonInstance.context).findViewById(R.id.staticLayerBookmarkBtn);
        		singletonInstance.returnButton = (ImageButton) ((Activity) singletonInstance.context).findViewById(R.id.staticLayerReturnBtn);
        		singletonInstance.clearARData();
        		singletonInstance.processJSONObject();
    		}
    	} catch (Exception e) {
    		Log.e(singletonInstance.TAG, "Cannot create new instance of AR screen", e);
    		e.printStackTrace();
    	}
    }
    
    public static synchronized void destroySingletonInstance() {
    	singletonInstance = null;
    }
    
    @SuppressLint("SetJavaScriptEnabled")
	private void processJSONObject() {
    	
    	// show progress dialog when processing json
    	showProgressDialog();
    	
    	// start processing on JSON Object
    	try {
    		
			// get isnap_id
			isnap_id = jsonObject.getString("isnap_id");
			
			// send impression tracking
			sendImpressionTracking(isnap_id);
			
			// download poster from field "poster_url"
			downloadPoster();
			
			// parse and set background
			parseBackground();
			
			modelDataList = new ArrayList<Static3DModelData>();
			downloadThreadListFor3DModelPackage = new ArrayList<Thread>();
			
			JSONArray jsonArray = jsonObject.getJSONArray("assets");
			
			// parse and process each asset in "assets" field
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					final JSONObject assetObj = jsonArray.getJSONObject(i);
					final int type = Integer.valueOf(assetObj.optString("type", "0"));
					
					String totalRowsString = jsonObject.optString("total_rows", "8"); 
					String totalColumnsString = jsonObject.optString("total_columns", "6");
					final int totalRows = Integer.valueOf(totalRowsString);
					final int totalColumns = Integer.valueOf(totalColumnsString);
                    
                    if ((type == ARiseConfigs.AR_TYPE_BANNER) || (type == ARiseConfigs.AR_TYPE_BUTTON) || (type == ARiseConfigs.AR_TYPE_VIDEO)) {
                    	((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
                    		public void run() {
                    			InteractiveContentView interactiveContentView = new InteractiveContentView(context, isnap_id, assetObj, totalRows, totalColumns);
                    			ImageView imageView = (ImageView) interactiveContentView.getView();
                            	interactiveContentContainer.addView(imageView);
                    		}
                    	});
                    } else if (type == ARiseConfigs.AR_TYPE_FORM) {
                    	((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
                    		public void run() {
                    			InteractiveContentView interactiveContentView = new InteractiveContentView(context, isnap_id, assetObj, totalRows, totalColumns);
                    			WebView webView = (WebView) interactiveContentView.getView();
                            	interactiveContentContainer.addView(webView);
                    		}
                    	});
                    } else if (type == ARiseConfigs.AR_TYPE_WEBSITE) {
                    	String websiteURL = assetObj.optString("url", "");
                    	if (websiteURL.length() > 0) {
                    		Intent intent = new Intent(context, WebActivity.class);
                            intent.putExtra("weblink", websiteURL);
                            ((Activity) singletonInstance.context).startActivity(intent);
                    	} else {
                    		// TODO: implementation for empty website url
                    	}
                    } else if (type == ARiseConfigs.AR_TYPE_3D) {
                    	String modelURL = assetObj.optString("asset", "");
                    	Log.i(TAG, "Model URL: " + modelURL);
                    	
                    	JSONArray coord = assetObj.optJSONArray("coord");
                    	float scale = Float.parseFloat(assetObj.optString("scale", "1.0"));
                    	
                    	if (modelURL.length() > 0 && coord != null) {
                    		String filename = (new File(modelURL)).getName();
                            filename = filename.substring(0, filename.length() - 7);
                        	
                        	try {
                        		// build modelData
    							Point frame3DOffset = new Point((int) (Double.valueOf(coord.get(0).toString()) * Gdx.graphics.getWidth()), ((int) (Double.valueOf(coord.get(1).toString()) * Gdx.graphics.getHeight())));
    							int frame3DWidth = (int) (Double.valueOf(coord.get(2).toString()) * Gdx.graphics.getWidth());
    							int frame3DHeight = (int) (Double.valueOf(coord.get(3).toString()) * Gdx.graphics.getHeight());
    							
    							Static3DModelData modelData = new Static3DModelData(context);
    	                    	modelData.setURL(modelURL);
    	                    	modelData.setFrameData(frame3DOffset, frame3DWidth, frame3DHeight);
    	                    	modelData.setScale(scale);
    	                    	modelDataList.add(modelData);
    	                    	
    	                    	if (modelURL.startsWith("file:///")) { // in-app model
    	                    		// not download anything
    	                    	} else {
    		                    	// check existence for download
    		                    	File modelFile = new File(modelData.getModelPath());
    		                    	if (!modelFile.exists()) {		                        	
    		                    		// prepare thread for downloading
    		                        	Thread thread = new Thread(new Download3DModelPackage(context, modelURL));
    		                        	downloadThreadListFor3DModelPackage.add(thread);
    		                        }
    	                    	}
                            } catch (JSONException e) {
                            	Log.e(TAG, "Error while parsing 3D model", e);
                            	e.printStackTrace();
                            } catch (Exception e) {
                            	Log.e(TAG, "Error while parsing 3D model", e);
                            	e.printStackTrace();
                            }
                    	} else {
                    		// TODO: invalid url
                    	}
                    }
				} catch (Exception e) {
					Log.e(TAG, String.format("ERROR while parsing asset at index %d", i), e);
					e.printStackTrace();
				}
			}
            
            // start download
            for (Thread downloadThread : downloadThreadListFor3DModelPackage) {
            	downloadThread.start();
            }
            
            // wait until download finishes, download thread timeout is set at 10 seconds so it will terminate in 10-second time
            for (Thread downloadThread : downloadThreadListFor3DModelPackage) {
            	downloadThread.join();
            }
            
            Log.i(TAG, "Start loading " + modelDataList.size() + " 3D model(s) into asset...");
			for (Static3DModelData modelData : modelDataList) {
				Log.i(TAG, "\t+ Loading " + modelData.getModelPath());
				loadModelDataToAssets(modelData);
			}
			
			isLoading3D.set(true);
			Log.i(TAG, "isLoading3D at the end of json parsing is " + isLoading3D.get());
    	} catch (Exception e) {
    		Log.e(TAG, "Error when parsing json response", e);
    		e.printStackTrace();
    	} finally {
    		Log.i(TAG, "Finish parsing AR static layer");
    	}
    	
    	prepareBottomPanel();
    	
    	// close progress dialog when finishing
    	closeProgressDialog();
    }
    
    /**
     * This methods prepares the bottom panel of interactive layer.
     */
    private void prepareBottomPanel() {
    	// set entry name and theme color for bottom panel
    	((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
    		public void run() {
    			bottomPanel.setBackgroundColor(ARiseConfigs.THEME_COLOR);
    	    	try {
    	    		entryName.setText(jsonObject.getString("name"));
    	    	} catch (JSONException e) {
    	    		Log.e(TAG, "Error while setting entry name", e);
    	    		e.printStackTrace();
    	    	} catch (NullPointerException e) {
    	    		Log.e(TAG, "Error while setting entry name", e);
    	    		e.printStackTrace();
    	    	} catch (Exception e) {
    	    		Log.e(TAG, "Error while setting entry name", e);
    	    		e.printStackTrace();
    	    	}
    		}
    	});
    	
    	// bookmark button
    	if (isBookmarkAllowed()) {
    		Log.i(TAG, "Bookmark is enabled for this entry");
    		try {
        		final String id = jsonObject.getString("isnap_id");
        		final String title = jsonObject.getString("name");
    			final String posterURL = jsonObject.getString("poster_url");
    			
    			if (DbHelper.getInstance(context).isBookmarked(id)) {
    				hideBookmarkButton();
    	        } else {
    	        	bookmarkButton.setOnClickListener(new View.OnClickListener() {
    	        		public void onClick(View v) {
    						DbHelper.getInstance(context).addToBookmark(id, title, posterURL, jsonObject.toString());
    						
    						returnButton.setVisibility(View.GONE);
    						bookmarkButton.setVisibility(View.GONE);
    						entryName.setText(LangPref.TXTBOOKMARKING);
    						
    						// set timer for displaying entry name and close button after 2 seconds 
    						(new Timer()).schedule(new TimerTask() {
    							public void run() {
    								((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
    									public void run() {
    										entryName.setText(title);
    										
    										AlphaAnimation fadeInAnim = new AlphaAnimation(0, 1);
    										fadeInAnim.setDuration(300);
    										fadeInAnim.setFillAfter(true);
    										fadeInAnim.setRepeatCount(0);
    										fadeInAnim.setAnimationListener(new Animation.AnimationListener() {
    											@Override
    											public void onAnimationStart(Animation animation) {
    												returnButton.setVisibility(View.VISIBLE);
    											}
    											
    											@Override
    											public void onAnimationRepeat(Animation animation) {}
    											
    											@Override
    											public void onAnimationEnd(Animation animation) {}
    										});
    										returnButton.startAnimation(fadeInAnim);
    									}
    								});	
    							}
    						}, 2000);
    	        		}
    	        	});
    	        }
        	} catch (NullPointerException e) {
        		Log.e(TAG, "Error occurs while preparing bookmark button", e);
        		e.printStackTrace();
        		hideBookmarkButton();
        	} catch (JSONException e) {
        		Log.e(TAG, "Error occurs while preparing bookmark button", e);
        		e.printStackTrace();
        		hideBookmarkButton();
        	} catch (Exception e) {
        		Log.e(TAG, "Error occurs while preparing bookmark button", e);
        		e.printStackTrace();
        		hideBookmarkButton();
        	}
    	} else {
    		Log.i(TAG, "Bookmark is disabled for this entry");
    		hideBookmarkButton();
    	}
    	
    	// close button
		returnButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AlphaAnimation fadeOutAnim = new AlphaAnimation(1, 0);
				fadeOutAnim.setDuration(300);
				fadeOutAnim.setRepeatCount(0);
				fadeOutAnim.setFillAfter(false);
				fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {}
					
					@Override
					public void onAnimationRepeat(Animation animation) {}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						clearARData();
						staticLayerLayout.setVisibility(View.GONE);
						bottomPanel.setVisibility(View.GONE);
						shouldInvalidateOtherResponses.set(false);
						((IStaticLayerParent) context).onStaticLayerReturnButtonClicked();
					}
				});
				staticLayerLayout.startAnimation(fadeOutAnim);
			}
		});
    }
    
    private boolean isBookmarkAllowed() {
    	try {
    		boolean bookmarkAllowance = jsonObject.optBoolean("bookmark", true);
    		return bookmarkAllowance;
    	} catch (Exception e) {
    		Log.e(TAG, "Error while parsing field \"bookmark\"", e);
    		e.printStackTrace();
    		return true;
    	}
    }
    
    private void hideBookmarkButton() {
    	((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
			public void run() {
				bookmarkButton.setVisibility(View.GONE);
			}
		});
    }
    
    public void createStatic3DModels() {
    	Log.i(TAG, "Start creating " + modelDataList.size() + " static 3D model(s)...");
		for (Static3DModelData modelData : modelDataList) {
			Log.i(TAG, "\t+ Create model from " + modelData.getModelPath());
			try {
				Model builtModel = createModelWithModelData(modelData);
				
				Log.i(TAG, "Finish creating model " + modelData.getModelName());
				
				Static3DModel newModelInstance = new Static3DModel(builtModel, modelData.getFrame());
				newModelInstance.setPosition(modelData.getOffset().x, modelData.getOffset().y);
				newModelInstance.setScaleFactor(modelData.getScale());
				newModelInstance.setFrame(modelData.getFrame());
				
				Log.i(TAG, "Finish creating ARise static model");
				
				if (newModelInstance.animations.size > 0) {
		        	newModelInstance.doAnimation(0);
		        }
				
		        modelList.add(newModelInstance);
			} catch (Exception e) {
				Log.e(TAG, "Error in creating 3D models", e);
				e.printStackTrace();
				((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(context, "Error loading 3D model", Toast.LENGTH_SHORT).show();
					}
				});
			} finally {
				removeProgressBar(modelData);
			}
		}

		isLoading3D.set(false);
    }
    
    private HashMap<Static3DModelData, RelativeLayout> progressBarList;
    private void addProgressBar(final Static3DModelData model) {
    	((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				if (progressBarList == null)
					progressBarList = new HashMap<Static3DModelData, RelativeLayout>();
				
				Rectangle frame = model.getFrame();
				
				ProgressBar bar = new ProgressBar(context);
				RelativeLayout relativeLayout = new RelativeLayout(context);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				lp.setMargins((int) frame.x, (int) (Gdx.graphics.getHeight() - frame.y - frame.height), (int) (Gdx.graphics.getWidth() - frame.x - frame.width), (int) frame.y);
				relativeLayout.setGravity(Gravity.CENTER);
				relativeLayout.setLayoutParams(lp);
				relativeLayout.addView(bar);
				staticLayerLayout.addView(relativeLayout);
				
				progressBarList.put(model, relativeLayout);
			}
		});
    }
    
    private void removeProgressBar(final Static3DModelData model) {
    	((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				if (progressBarList == null)
					return;
				
				RelativeLayout layout = progressBarList.get(model);
				staticLayerLayout.removeView(layout);
				layout.setVisibility(View.GONE);
			}
		});
    }
    
    public boolean isLoading3DModel() {
    	return isLoading3D.get();
    }
    
    public ArrayList<Static3DModel> getModelList() {
    	return modelList;
    }
    
    public boolean isShowing() {
    	return isShowing;
    }
    
    public void showARLayout() {
    	((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
			public void run() {
				synchronized (this) {
					for (int i = 0; i < interactiveContentContainer.getChildCount(); i++) {
						View interactiveContentView = interactiveContentContainer.getChildAt(i);
						interactiveContentView.setVisibility(View.GONE);
					}
					bottomPanel.setVisibility(View.GONE);
					
					staticLayerLayout.setVisibility(View.VISIBLE);
					
					for (int i = 0; i < interactiveContentContainer.getChildCount(); i++) {
						final View interactiveContentView = interactiveContentContainer.getChildAt(i);
						RelativeLayout.LayoutParams interactiveContentViewLayoutParams = (RelativeLayout.LayoutParams) interactiveContentView.getLayoutParams();
						
						// Entrance animation for interactive contents
						ScaleAnimation assetScaleAnimation = new ScaleAnimation(0, 1, 0, 1,
								0.5f * interactiveContentViewLayoutParams.width,
								0.5f * interactiveContentViewLayoutParams.height);
						assetScaleAnimation.setDuration(300);
						assetScaleAnimation.setRepeatCount(0);
						assetScaleAnimation.setFillAfter(true);
						assetScaleAnimation.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationStart(Animation animation) {
								interactiveContentView.setVisibility(View.VISIBLE);
							}
							
							@Override
							public void onAnimationRepeat(Animation animation) {}
							
							@Override
							public void onAnimationEnd(Animation animation) {}
						});
						interactiveContentView.startAnimation(assetScaleAnimation);
						
						// Sliding up animation for bottom panel
						// Animation starts after all interactive contents appear
						TranslateAnimation bottomPanelSlidingUpAnimation = new TranslateAnimation(0, 0, bottomPanel.getLayoutParams().height, 0);
						bottomPanelSlidingUpAnimation.setDuration(300);
						bottomPanelSlidingUpAnimation.setRepeatCount(0);
						bottomPanelSlidingUpAnimation.setFillAfter(true);
						bottomPanelSlidingUpAnimation.setStartOffset(300);
						bottomPanelSlidingUpAnimation.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationStart(Animation anim) {
								bottomPanel.setVisibility(View.VISIBLE);
							}
							
							@Override
							public void onAnimationRepeat(Animation anim) {}
							
							@Override
							public void onAnimationEnd(Animation anim) {}
						});
						bottomPanel.startAnimation(bottomPanelSlidingUpAnimation);
					}
					
					isShowing = true;
				}
			}
		});
    }
    
    /**
     * This method clears all the AR data of the manager.
     */
    public void clearARData() {
    	try {
    		
    		isLoading3D.set(false);
    		modelList = new ArrayList<Static3DModel>();
    		
    		hideBackground();
    		
    		((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
				public void run() {
					try {
						// clear the views from the viewList
						interactiveContentContainer.removeAllViews();
						
						// clear progress bars
						for (Static3DModelData modelData : modelDataList) {
							removeProgressBar(modelData);
						}
						
//						viewList.clear();
						
					} catch (Exception e) {
						Log.e(TAG, "Cannot clear old data", e);
						e.printStackTrace();
					}
					
					isShowing = false;
				}
			});
    	} catch (NullPointerException e) {
    		Log.e(TAG, String.format("Error while clearing AR data. Type: %s", "NullPointerException"));
    	} catch (Exception e) {
    		Log.e(TAG, String.format("Error while clearing AR data. Type: %s", "Unknown"));
    		e.printStackTrace();
    	}
	}
	
	private void sendImpressionTracking(final String ariseId) {
		CameraActivity.run(new Runnable() {
			public void run() {
				ARiseTracking.trackImpression(context, ARiseConfigs.TRACKING_URL, ARiseTracking.TRACKING_IMPRESSION, ariseId);
			}
		});
	}
    
    private void showProgressDialog() {
    	try {
    		((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
    			public void run() {
    				mDialog = new ProgressDialog(context, AlertDialog.THEME_HOLO_DARK);
    				mDialog.setMessage("Please wait...");
    				mDialog.setCancelable(false);
    				mDialog.show();
    			}
    		});
    	} catch (NullPointerException e) {
    		Log.e(TAG, String.format("Error while showing progress dialog"));
    	}
    }
    
    private void closeProgressDialog() {
    	// hide dialog
    	((Activity) singletonInstance.context).runOnUiThread(new Runnable() {
			public void run() {
				mDialog.cancel();
			}
		});
    }
    
    /**
     * Loads the model into assets manager, using some certain data.
     * @param modelData	: the data used for loading process.
     */
    private void loadModelDataToAssets(Static3DModelData modelData) {
    	try {
			if (modelData.getURL().startsWith("file:///")) { // in-app model
				internalAssets.load(modelData.getModelPath(), Model.class);
				addProgressBar(modelData);
			} else {
				// check if model file is already there
				File modelFile = new File(modelData.getModelPath());
				
				if (modelFile.exists()) {
					Log.i(TAG, "Found 3D model at: " + modelData.getModelPath());
					externalAssets.load(modelData.getModelPath(), Model.class);
					addProgressBar(modelData);
				} else {
					// somehow, the result from download is broken or missing
					Log.e(TAG, "Cannot find 3D model at: " + modelData.getModelPath());
					modelDataList.remove(modelData);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, String.format("Error while loading model data (name: %s) to asset manager.", modelData.getModelName()));
		}
    }
    
    /**
     * Creates a model from certain data.
     * @param modelData	: the object contains data of the 3D model.
     * @return	the model from certain data; NULL if failed. 
     */
    private Model createModelWithModelData(Static3DModelData modelData) {
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
    	
    	return curModel;
    }
    
    public ImageButton getReturnButton() {
    	return returnButton;
    }
    
    /**
     * This method handles parsing of fields "background" and "backgroundColor"
     */
    private void parseBackground() {
    	try {
    		
    		final String backgroundImageURL = jsonObject.optString("background");
			final String backgroundColorHexCode = jsonObject.optString("backgroundColor");
    		
    		if (backgroundImageURL.length() == 0 && backgroundColorHexCode.length() == 0) {
    			Log.e(TAG, "No image or color is set for background");
    			hideBackground();
    		} else {
    			if (backgroundColorHexCode.length() > 0) {
    				try {

    					if (backgroundColorHexCode.length() == 6) {
		    				int a = Integer.parseInt("FF", 16);
		    				int r = Integer.parseInt(backgroundColorHexCode.substring(0,  2), 16);
		    				int g = Integer.parseInt(backgroundColorHexCode.substring(2,  4), 16);
		    				int b = Integer.parseInt(backgroundColorHexCode.substring(4,  6), 16);
		    				
		    				backgroundMap.clear();
		    				backgroundMap.put("r", Integer.toString(r));
		    				backgroundMap.put("g", Integer.toString(g));
		    				backgroundMap.put("b", Integer.toString(b));
		    				backgroundMap.put("a", Integer.toString(a));
	    				
    					} else {
    						hideBackground();
    					}
    				} catch (Exception e) {
    					Log.e(TAG, "Error while parsing color hexcode", e);
    					e.printStackTrace();
    					
    					hideBackground();
    				}
    			}
    			
    			if (backgroundImageURL.length() > 0) {
    				String path = (new URL(backgroundImageURL)).getPath();
    				String filename = path.substring(path.lastIndexOf("/") + 1);
    				
    				final File file = new File(Shared.getAssetDir(context) + "/" + filename);
    				if (file.exists()) {
    					boolean passMD5check = ARiseUtils.performMD5ValidationOnFile(file.getAbsolutePath());
    					if (passMD5check) {
    						Log.i(TAG, "Local background file exists and passes md5");
    						backgroundMap.clear();
    						backgroundMap.put("path", file.getAbsolutePath());
    					} else {
    						Log.i(TAG, "Local background file not pass MD5 check");
    						(new DownloadBackgroundImage(context, backgroundImageURL, this)).execute();
    					}
    				} else {
    					Log.i(TAG, "Local background file not exist");
    					(new DownloadBackgroundImage(context, backgroundImageURL, this)).execute();
    				}
    			}
    		}
    	} catch (Exception e) {
    		Log.e(TAG, "Error while parsing background", e);
    		e.printStackTrace();
    		hideBackground();
    	}
    }
    
    private HashMap<String, String> backgroundMap = new HashMap<String, String>();
    private Texture staticLayerBackgroundTexture = null;
    public Texture generateStaticLayerBackgroundTexture() {
    	if (staticLayerBackgroundTexture != null) {
    		return staticLayerBackgroundTexture;
    	} else {
    		if (backgroundMap == null || backgroundMap.keySet().size() == 0) {
    			hideBackground();
        	} else if (backgroundMap.containsKey("r") &&
        			backgroundMap.containsKey("g") &&
        			backgroundMap.containsKey("b") &&
        			backgroundMap.containsKey("a")) {
        		try {
        			Log.i(TAG, "Start setting background from color");
        			
        			int r = Integer.parseInt(backgroundMap.get("r"));
        			int g = Integer.parseInt(backgroundMap.get("g"));
        			int b = Integer.parseInt(backgroundMap.get("b"));
        			int a = Integer.parseInt(backgroundMap.get("a"));
        			
        			Log.i(TAG, "Background color: rgba = (" + r + ", " + g + ", " + b + ", " + a + ")");
        			
        			Pixmap colorStaticBackgroundPixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
        			colorStaticBackgroundPixmap.setColor(com.badlogic.gdx.graphics.Color.rgba8888(r, g, b, a));
        			for (int x = 0; x < Gdx.graphics.getWidth(); x++) {
        				for (int y = 0; y < Gdx.graphics.getHeight(); y++) {
        					colorStaticBackgroundPixmap.drawPixel(x, y, com.badlogic.gdx.graphics.Color.rgba8888(r/255.f, g/255.f, b/255.f, a/255.f));
        				}
        			}
	    			staticLayerBackgroundTexture = new Texture(colorStaticBackgroundPixmap);
	    			colorStaticBackgroundPixmap.dispose();
	        	} catch (Exception e) {
	        		Log.e(TAG, "Error while setting background from color", e);
	        		e.printStackTrace();
	        		hideBackground();
	        	}
        	} else if (backgroundMap.containsKey("path")) {
        		String pathToBackground = backgroundMap.get("path");
        		if (pathToBackground != null && pathToBackground.length() > 0) {
        			Log.i(TAG, "pathToBackground=" + pathToBackground);
        			if (Gdx.files.absolute(pathToBackground).exists()) {
        				Log.i(TAG, "Found background image at " + pathToBackground);
        				
        				// handle for progressive JPEG
        				Bitmap backgroundBmp = BitmapFactory.decodeFile(pathToBackground);
        				ByteArrayOutputStream stream = new ByteArrayOutputStream();
        				backgroundBmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        				byte[] backgroundByteArr = stream.toByteArray();
        				
        				Pixmap imageBackgroundPixmap = new Pixmap(backgroundByteArr, 0, backgroundByteArr.length);
	        			staticLayerBackgroundTexture = new Texture(imageBackgroundPixmap);
	        			imageBackgroundPixmap.dispose();
        			} else {
        				Log.i(TAG, "Not found background image at " + pathToBackground);
        				hideBackground();
        			}
        		} else {
        			hideBackground();
        		}
        	} else {
        		hideBackground();
        	}
    		
    		return staticLayerBackgroundTexture;
    	}
    }
    
    public void setBackgroundPath(String path) {
    	if (path == null || path.length() == 0) {
    		hideBackground();
    	} else {
    		backgroundMap.put("path", path);
    	}
    }
    
    public void hideBackground() {
    	backgroundMap.clear();
    	if (staticLayerBackgroundTexture != null) {
//			staticLayerBackgroundTexture.dispose();
    		staticLayerBackgroundTexture = null;
    	}
    }
    
    private void downloadPoster() {
    	try {
			String posterLink = jsonObject.getString("poster_url");
			File file = new File(Shared.getPosterDir(context) + "/" + posterLink.hashCode());
			if (!file.exists()) {
				(new DownloadPoster(context)).execute(posterLink);
			}
		} catch (JSONException e) {
			Log.e(TAG, "Error while downloading poster in static layer." + e);
			e.printStackTrace();
		}
    }
}