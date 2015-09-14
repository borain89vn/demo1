package com.knx.framework.main.cameraUI;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class RoundedCornerView extends RelativeLayout {
	
	private float density = 1.f;
	private GradientDrawable viewDrawable;
	private float cornerRadius = 5.f;
	private int backgroundColor = Color.WHITE;
	
	public RoundedCornerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		density = context.getResources().getDisplayMetrics().density;
		initDrawable();
		
		setBackgroundDrawable(viewDrawable);
	}
 
	public RoundedCornerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		density = context.getResources().getDisplayMetrics().density;
		initDrawable();
		
		setBackgroundDrawable(viewDrawable);
	}
 
	public RoundedCornerView(Context context) {
		super(context);
		density = context.getResources().getDisplayMetrics().density;
		initDrawable();
		
		setBackgroundDrawable(viewDrawable);
	}
	
	private void initDrawable() {
		viewDrawable = new GradientDrawable();
		viewDrawable.setColor(backgroundColor);
		viewDrawable.setCornerRadius(cornerRadius * density);
		viewDrawable.setStroke(0, Color.TRANSPARENT);
	}
	
	public void setBackgroundColorForRoundedCorner(int color) {
		backgroundColor = color;
		viewDrawable.setColor(backgroundColor);
	}
	
	public void setCornerRadiusForRoundedCorner(int radius) {
		cornerRadius = radius;
		viewDrawable.setCornerRadius(cornerRadius * density);
	}
}
