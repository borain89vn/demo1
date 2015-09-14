package com.knx.framework.main.setting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;
import com.knx.framework.R;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.helper.LangPref;
import com.knx.framework.main.Shared;
import com.knx.framework.task.DbHelper;
import com.knx.framework.ui.ARiseDialogTwoButton;
import com.knx.framework.utils.ARiseUtils;
import com.knx.framework.videoplayer.YouTubeFailureRecoveryActivity;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.ResponseListener;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGaussianBlurFilter;

public class SettingPage extends YouTubeFailureRecoveryActivity {
	
	private static final String TAG = "ARiseNewSettingPage";
	
	private ImageView settingPageBackground;
	
	private RelativeLayout pageTitleSection;
	
	private RelativeLayout topSection;
	private AboutView aboutViewLayout;
	
	private RelativeLayout videoViewLayout;
	private String youtubeId;
	private YouTubePlayerView youtubePlayerView;
    private YouTubePlayer youtubePlayer;
    private boolean isShowingVideoFullscreen;
    private VideoView videoView;
    
    private LinearLayout buttonsSection;
    private SettingPageButton aboutButton;
    private SettingPageButton clearDataButton;
    private SettingPageButton guideImageButton;
    private SettingPageButton guideVideoButton;
    
    private AlphaAnimation fadeInAnimation;
    private AlphaAnimation fadeOutAnimation;
    private ScaleAnimation closeVidAnimation;
    private ScaleAnimation openVidAnimation;
    
    private GuideImageLayout guideImageLayout;
    
