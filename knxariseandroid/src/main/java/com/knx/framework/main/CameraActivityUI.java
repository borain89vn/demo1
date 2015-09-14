package com.knx.framework.main;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knx.framework.R;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.main.cameraUI.ARiseCameraIndicator;
import com.knx.framework.main.cameraUI.ARiseCameraTooltip;
import com.knx.framework.main.cameraUI.NonInteractableView;
import com.knx.framework.main.cameraUI.RoundedCornerView;

public class CameraActivityUI {	
	
	private Context context;
	private ArrayList<View> viewList;
	
	private NonInteractableView nonInteractableView;
	private ARiseCameraIndicator recordingIndicator;
	private ImageButton settingButton, historyButton, exitButton;
	
	private RoundedCornerView connectionStatusContainer;
	
	ARiseCameraTooltip tooltip;
	
	private static CameraActivityUI singletonInstance;
	public static CameraActivityUI getSingletonInstance() {
		
		if (singletonInstance == null) {
			singletonInstance = new CameraActivityUI();
			singletonInstance.context = null;
			singletonInstance.viewList = new ArrayList<View>();
		}
		return singletonInstance;
	}
	
	public static void createNewInstance(Context cxt) {
		if (singletonInstance == null) {
			singletonInstance = getSingletonInstance(); 
		}
		
		singletonInstance.context = cxt;
		singletonInstance.loadUIFromXML();
		singletonInstance.show();
	}
	
	public static void destroySingletonInstance() {
		if (singletonInstance == null)
			return;
		singletonInstance = null;
	}
	
	private void loadUIFromXML() {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				DisplayMetrics displaymetrics = new DisplayMetrics();
				((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
				int screenWidth = displaymetrics.widthPixels;
				int screenHeight = displaymetrics.heightPixels;
				
				nonInteractableView = (NonInteractableView) ((Activity) context).findViewById(R.id.non_interactable_view);
				
				float density = context.getResources().getDisplayMetrics().density;
				int buttonSize = (int) (44.f * density);
				int buttonMargin = (int) (3.f * density);
				
				settingButton = (ImageButton) ((Activity) context).findViewById(R.id.settingBtn);
				RelativeLayout.LayoutParams settingButtonLayoutParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
				settingButtonLayoutParams.setMargins(buttonMargin, screenHeight - buttonSize - buttonMargin, 0, 0);
				settingButton.setLayoutParams(settingButtonLayoutParams);
				settingButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						((CameraActivity) context).onSettingButtonPressed();
					}
				});
				
