package com.knx.framework.arcontents.old;

import java.io.File;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.helper.ARiseTracking;
import com.knx.framework.helper.MyWebViewClient;
import com.knx.framework.main.CameraActivity;
import com.knx.framework.main.Shared;
import com.knx.framework.main.WebActivity;
import com.knx.framework.main.pdfreader.ARisePdfReaderActivity;
import com.knx.framework.task.DownloadAssetImage;
import com.knx.framework.utils.ARiseUtils;
import com.knx.framework.videoplayer.MP4VideoPlayer;
import com.knx.framework.videoplayer.YouTubeVideoPlayer;

public class InteractiveContentView {
	
	private static final String TAG = "ARiseInteractiveContentView";
	
	private Context context;
	private String isnap_id;
	
	private String asset;
	private String url;
	private int top;
	private int left;
	private int width;
	private int height;
	private int type;
	private String widget;
	private int urlOpenType;
	
	private ImageView imageView;
	private WebView webView;
	
	public InteractiveContentView(Context cxt, String id, String asset, String url, int left, int top, int width, int height, int type, int urlOpenType) {
		this.context = cxt;
		this.isnap_id = id;
		this.asset = asset;
		this.url = url;
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.type = type;
		this.urlOpenType = urlOpenType;
		
		generateWidget();
		initView();
	}
	
	public InteractiveContentView(Context cxt, String id, JSONObject assetJSONObject, int totalRows, int totalColumns) {
		this.context = cxt;
		this.isnap_id = id;
		this.asset = assetJSONObject.optString("asset", "");
		this.url = assetJSONObject.optString("url", "");
		JSONArray coord = assetJSONObject.optJSONArray("coord");
		
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		
		if (coord != null && coord.length() == 4) {
			this.left = (int) (coord.optDouble(0, 0.0) * displayMetrics.widthPixels);
			this.top = (int) (coord.optDouble(1, 0.0) * displayMetrics.heightPixels);
			this.width = (int) (coord.optDouble(2, 0.0) * displayMetrics.widthPixels);
			this.height = (int) (coord.optDouble(3, 0.0) * displayMetrics.heightPixels);
		} else if (assetJSONObject.has("row") && assetJSONObject.has("column") && assetJSONObject.has("width") && assetJSONObject.has("height")) {
			this.left = (assetJSONObject.optInt("column", 0) - 1) * displayMetrics.widthPixels / totalColumns;
			this.top = (assetJSONObject.optInt("row", 0) - 1) * displayMetrics.heightPixels / totalRows;
			this.width = assetJSONObject.optInt("width", 0) * displayMetrics.widthPixels / 320;
			this.height = assetJSONObject.optInt("height", 0) * displayMetrics.heightPixels / 480;
		} else {
			this.left = 0;
			this.top = 0;
			this.width = 0;
			this.height = 0;
		}
		try {
			this.type = Integer.parseInt(assetJSONObject.optString("type", "0"));
		} catch (NumberFormatException e) {
			Log.e(TAG, "Error while parsing type of interactive content asset", e);
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(TAG, "Error while parsing type of interactive content asset", e);
			e.printStackTrace();
		}
		this.urlOpenType = assetJSONObject.optInt("urlOpenType", 0);
		
		generateWidget();
		initView();
	}
	
	public int getLeft() {return this.left;}
	public int getTop() {return this.top;}
	public int getWidth() {return this.width;}
	public int getHeight() {return this.height;}
	
	public View getView() {
		if (this.type == ARiseConfigs.AR_TYPE_BUTTON || this.type == ARiseConfigs.AR_TYPE_BANNER || this.type == ARiseConfigs.AR_TYPE_VIDEO) return imageView;
		if (this.type == ARiseConfigs.AR_TYPE_FORM) return webView;
		return null;
	}
	
