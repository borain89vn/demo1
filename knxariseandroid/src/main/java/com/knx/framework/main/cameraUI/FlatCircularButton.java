package com.knx.framework.main.cameraUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class FlatCircularButton extends ImageButton {

	private Paint myPaint;
	private float strokeWidth = 2.f;
	
	private float density = 1.0f;

	public FlatCircularButton(Context context) {
		super(context);

		density = context.getResources().getDisplayMetrics().density;
		initPaintObject();
	}
	
	public FlatCircularButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		density = context.getResources().getDisplayMetrics().density;
		initPaintObject();
	}
	
	public FlatCircularButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		density = context.getResources().getDisplayMetrics().density;
		initPaintObject();
	}
	
	public void setStrokeWidth(float w) {
		strokeWidth = w;
		initPaintObject();
	}
	
	private void initPaintObject() {
		myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		myPaint.setStyle(Paint.Style.STROKE);
	    myPaint.setStrokeWidth(strokeWidth * density);
	    myPaint.setColor(Color.WHITE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float radius = (getWidth() - strokeWidth * density) / 2.f;
	    canvas.drawCircle(getWidth() / 2.f, getWidth() / 2.f, radius, myPaint);
		
		super.onDraw(canvas);
	}
}
