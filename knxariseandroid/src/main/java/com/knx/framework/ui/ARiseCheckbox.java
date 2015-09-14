package com.knx.framework.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class ARiseCheckbox extends ImageButton {
	
	private boolean state;
	private Paint strokePaint;
	private Paint fillPaint;
	private float strokeWidth = 2.f;
	
	private float density = 1.0f;

	public ARiseCheckbox(Context context) {
		super(context);

		state = false;
		density = context.getResources().getDisplayMetrics().density;
		initPaintObject();
	}
	
	public ARiseCheckbox(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		state = false;
		density = context.getResources().getDisplayMetrics().density;
		initPaintObject();
	}
	
	public ARiseCheckbox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		state = false;
		density = context.getResources().getDisplayMetrics().density;
		initPaintObject();
	}
	
	public void setStrokeWidth(float w) {
		strokeWidth = w;
		initPaintObject();
	}
	
	public void toggle() {
		state = !state;
		invalidate();
	}
	
	public boolean getState() {
		return state;
	}
	
	private void initPaintObject() {
		strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		strokePaint.setStyle(Paint.Style.STROKE);
	    strokePaint.setStrokeWidth(strokeWidth * density);
	    strokePaint.setColor(Color.WHITE);
	    
	    fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    fillPaint.setStyle(Paint.Style.FILL);
	    fillPaint.setColor(Color.WHITE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!state) {
			float radius = (getWidth() - strokeWidth * density) / 2.f;
	    	canvas.drawCircle(getWidth() / 2.f, getWidth() / 2.f, radius, strokePaint);
		} else {
			float radius = (getWidth() - strokeWidth * density) / 2.f;
			canvas.drawCircle(getWidth() / 2.f, getWidth() / 2.f, radius, strokePaint);
	    	canvas.drawCircle(getWidth() / 2.f, getWidth() / 2.f, radius / 2, fillPaint);
		}
		
		super.onDraw(canvas);
	}
}
