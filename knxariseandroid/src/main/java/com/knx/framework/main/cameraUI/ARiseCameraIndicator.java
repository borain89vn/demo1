package com.knx.framework.main.cameraUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class ARiseCameraIndicator extends ImageButton {
	
	private Paint outerPaint;
	private Paint innerPaint;
	
	private float strokeWidth = 3.f;
	private float strokeSpacing = 1.f;
	
	private RectF outerRectF;
	private RectF innerRectF;
	
	private float density = 1.0f;
	private float curOuterAngle = 0.f;
	
	private int themeColor;
	private int darkenThemeColor;
	
	private boolean animationFlag;
	
	private Handler handler;
	private Runnable runnable;
	
	private final float DELTA_ANGLE = 5.f;

	public ARiseCameraIndicator(Context context) {
		super(context);

		density = context.getResources().getDisplayMetrics().density;
		initPaintObject();
	}
	
	public ARiseCameraIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		density = context.getResources().getDisplayMetrics().density;
		initPaintObject();
	}
	
	public ARiseCameraIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		density = context.getResources().getDisplayMetrics().density;
		initPaintObject();
	}
	
	public void setStrokeWidth(float w) {
		strokeWidth = w;
		initPaintObject();
	}
	
	private void initPaintObject() {
		outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		outerPaint.setStyle(Paint.Style.STROKE);
	    outerPaint.setStrokeWidth(strokeWidth * density);
	    
	    innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		innerPaint.setStyle(Paint.Style.STROKE);
	    innerPaint.setStrokeWidth(strokeWidth * density);
	    
	    setThemeColor(Color.WHITE);
	    animationFlag = true;
	    
	    handler = new Handler();
	    runnable = new Runnable() {
	    	@Override
	    	public void run() {
	    		invalidate();
	    	}
	    };
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		if (animationFlag) {
			curOuterAngle = curOuterAngle + DELTA_ANGLE;
			if (curOuterAngle > 360)
				curOuterAngle = curOuterAngle - 360;
		}
		
		if (outerRectF == null)
			outerRectF = new RectF(strokeWidth * density, strokeWidth * density,
					this.getWidth() - strokeWidth * density,
					this.getHeight() - strokeWidth * density);
	    
		if (innerRectF == null)
			innerRectF = new RectF((strokeWidth + strokeSpacing + strokeWidth) * density, (strokeWidth + strokeSpacing + strokeWidth) * density,
					this.getWidth() - (strokeWidth + strokeSpacing + strokeWidth) * density,
					this.getHeight() - (strokeWidth + strokeSpacing + strokeWidth) * density);
		
	 	canvas.drawArc(outerRectF, 0.f + curOuterAngle, 50.f, false, outerPaint);
	 	canvas.drawArc(outerRectF, 60.f + curOuterAngle, 50.f, false, outerPaint);
	 	canvas.drawArc(outerRectF, 120.f + curOuterAngle, 50.f, false, outerPaint);
	 	canvas.drawArc(outerRectF, 180.f + curOuterAngle, 50.f, false, outerPaint);
	 	canvas.drawArc(outerRectF, 240.f + curOuterAngle, 50.f, false, outerPaint);
	 	canvas.drawArc(outerRectF, 300.f + curOuterAngle, 50.f, false, outerPaint);
	 	
	 	canvas.drawArc(innerRectF, 55.f - curOuterAngle, 70.f, false, innerPaint);
	 	canvas.drawArc(innerRectF, 145.f - curOuterAngle, 70.f, false, innerPaint);
	 	canvas.drawArc(innerRectF, 235.f - curOuterAngle, 70.f, false, innerPaint);
	 	canvas.drawArc(innerRectF, 325.f - curOuterAngle, 70.f, false, innerPaint);
	 	
	 	handler.postDelayed(runnable, 30);
	 	
		super.onDraw(canvas);
	}
	
	public void startAnimating() {
		animationFlag = true;
	}
	
	public void stopAnimating() {
		animationFlag = false;
	}
	
	public void setThemeColor(int tColor) {
		themeColor = tColor;
		
		outerPaint.setColor(themeColor);
		
		float[] hsv = new float[3];
		Color.colorToHSV(themeColor, hsv);
		hsv[2] *= 0.8f; // value component
		darkenThemeColor = Color.HSVToColor(hsv);
		
		innerPaint.setColor(darkenThemeColor);
	}	
}
