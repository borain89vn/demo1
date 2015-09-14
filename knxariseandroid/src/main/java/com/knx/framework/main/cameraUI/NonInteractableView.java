package com.knx.framework.main.cameraUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knx.framework.helper.ARiseConfigs;

public class NonInteractableView extends RelativeLayout {
	
	private FocusAreaFrame focusAreaFrame;
	private InstructionView instructionView;
	
	public NonInteractableView(Context cxt) {
		super(cxt);
		initFocusAreaFrame();
		initInstructionView();
	}
	
	public NonInteractableView(Context cxt, AttributeSet attrs) {
		super(cxt, attrs);
		initFocusAreaFrame();
		initInstructionView();
	}
	
	public NonInteractableView(Context cxt, AttributeSet attrs, int defStyle) {
		super(cxt, attrs, defStyle);
		initFocusAreaFrame();
		initInstructionView();
	}
	
	private void initFocusAreaFrame() {
		focusAreaFrame = new FocusAreaFrame(getContext());
		addView(focusAreaFrame);
	}
	
	private void initInstructionView() {
		instructionView = new InstructionView(getContext());
		addView(instructionView);
	}
	
	public void setLogoFromResourceId(int resId) {
		if (resId == 0) { // there is no logo, hide all views of instruction
			instructionView.setVisibility(View.GONE);
			if (instructionView.getParent() != null) {
				((ViewGroup) instructionView.getParent()).removeView(instructionView);
			}
		} else { // set instruction text and logo
			instructionView.setLogoFromResourceId(resId);
			instructionView.setVisibility(View.VISIBLE);
			if (instructionView.getParent() != null) {
				((ViewGroup) instructionView.getParent()).removeView(instructionView);
			}
			addView(instructionView);
		}
	}
	
	private class FocusAreaFrame extends View {
		
		private float strokeWidth = 2.f;
		private Paint paint;
		
		private int top, left, bottom, right;
		private int length;
		
		public FocusAreaFrame(Context cxt) {
			super(cxt);
			initFocusAreaFrame();
		}
		
		public FocusAreaFrame(Context cxt, AttributeSet attrs) {
			super(cxt, attrs);
			initFocusAreaFrame();
		}
		
		public FocusAreaFrame(Context cxt, AttributeSet attrs, int defStyle) {
			super(cxt, attrs, defStyle);
			initFocusAreaFrame();
		}
		
		private void initFocusAreaFrame() {
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			setLayoutParams(layoutParams);
			
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.FILL);
			paint.setStrokeWidth(strokeWidth * getContext().getResources().getDisplayMetrics().density);
			
			int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
			int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
			float density = getContext().getResources().getDisplayMetrics().density;
			
			if (screenWidth / 8 < 50 * density) {
				left = (int) (60 * density);
				right = (int) (screenWidth - 60 * density);
			} else {
				left = screenWidth / 8;
				right = screenWidth * 7 / 8;
			}
			
			if (screenHeight / 8 < 50 * density) {
				top = (int) (60 * density);
				bottom = (int) (screenHeight - 60 * density);
			} else {
				top = screenHeight / 8;
				bottom = screenHeight * 7 / 8;
			}
			
			if ((right - left) / 3 < 50 * density) {
				length = (right - left) / 3;
			} else {
				length = (int) (50 * density);
			}
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			
			// top left
			canvas.drawLine(left, top - strokeWidth / 2, left, top + length, paint);
			canvas.drawLine(left - strokeWidth / 2, top, left + length, top, paint);
			
			// top right
			canvas.drawLine(right, top - strokeWidth / 2, right, top + length, paint);
			canvas.drawLine(right + strokeWidth / 2, top, right - length, top, paint);
			
			// bottom left
			canvas.drawLine(left, bottom + strokeWidth / 2, left, bottom - length, paint);
			canvas.drawLine(left - strokeWidth / 2, bottom, left + length, bottom, paint);
						
			// bottom right
			canvas.drawLine(right, bottom + strokeWidth / 2, right, bottom - length, paint);
			canvas.drawLine(right + strokeWidth / 2, bottom, right - length, bottom, paint);
			
			super.onDraw(canvas);
		}
	}
	
	private class InstructionView extends RelativeLayout {
		
		private ImageView logo;
		private TextView instructionText;
		
		public InstructionView(Context cxt) {
			super(cxt);
			initLayout();
		}
		
		public InstructionView(Context cxt, AttributeSet attrs) {
			super(cxt, attrs);
			initLayout();
		}
		
		public InstructionView(Context cxt, AttributeSet attrs, int defStyle) {
			super(cxt, attrs, defStyle);
			initLayout();
		}
		
		private void initLayout() {
			float density = getContext().getResources().getDisplayMetrics().density;
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) (160 * density), (int) (180 * density));
			layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
			setLayoutParams(layoutParams);
			
			instructionText = new TextView(getContext());
			instructionText.setText(getContext().getResources().getString(com.knx.framework.R.string.txtInstructionText));
			instructionText.setTextColor(Color.WHITE);
			instructionText.setGravity(Gravity.CENTER);
			RelativeLayout.LayoutParams textLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) (60 * density));
			textLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
			textLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
			instructionText.setLayoutParams(textLayoutParams);
			addView(instructionText);
			
			logo = new ImageView(getContext());
			RelativeLayout.LayoutParams logoLayoutParams = new RelativeLayout.LayoutParams((int) (120 * density), (int) (120 * density));
			logoLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
			logoLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
			logo.setLayoutParams(logoLayoutParams);
			addView(logo);
			
			setVisibility(View.GONE);
		}
		
		public void setLogoFromResourceId(int resId) {
			logo.setBackgroundResource(resId);
			logo.setAlpha(ARiseConfigs.LOGO_ALPHA);
		}
	}
}