	private void generateWidget() {
		switch (this.type) {
			case ARiseConfigs.AR_TYPE_BUTTON:
				this.widget = "button";
				break;
			case ARiseConfigs.AR_TYPE_FORM:
				this.widget = "webform";
				break;
			case ARiseConfigs.AR_TYPE_BANNER:
				this.widget = "banner";
				break;
			case ARiseConfigs.AR_TYPE_VIDEO:
				this.widget = "video";
				break;
			default:
				this.widget = "unknown";
				break;
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void initView() {
		if (this.type == ARiseConfigs.AR_TYPE_BANNER || this.type == ARiseConfigs.AR_TYPE_BUTTON || this.type == ARiseConfigs.AR_TYPE_VIDEO) {
			imageView = new ImageView(this.context);
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {								
					// REPLACED on 01-08-2014
					RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
					layoutParams.setMargins(left, top, 0, 0);
					layoutParams.width = width;
					layoutParams.height = height;
					imageView.setLayoutParams(layoutParams);
					
					if (url != null && !(url.equalsIgnoreCase(""))) {
						imageView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(final View v) {
								sendInteractionTracking(isnap_id, "click", widget, url);
								
								ScaleAnimation scale1 = new ScaleAnimation(1.8f, 1.f, 1.8f, 1.f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
								scale1.setDuration(300);
								scale1.setRepeatCount(0);
								scale1.setFillAfter(true);
								scale1.setAnimationListener(new Animation.AnimationListener() {
									@Override
									public void onAnimationStart(Animation anim) {}
									
									@Override
									public void onAnimationRepeat(Animation anim) {}
									
									@Override
									public void onAnimationEnd(Animation anim) {
										if (urlOpenType == ARiseConfigs.OPEN_BY_IN_APP_BROWSER) {
											openWebURLEnhanced(url, type);
										} else {
											openWebURL(url);
										}
									}
								});
								
								v.startAnimation(scale1);
							}
						});
					}
				}
			});
			setViewImage();
		} else if (this.type == ARiseConfigs.AR_TYPE_FORM) {
			webView = new WebView(context);
			// disable scroll on touch
			webView.getSettings().setPluginState(WebSettings.PluginState.ON);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
//			webView.getSettings().setPluginsEnabled(true);
			webView.getSettings().setSupportMultipleWindows(false);
			webView.getSettings().setSupportZoom(false);
			webView.setVerticalScrollBarEnabled(false);
			webView.setHorizontalScrollBarEnabled(false);

			webView.setWebViewClient(new MyWebViewClient());
			
			((Activity) context).runOnUiThread(new Runnable() {
				public void run() {
					RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
					layoutParams.setMargins(left, top, 0, 0);
					layoutParams.width = width;
					layoutParams.height = height;
					webView.setLayoutParams(layoutParams);
					
					webView.setOnTouchListener(new View.OnTouchListener() {
						public boolean onTouch(View v, MotionEvent event) {
							switch (event.getAction()) {
								case MotionEvent.ACTION_DOWN:
								case MotionEvent.ACTION_UP:
									if (!v.hasFocus()) {
										v.requestFocus();
									}
				                    break;
			                }
							return false;
						}
					});
				}
			});
			webView.loadUrl(this.url);
		} else {
			
		}
	}
	
	private void sendInteractionTracking(final String ariseId, final String trigger, final String object, final String targetURL) {
        CameraActivity.run(new Runnable() {
        	public void run() {
        		ARiseTracking.trackInteraction(context, ARiseConfigs.TRACKING_URL, ARiseTracking.TRACKING_INTERACTION, ariseId, trigger, object, targetURL);
        	}
        });
    }
	
	@SuppressWarnings("unchecked")
	private void setViewImage() {
		if (!(this.asset.equalsIgnoreCase(""))) {
			String filename = this.asset.substring(this.asset.lastIndexOf('/') + 1);
			File file = new File(Shared.getAssetDir(this.context) + "/" + filename);
			
			if (file.exists()) {
				boolean passMD5check = ARiseUtils.performMD5ValidationOnFile(file.getAbsolutePath());
				
				if (passMD5check) {
					imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
				} else {
                    HashMap<String, String> asset = new HashMap<String, String>();
                    asset.put("assetURL", this.asset);
                    asset.put("type", String.valueOf(this.type));
                    new DownloadAssetImage((Activity) context, imageView).execute(asset);
				}
			} else {
				HashMap<String, String> asset = new HashMap<String, String>();
                asset.put("assetURL", this.asset);
                asset.put("type", String.valueOf(this.type));
                new DownloadAssetImage((Activity) context, imageView).execute(asset);
			}
		}
	}
	
	private void openWebURL(String inURL) {
        if (ARiseUtils.isYoutubeURL(inURL)) {
            String videoId = ARiseUtils.extractYoutubeId(inURL);
            Intent video = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
            try {
            	video.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            	video.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            	video.addFlags(Intent.FLAG_FROM_BACKGROUND);
                ((Activity) context).startActivity(video);
            } catch (ActivityNotFoundException exp) {
                video = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
                video.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            	video.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            	video.addFlags(Intent.FLAG_FROM_BACKGROUND);
            	((Activity) context).startActivity(video);
            }
        } else {
        	Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
        	browser.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        	browser.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        	browser.addFlags(Intent.FLAG_FROM_BACKGROUND);
        	((Activity) context).startActivity(browser);
        }
    }

	private void openWebURLEnhanced(String inURL, int type) {
		if (inURL.startsWith("tel:/")) {
			StringBuilder sb = new StringBuilder(inURL);
			sb.deleteCharAt(4);
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString()));
    		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    		intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
    		((Activity) context).startActivity(intent);
		} else if (inURL.startsWith("mailto:")) {
    		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
    		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    		intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
    		((Activity) context).startActivity(intent);
    	} else {
    		if (inURL.endsWith(".mp4") || inURL.endsWith(".mp3")) {
    			Bundle args = new Bundle();
    			args.putString("url", inURL);
				args.putBoolean("autoplay", true);
				args.putBoolean("callback", false);
				args.putInt("delay", -1);

				Intent mp4Activity = new Intent(context, MP4VideoPlayer.class);
				mp4Activity.putExtras(args);
				mp4Activity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				mp4Activity.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				mp4Activity.addFlags(Intent.FLAG_FROM_BACKGROUND);
				((Activity) context).startActivity(mp4Activity);
    		} else {
    			if (ARiseUtils.isYoutubeURL(inURL)) {
					inURL = inURL.substring(inURL.lastIndexOf("/") + 1);
					Bundle args = new Bundle();
					args.putString("videoId", inURL);
					args.putBoolean("autoplay", true);
					args.putBoolean("callback", false);
					args.putInt("delay", -1);

					Intent youtubeVideoActivity = new Intent(context, YouTubeVideoPlayer.class);
					youtubeVideoActivity.putExtras(args);
					youtubeVideoActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					youtubeVideoActivity.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					youtubeVideoActivity.addFlags(Intent.FLAG_FROM_BACKGROUND);
					((Activity) context).startActivity(youtubeVideoActivity);
				} else {
					
					if (inURL.endsWith(".pdf")) {
						Bundle bundle = new Bundle();
						bundle.putString("PDF_URL_KEY", inURL);
						Intent intent = new Intent(context, ARisePdfReaderActivity.class);
						intent.setAction(Intent.ACTION_VIEW);
						intent.putExtras(bundle);
						((Activity) context).startActivity(intent);
					} else {
						Bundle args = new Bundle();
						args.putString("weblink", inURL);
						args.putBoolean("callback", false);
						args.putInt("delay", -1);
						Intent browser = new Intent(context, WebActivity.class);
						browser.putExtras(args);
						browser.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						browser.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						browser.addFlags(Intent.FLAG_FROM_BACKGROUND);
						((Activity) context).startActivity(browser);
					}
				}
    		}
    	}
    }
}
