package com.knx.framework.main.cameraUI;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knx.framework.helper.ARiseConfigs;

public class ARiseCameraTooltip extends RelativeLayout {
	
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	
	private ViewParent parentView;
	
	private Paint paint;
	private Paint backgroundPaint;
	private Paint textPaint;
	
	private final int BORDER_WIDTH = 3;
	
	private float density;
	
	private RectF rectF1;
	private RectF rectF2;
	
	private Path path;
	
	private Bitmap tooltipBitmap;
	private Rect tooltipBitmapRect;
	private RectF tooltipBitmapRectF;
	
	private TextView tooltipText;
	private TooltipHideButton tooltipHideButton;
	
	private boolean isShown;
	 
	// CONSTRUCTOR
	public ARiseCameraTooltip(Context context) {
		super(context);
		this.setFocusable(true);
		
		initViews();
	}
	
	public ARiseCameraTooltip(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setFocusable(true);
		
		initViews();
	}
	
	public ARiseCameraTooltip(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.setFocusable(true);
		
		initViews();
	}
	
	private void initViews() {
		
		this.setWillNotDraw(false);
		
		prefs = getContext().getSharedPreferences(ARiseConfigs.PREF_FILENAME, Context.MODE_PRIVATE);
		editor = prefs.edit();
		
		density = getContext().getResources().getDisplayMetrics().density;
		isShown = false;

		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(ARiseConfigs.THEME_COLOR);
		paint.setStyle(Paint.Style.STROKE); 
		paint.setStrokeWidth(BORDER_WIDTH * density);
		
		backgroundPaint = new Paint();
		backgroundPaint.setAntiAlias(true);
		backgroundPaint.setColor(Color.argb(225, 0, 0, 0));
		backgroundPaint.setStyle(Paint.Style.FILL);
		
		textPaint = new Paint(); 
		textPaint.setColor(Color.WHITE); 
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setTextSize((int) (17 * density + 0.5f));
		
		rectF1 = new RectF(getContext().getResources().getDisplayMetrics().widthPixels - (60 + 20) * density, (0 + BORDER_WIDTH) * density,
				getContext().getResources().getDisplayMetrics().widthPixels - 60 * density, (20 + BORDER_WIDTH) * density);
		rectF2 = new RectF(getContext().getResources().getDisplayMetrics().widthPixels - 60 * density, (0 + BORDER_WIDTH) * density,
				getContext().getResources().getDisplayMetrics().widthPixels - (60 - 20) * density, (20 + BORDER_WIDTH) * density);
		
		path = new Path();
		path.moveTo(0, (20 + BORDER_WIDTH) * density);
		path.lineTo(getContext().getResources().getDisplayMetrics().widthPixels - (60 + 20) * density, (20 + BORDER_WIDTH) * density);
		path.arcTo(rectF1, 90, -90);
		path.arcTo(rectF2, 180, 90);
		path.lineTo(getContext().getResources().getDisplayMetrics().widthPixels, (0 + BORDER_WIDTH) * density);
		path.lineTo(getContext().getResources().getDisplayMetrics().widthPixels, (100 + BORDER_WIDTH) * density);
		path.lineTo(0, (100 + BORDER_WIDTH) * density);
		path.lineTo(0, (20 + BORDER_WIDTH) * density);
		path.close();
		
		// text
		tooltipText = new TextView(getContext());
		RelativeLayout.LayoutParams txtParams = new RelativeLayout.LayoutParams(
				getContext().getResources().getDisplayMetrics().widthPixels - (int) (80 * density),
				RelativeLayout.LayoutParams.MATCH_PARENT);
		txtParams.setMargins((int) (80 * density), (int) ((20 + BORDER_WIDTH) * density), 0, 0);
		tooltipText.setGravity(Gravity.LEFT | Gravity.CENTER);
		tooltipText.setText("Avoid blur, glare, and shadow while scanning");
		tooltipText.setTextColor(Color.WHITE);
		tooltipText.setLayoutParams(txtParams);
		addView(tooltipText);
		
		tooltipHideButton = new TooltipHideButton(getContext());
		RelativeLayout.LayoutParams hideParams = new RelativeLayout.LayoutParams((int) (60 * density), (int) (20 * density));
		hideParams.setMargins((int) (getContext().getResources().getDisplayMetrics().widthPixels - 60 * density), (int) (BORDER_WIDTH * density), 0, 0);
		tooltipHideButton.setLayoutParams(hideParams);
		addView(tooltipHideButton);
		
		tooltipHideButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hide();
			}
		});
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawPath(path, backgroundPaint);

		canvas.drawLine(0, (20 + BORDER_WIDTH) * density, getContext().getResources().getDisplayMetrics().widthPixels - (60 + 10) * density, (20 + BORDER_WIDTH) * density, paint);
		canvas.drawArc(rectF1, 90, -90, false, paint);
		canvas.drawArc(rectF2, 180, 90, false, paint);
		canvas.drawLine(getContext().getResources().getDisplayMetrics().widthPixels - (60 - 10) * density, (0 + BORDER_WIDTH) * density,
				getContext().getResources().getDisplayMetrics().widthPixels, (0 + BORDER_WIDTH) * density, paint);
		
		if (tooltipBitmap != null) {
			canvas.drawBitmap(tooltipBitmap, tooltipBitmapRect, tooltipBitmapRectF, null);
		}
	}
	
	public void setParentView(ViewParent pView) {
		parentView = pView;
	}
	
	public void setTooltipText(String text) {
		tooltipText.setText(text);
	}
	
	public void setTooltipImage(final int resId) {
		((Activity) getContext()).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (resId == 0) {
					tooltipBitmap = null;
				} else {
					tooltipBitmap = BitmapFactory.decodeResource(getContext().getResources(), resId);
					tooltipBitmapRect = new Rect(0, 0, tooltipBitmap.getWidth(), tooltipBitmap.getWidth());
					tooltipBitmapRectF = new RectF(10 * density, (10 + 20 + BORDER_WIDTH) * density, 70 * density, (90 + BORDER_WIDTH) * density);
				}
			}
		});
	}
	
	public void show() {

		if (parentView == null)
			return;
		
		if (isShown)
			return;
		
		long lastShownTime = prefs.getLong(ARiseConfigs.PREF_LAST_SHOWN_TOOLTIP, 0);
		long diffTime = System.currentTimeMillis() - lastShownTime;
		Log.i("ARiseTooltip", "Diff time since last shown: " + diffTime);
		
		if (diffTime < 2 * 1000 * 60 * 60 * 24) {
			return;
		}
		
		editor.putLong(ARiseConfigs.PREF_LAST_SHOWN_TOOLTIP, System.currentTimeMillis());
		editor.commit();
		
		isShown = true;
		
		(new Timer()).schedule(new TimerTask() {
			
			@Override
			public void run() {
				((Activity) getContext()).runOnUiThread(new Runnable() {
					public void run() {
						RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) ((100 + BORDER_WIDTH) * density));
						layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
						layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
						setLayoutParams(layoutParams);

						((ViewGroup) parentView).addView(ARiseCameraTooltip.this, layoutParams);

						TranslateAnimation slidingUp = new TranslateAnimation(0, 0, (int) (100 * density), 0);
						slidingUp.setDuration(300);
						slidingUp.setStartOffset(300);
						slidingUp.setRepeatCount(0);
						slidingUp.setFillAfter(true);

						slidingUp.setAnimationListener(new Animation.AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {
								ARiseCameraTooltip.this.setVisibility(View.VISIBLE);
								ARiseCameraTooltip.this.setClickable(true);
							}

							@Override
							public void onAnimationEnd(Animation animation) {
								(new Timer()).schedule(new TimerTask() {
									
									@Override
									public void run() {
										hide();
									}
								}, 5000);
							}

							@Override
							public void onAnimationRepeat(Animation animation) { }
						});
						ARiseCameraTooltip.this.startAnimation(slidingUp);
					}
				});
			}
		}, 1000);
	}

	public void hide() {
		
		if (!isShown)
			return;
			
		((Activity) getContext()).runOnUiThread(new Runnable() {
			public void run() {

				TranslateAnimation slidingDown = new TranslateAnimation(0, 0, 0, (int) (100 * density));
				slidingDown.setDuration(300);
				slidingDown.setRepeatCount(0);
				slidingDown.setFillAfter(true);

				slidingDown.setAnimationListener(new Animation.AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						isShown = false;
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						ARiseCameraTooltip.this.setVisibility(View.GONE);
						((ViewGroup) parentView).removeView(ARiseCameraTooltip.this);
						parentView = null;
					}

					@Override
					public void onAnimationRepeat(Animation animation) { }
				});
				ARiseCameraTooltip.this.startAnimation(slidingDown);
			}
		});
	}
	
	private class TooltipHideButton extends View {
		
		private Paint tooltipHideButton;
		
		public TooltipHideButton(Context context) {
			super(context);
			
			initPaint();
		}
		
		public TooltipHideButton(Context context, AttributeSet attrs) {
			super(context, attrs);
			
			initPaint();
		}
		
		public TooltipHideButton(Context context, AttributeSet attrs, int defStyleAttr) {
			super(context, attrs, defStyleAttr);
			
			initPaint();
		}
		
		private void initPaint() {
			tooltipHideButton = new Paint();
			tooltipHideButton.setAntiAlias(true);
			tooltipHideButton.setColor(ARiseConfigs.THEME_COLOR);
			tooltipHideButton.setStyle(Paint.Style.STROKE);
			tooltipHideButton.setStrokeWidth(4 * density);
			tooltipHideButton.setStrokeCap(Cap.ROUND);
		}
		
		@Override
		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			canvas.drawLine(15 * density, 8 * density, (45 + BORDER_WIDTH) * density, 8 * density, tooltipHideButton);
			canvas.drawLine(15 * density, 14 * density, (45 + BORDER_WIDTH) * density, 14 * density, tooltipHideButton);
		}
	}
}