				historyButton = (ImageButton) ((Activity) context).findViewById(R.id.historyBtn);
				RelativeLayout.LayoutParams historyButtonLayoutParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
				historyButtonLayoutParams.setMargins(screenWidth - buttonSize - buttonMargin, screenHeight - buttonSize - buttonMargin, 0, 0);
				historyButton.setLayoutParams(historyButtonLayoutParams);
				historyButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						((CameraActivity) context).onHistoryButtonPressed();
					}
				});
				
				exitButton = (ImageButton) ((Activity) context).findViewById(R.id.exitBtn);
				RelativeLayout.LayoutParams exitButtonLayoutParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
				exitButtonLayoutParams.setMargins(screenWidth - buttonSize - buttonMargin, buttonMargin, 0, 0);
				exitButton.setLayoutParams(exitButtonLayoutParams);
				exitButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						((Activity) context).finish();
					}
				});
				
				// set logo
				String uri = "@drawable/" + ARiseConfigs.LOGO;
				int logoResourceID = ((Activity) context).getResources().getIdentifier(uri, null, ARiseConfigs.PACKAGE_NAME);
				nonInteractableView.setLogoFromResourceId(logoResourceID);
				
				recordingIndicator = (ARiseCameraIndicator) ((Activity) context).findViewById(R.id.recordingIndicator);
				if (recordingIndicator != null) {
					recordingIndicator.setThemeColor(ARiseConfigs.THEME_COLOR);
				}
				
				viewList.add(settingButton);
				viewList.add(historyButton);
				viewList.add(exitButton);
				viewList.add(recordingIndicator);
				viewList.add(nonInteractableView);
				
				connectionStatusContainer = (RoundedCornerView) ((Activity) context).findViewById(R.id.connection_status_container);
				int connectionHPadding;
				if (screenWidth / 4 < 60 * context.getResources().getDisplayMetrics().density) {
					connectionHPadding = (int) (60 * context.getResources().getDisplayMetrics().density);
				} else {
					connectionHPadding = screenWidth / 4;
				}
				int connectionStatusLayoutParamsWidth = screenWidth - 2 * connectionHPadding;
				int connectionStatusLayoutParamsHeight = (int) (40 * context.getResources().getDisplayMetrics().density);
				RelativeLayout.LayoutParams connectionStatusLayoutParams = new RelativeLayout.LayoutParams(connectionStatusLayoutParamsWidth, connectionStatusLayoutParamsHeight);
				connectionStatusLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
				connectionStatusLayoutParams.setMargins(connectionHPadding, screenHeight - buttonSize - connectionStatusLayoutParamsHeight, connectionHPadding, 0);
				connectionStatusContainer.setLayoutParams(connectionStatusLayoutParams);
				connectionStatusContainer.setBackgroundColorForRoundedCorner(Color.BLACK);
				connectionStatusContainer.setAlpha(0.8f);
			}
		});
	}
	
	public void show() {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (viewList != null) {
					for (View view:viewList) {
						view.setVisibility(View.VISIBLE);
					}
				}
				
				if ((ARiseConfigs.TOOLTIP_IMAGE == null || ARiseConfigs.TOOLTIP_IMAGE.length() == 0) ||
						(ARiseConfigs.TOOLTIP_TEXT == null || ARiseConfigs.TOOLTIP_TEXT.length() == 0)) {
					// invalid
				} else {
					if (tooltip == null) {
						tooltip = new ARiseCameraTooltip(context);
						tooltip.setParentView(exitButton.getParent());
						String uri = "@drawable/" + ARiseConfigs.TOOLTIP_IMAGE;
						int tooltipImgResourceID = ((Activity) context).getResources().getIdentifier(uri, null, ARiseConfigs.PACKAGE_NAME);
						tooltip.setTooltipImage(tooltipImgResourceID);
						tooltip.setTooltipText(ARiseConfigs.TOOLTIP_TEXT);
					}
					tooltip.show();
				}
			}
		});
	}
	
	public void hide() {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (viewList != null) {
					for (View view : viewList) {
						view.setVisibility(View.GONE);
					}
				}
				
				if ((ARiseConfigs.TOOLTIP_IMAGE == null || ARiseConfigs.TOOLTIP_IMAGE.length() == 0) ||
						(ARiseConfigs.TOOLTIP_TEXT == null || ARiseConfigs.TOOLTIP_TEXT.length() == 0)) {
					// invalid
				} else {
					tooltip.hide();
				}
			}
		});
	}
	
	public void displayToastWithText(final String text, final long period) {
		((Activity) context).runOnUiThread(new Runnable() {
			public void run() {
				TextView connectionStatus = (TextView) ((Activity) context).findViewById(R.id.connection_status);
				connectionStatus.setText(text);
				
				if (connectionStatusContainer != null && connectionStatusContainer.getVisibility() != View.VISIBLE) {
					ScaleAnimation appearAnimation = new ScaleAnimation(0, 1, 0, 1,
							connectionStatusContainer.getLayoutParams().width / 2, connectionStatusContainer.getLayoutParams().height / 2);
					appearAnimation.setDuration(300);
					appearAnimation.setRepeatCount(0);
					appearAnimation.setFillAfter(true);
					appearAnimation.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation anim) {
							connectionStatusContainer.setVisibility(View.VISIBLE);
						}
						
						@Override
						public void onAnimationRepeat(Animation anim) {}
						
						@Override
						public void onAnimationEnd(Animation anim) {}
					});
					connectionStatusContainer.startAnimation(appearAnimation);
				}
			}
		});
	}
	
	private boolean isPerformingDisappearAnimation = false;
	public void hideToast() {
		((Activity) context).runOnUiThread(new Runnable() {
			public void run() {
				if (connectionStatusContainer != null && connectionStatusContainer.getVisibility() == View.VISIBLE && !isPerformingDisappearAnimation) {
					ScaleAnimation disappearAnimation = new ScaleAnimation(1, 0, 1, 0,
							connectionStatusContainer.getLayoutParams().width / 2, connectionStatusContainer.getLayoutParams().height / 2);
					disappearAnimation.setDuration(300);
					disappearAnimation.setRepeatCount(0);
					disappearAnimation.setFillAfter(true);
					disappearAnimation.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation anim) {
							isPerformingDisappearAnimation = true;
						}
						
						@Override
						public void onAnimationRepeat(Animation anim) {}
						
						@Override
						public void onAnimationEnd(Animation anim) {
							connectionStatusContainer.setVisibility(View.GONE);
							isPerformingDisappearAnimation = false;
						}
					});
					connectionStatusContainer.startAnimation(disappearAnimation);
				}
			}
		});
	}
	
//	private ARisePopupInstruction instruction;
//	public void showPopupInstruction() {
//		Log.i("ARiseUI", "Show popup instruction");
//		if (instruction == null) {
//			instruction = new ARisePopupInstruction(context);
//			String uri = "@drawable/" + ARiseConfigs.GUIDE_IMAGE;
//			int imageResource = context.getResources().getIdentifier(uri, null, ARiseConfigs.PACKAGE_NAME);
//			instruction.setInstructionImage(imageResource);
//			((ViewGroup) recordingIndicator.getParent()).addView(instruction, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
//			instruction.showInstruction();
//		}
//	}
//	
//	public void hidePopupInstruction() {
//		instruction = null;
//	}
	
	public void setCameraIndicatorAnimate(boolean flag) {
		if (recordingIndicator != null) {
			if (flag) {
				recordingIndicator.startAnimating();
			} else {
				recordingIndicator.stopAnimating();
			}
		}
		
	}
}