    private boolean isShowingGuideImage = false;
    private boolean lastChosenIsVideoButton = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.arise_activity_setting);
		
		makeBlurBackground();
		makePageTitleSection();
		makeTopSection();
		
		makeAboutView();
		makeVideoView();
		
		makeButtonsSection();
		
		createAnimations();
	}
	
	private void makeBlurBackground() {
		settingPageBackground = (ImageView) findViewById(R.id.setting_page_background);
		
		byte[] cameraFrame = SettingPage.this.getIntent().getExtras().getByteArray("lastCameraFrame");
		int width = SettingPage.this.getIntent().getExtras().getInt("cameraPreviewWidth");
		int height = SettingPage.this.getIntent().getExtras().getInt("cameraPreviewHeight");
		
		if (cameraFrame == null || width == 0 || height == 0) {
			settingPageBackground.setBackgroundColor(0xFFAAAAAA);
		} else {
			YuvImage yuvImg = new YuvImage(cameraFrame, ImageFormat.NV21, width, height, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvImg.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
			byte[] jpegByteArray = baos.toByteArray();
			Bitmap cameraFrameBitmap = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length); // bitmap of camera frame in landscape
			Matrix matrix = new Matrix();
			matrix.postRotate(90);
			Bitmap rotatedBmp = Bitmap.createBitmap(cameraFrameBitmap, 0, 0, width, height, matrix, true);
			
			GPUImageGaussianBlurFilter blurFilter = new GPUImageGaussianBlurFilter(2.5f);
			ArrayList<GPUImageFilter> filters = new ArrayList<GPUImageFilter>();
			filters.add(blurFilter);
			GPUImage.getBitmapForMultipleFilters(rotatedBmp, filters, new ResponseListener<Bitmap>() {

				@Override
				public void response(final Bitmap arg0) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							settingPageBackground.setImageBitmap(arg0);
						}
					});
				}
			});
		}
	}
	
	private void makePageTitleSection() {
		pageTitleSection = (RelativeLayout) findViewById(R.id.page_title_section);
		pageTitleSection.setBackgroundColor(ARiseConfigs.THEME_COLOR);
	}
	
	private void makeTopSection() {
		topSection = (RelativeLayout) findViewById(R.id.setting_page_top_section);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int width = displayMetrics.widthPixels;
		int height = ((width - 2 * 20) * 3 / 4) + 2 * 20;
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
		layoutParams.addRule(RelativeLayout.BELOW, pageTitleSection.getId());
		topSection.setLayoutParams(layoutParams);
	}
	
	private void makeButtonsSection() {
		
		buttonsSection = (LinearLayout) findViewById(R.id.buttonsSection);
		
		aboutButton = (SettingPageButton) findViewById(R.id.aboutButton);
		aboutButton.setText(LangPref.TXTABOUT);
		aboutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!(aboutButton.isChosen())) { // button is not being chosen
					lastChosenIsVideoButton = false;
					deselectAllButtons();
					aboutButton.setChosen(true);
					
					if (videoViewLayout != null && videoViewLayout.getVisibility() == View.VISIBLE) {
						pauseVideo();
					}
					
					showAboutView();
				}
			}
		});
		
		clearDataButton = (SettingPageButton) findViewById(R.id.clearDataButton);
		clearDataButton.setText(LangPref.TXTCLEARDATA);
		clearDataButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!(clearDataButton.isChosen())) {
					deselectAllButtons();
					clearDataButton.setChosen(true);
					
					if (videoViewLayout != null && videoViewLayout.getVisibility() == View.VISIBLE) {
						pauseVideo();
					}
					
					final ARiseDialogTwoButton dialog = new ARiseDialogTwoButton(SettingPage.this);
					dialog.setThemeColor(ARiseConfigs.THEME_COLOR);
					dialog.setTitleText(LangPref.TXTCLEARDATA);
					dialog.setMessageText(LangPref.TXTCONFIRMCLEARDATA);
					dialog.setButtonNo(LangPref.TXTNO, new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// dismiss the dialog
							dialog.dismiss();
						}
					});
					dialog.setButtonYes(LangPref.TXTYES, new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							ARiseUtils.deleteFileOrDirectory(new File(Shared.getAssetDir(SettingPage.this)));
							ARiseUtils.deleteFileOrDirectory(new File(Shared.getPosterDir(SettingPage.this)));
							DbHelper.getInstance(SettingPage.this).clearHistory();
							DbHelper.getInstance(SettingPage.this).clearBookmark();
							
							// dismiss the dialog
							dialog.dismiss();
						}
					});
					dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (lastChosenIsVideoButton) guideVideoButton.performClick();
							else aboutButton.performClick();
						}
					});
					
					dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							if (lastChosenIsVideoButton) guideVideoButton.performClick();
							else aboutButton.performClick();
						}
					});
					dialog.show();
				}
			}
		});
		
		guideImageButton = (SettingPageButton) findViewById(R.id.guideImageButton);
		guideImageButton.setText(LangPref.TXTGUIDE);
		guideImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!(guideImageButton.isChosen())) {
					deselectAllButtons();
					guideImageButton.setChosen(true);
					
					if (videoViewLayout != null && videoViewLayout.getVisibility() == View.VISIBLE) {
						pauseVideo();
					}
					
					guideImageLayout = new GuideImageLayout(SettingPage.this);
					guideImageLayout.setVisibility(View.VISIBLE);
					guideImageLayout.setGuideImage(ARiseConfigs.GUIDE_IMAGE, 80, 20);
					addContentView(guideImageLayout, guideImageLayout.getLayoutParams());
					guideImageLayout.showGuideImageView();
					isShowingGuideImage = true;
					
				}
			}
		});
		if (ARiseConfigs.GUIDE_IMAGE == null || ARiseConfigs.GUIDE_IMAGE.length() <= 0) {
			guideImageButton.setVisibility(View.GONE);
			buttonsSection.removeView(guideImageButton);
		}
		
		guideVideoButton = (SettingPageButton) findViewById(R.id.guideVideoButton);
		guideVideoButton.setText(LangPref.TXTVIDEO);
		guideVideoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!(guideVideoButton.isChosen())) { // button is not being chosen
					lastChosenIsVideoButton = true;
					deselectAllButtons();
					guideVideoButton.setChosen(true);
					showVideoView();
				}
			}
		});
		if (ARiseConfigs.GUIDE_VIDEO_URL == null || ARiseConfigs.GUIDE_VIDEO_URL.length() <= 0) {
			guideVideoButton.setVisibility(View.GONE);
			buttonsSection.removeView(guideVideoButton);
		}
		
		// modify frame of buttons
		float density = getResources().getDisplayMetrics().density;
		int buttonHeight = (int) (50 * density);
		int buttonMargin = (int) (5 * density);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int buttonsSectionMargin = (displayMetrics.heightPixels - ARiseUtils.getStatusBarHeightInContext(SettingPage.this) - pageTitleSection.getLayoutParams().height - topSection.getLayoutParams().height - (buttonHeight + 2 * buttonMargin) * buttonsSection.getChildCount()) / 2;
		((RelativeLayout.LayoutParams) buttonsSection.getLayoutParams()).setMargins(0, buttonsSectionMargin, 0, buttonsSectionMargin);
		
		for (int i = 0; i < buttonsSection.getChildCount(); i++) {
			SettingPageButton btn = (SettingPageButton) buttonsSection.getChildAt(i);
			((LinearLayout.LayoutParams) btn.getLayoutParams()).setMargins(displayMetrics.widthPixels / 4, buttonMargin, displayMetrics.widthPixels / 4, buttonMargin);
		}
	}
	
	private void deselectAllButtons() {
		if (aboutButton != null) aboutButton.setChosen(false);
		if (clearDataButton != null) clearDataButton.setChosen(false);
		if (guideImageButton != null) guideImageButton.setChosen(false);
		if (guideVideoButton != null) guideVideoButton.setChosen(false);
	}
	
	private void makeAboutView() {
		aboutViewLayout = new AboutView(SettingPage.this);
		aboutViewLayout.setAboutViewLogo(ARiseConfigs.LOGO);
		topSection.addView(aboutViewLayout);
	}
	
	private void makeVideoView() {
		if (ARiseConfigs.GUIDE_VIDEO_URL != null && ARiseConfigs.GUIDE_VIDEO_URL.length() > 0) {
			
			DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			
			videoViewLayout = (RelativeLayout) findViewById(R.id.video_view);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			layoutParams.width = displayMetrics.widthPixels - 2 * 20;
			layoutParams.height = (layoutParams.width * 3 / 4) + 2 * 20; 
			layoutParams.setMargins(20, 20, 20, 20);
			videoViewLayout.setLayoutParams(layoutParams);
			
			try {
				URL guideVideoURL = new URL(ARiseConfigs.GUIDE_VIDEO_URL);
				
				if (guideVideoURL.getProtocol().equals("file")) {
					// TODO: implementation for local file
				} else {
					if (guideVideoURL.getHost().equals("www.youtube.com")) {
						youtubeId = ARiseConfigs.GUIDE_VIDEO_URL.substring(ARiseConfigs.GUIDE_VIDEO_URL.lastIndexOf("/") + 1);
						youtubePlayerView = new YouTubePlayerView(SettingPage.this);
						RelativeLayout.LayoutParams youtubeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
						youtubePlayerView.setLayoutParams(youtubeLayoutParams);
						youtubePlayerView.initialize(ARiseConfigs.DEVELOPER_KEY, SettingPage.this);
						videoViewLayout.addView(youtubePlayerView);
					} else {
						Log.i(TAG, "Create videoView");
						videoView = new VideoView(SettingPage.this);
						Uri uri = Uri.parse(ARiseConfigs.GUIDE_VIDEO_URL);
						videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
							@Override
							public void onPrepared(MediaPlayer mp) {
								if (mp.getVideoWidth() != 0 && mp.getVideoHeight() != 0) {
									float whRatio = (float) mp.getVideoWidth() / mp.getVideoHeight();
									if (whRatio >= 4.f / 3.f) { // full width
										int vWidth = videoViewLayout.getLayoutParams().width;
										int vHeight = (int) (vWidth / whRatio);
										RelativeLayout.LayoutParams videoViewLayoutParams = new RelativeLayout.LayoutParams(vWidth, vHeight);
										videoViewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
										videoView.setLayoutParams(videoViewLayoutParams);
									} else { // full height
										int vHeight = videoViewLayout.getLayoutParams().height;
										int vWidth = (int) (vHeight * whRatio);
										RelativeLayout.LayoutParams videoViewLayoutParams = new RelativeLayout.LayoutParams(vWidth, vHeight);
										videoViewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
										videoView.setLayoutParams(videoViewLayoutParams);
									}
								}
								MediaController controller = new MediaController(SettingPage.this);
								controller.setAnchorView(videoView);
								controller.setMediaPlayer(videoView);
								videoView.setMediaController(controller);
							}
						});
						videoView.setVideoURI(uri);
						videoView.requestFocus();
						videoViewLayout.addView(videoView);
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "Error occurs while making video view", e);
				e.printStackTrace();
			}
		} else { // no video url was set
			
		}
	}
	
	public void onClosingGuideImageView() {
		fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				guideImageLayout.setVisibility(View.GONE);
				((ViewGroup) guideImageLayout.getParent()).removeView(guideImageLayout);
				isShowingGuideImage = false;
				if (lastChosenIsVideoButton) guideVideoButton.performClick();
				else aboutButton.performClick();
			}
		});
		guideImageLayout.startAnimation(fadeOutAnimation);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (aboutButton != null) aboutButton.performClick();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (videoViewLayout != null && videoViewLayout.getVisibility() == View.VISIBLE) {
			pauseVideo();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if (isShowingVideoFullscreen) {
			Log.i(TAG, "Video is on fullscreen. Go back to Settings page");
			if (youtubePlayerView != null) {
				youtubePlayer.setFullscreen(false);
			} else {
				
			}
		} else if (isShowingGuideImage) {
			onClosingGuideImageView();
		} else {
			super.onBackPressed();
			finish();
		}
	}

	@Override
	public void onInitializationSuccess(Provider mProvider, YouTubePlayer mYoutubePlayer, boolean mWasRestored) {
		youtubePlayer = mYoutubePlayer;
    	youtubePlayer.setPlayerStyle(PlayerStyle.DEFAULT);
    	youtubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
			@Override
			public void onFullscreen(boolean arg0) {
				isShowingVideoFullscreen = arg0;
			}
		});
    	youtubePlayer.cueVideo(youtubeId);
	}

	@Override
	protected Provider getYouTubePlayerProvider() {
		return youtubePlayerView;
	}
	
	private void showAboutView() {
		fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation anim) {
				aboutViewLayout.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation anim) {}
			
			@Override
			public void onAnimationEnd(Animation anim) {}
		});
		
		if (videoViewLayout != null && videoViewLayout.getVisibility() == View.VISIBLE) {
			closeVidAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					videoViewLayout.setVisibility(View.GONE);
					aboutViewLayout.startAnimation(fadeInAnimation);
				}
			});
			videoViewLayout.startAnimation(closeVidAnimation);
		} else {
			aboutViewLayout.startAnimation(fadeInAnimation);
		}
	}
	
	private void showVideoView() {
		openVidAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation anim) {
				videoViewLayout.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation anim) {}
			
			@Override
			public void onAnimationEnd(Animation anim) {}
		});
		
		if (aboutViewLayout.getVisibility() == View.VISIBLE) {
			fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					aboutViewLayout.setVisibility(View.GONE);
					videoViewLayout.startAnimation(openVidAnimation);
				}
			});
			aboutViewLayout.startAnimation(fadeOutAnimation);
		} else {
			videoViewLayout.startAnimation(openVidAnimation);
		}
	}
	
	private void pauseVideo() {
		if (youtubePlayer != null) {
			youtubePlayer.pause();
		}
		
		if (videoView != null) {
			videoView.pause();
		}
	}
	
	private void createAnimations() {
		fadeInAnimation = new AlphaAnimation(0, 1);
		fadeInAnimation.setDuration(300);
		fadeInAnimation.setRepeatCount(0);
		fadeInAnimation.setFillAfter(true);
		
		fadeOutAnimation = new AlphaAnimation(1, 0);
		fadeOutAnimation.setDuration(300);
		fadeOutAnimation.setRepeatCount(0);
		fadeOutAnimation.setFillAfter(true);
		
		if (videoViewLayout != null && videoViewLayout.getLayoutParams() != null) {
			closeVidAnimation = new ScaleAnimation(1, 0, 1, 1, videoViewLayout.getLayoutParams().width / 2, videoViewLayout.getLayoutParams().height / 2);
			closeVidAnimation.setDuration(300);
			closeVidAnimation.setRepeatCount(0);
			closeVidAnimation.setFillAfter(true);

			openVidAnimation = new ScaleAnimation(0, 1, 1, 1, videoViewLayout.getLayoutParams().width / 2, videoViewLayout.getLayoutParams().height / 2);
			openVidAnimation.setDuration(300);
			openVidAnimation.setRepeatCount(0);
			openVidAnimation.setFillAfter(true);
		}
	}
}
