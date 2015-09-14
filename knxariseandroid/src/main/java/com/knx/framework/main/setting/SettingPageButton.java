package com.knx.framework.main.setting;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.Button;

import com.knx.framework.helper.ARiseConfigs;

public class SettingPageButton extends Button {
	
	private float density = 1.0f;
	
	private boolean chosen;
	private GradientDrawable selectedDrawable;
	private GradientDrawable unselectedDrawable;
	
	private float cornerRadius = 5.f;
	private float borderWidth = 1.f;
	
	public SettingPageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		density = context.getResources().getDisplayMetrics().density;
		initSelectedDrawable();
		initUnselectedDrawable();
		setChosen(false);
	}
 
	public SettingPageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		density = context.getResources().getDisplayMetrics().density;
		initSelectedDrawable();
		initUnselectedDrawable();
		setChosen(false);
	}
 
	public SettingPageButton(Context context) {
		super(context);
		density = context.getResources().getDisplayMetrics().density;
		initSelectedDrawable();
		initUnselectedDrawable();
		setChosen(false);
	}
	
	private void initSelectedDrawable() {
		selectedDrawable = new GradientDrawable();
		selectedDrawable.setColor(ARiseConfigs.THEME_COLOR);
		selectedDrawable.setCornerRadius(cornerRadius * density);
		selectedDrawable.setStroke(0, Color.TRANSPARENT);
	}
	
	private void initUnselectedDrawable() {
		unselectedDrawable = new GradientDrawable();
		unselectedDrawable.setColor(Color.TRANSPARENT);
		unselectedDrawable.setCornerRadius(cornerRadius * density);
		unselectedDrawable.setStroke((int) (borderWidth * density), Color.WHITE);
	}
	
	public void setCornerRadius(float radius) {
		cornerRadius = radius;
		selectedDrawable.setCornerRadius(cornerRadius * density);
		unselectedDrawable.setCornerRadius(cornerRadius * density);
	}
	
	public void setBorderWidth(float width) {
		borderWidth = width;
		selectedDrawable.setStroke((int) (borderWidth * density), Color.WHITE);
		unselectedDrawable.setStroke(0, Color.TRANSPARENT);
	}
	
	public void setChosen(boolean c) {
		chosen = c;
		if (chosen) {
			setBackgroundDrawable(selectedDrawable);
			this.setTypeface(null, Typeface.BOLD);
		} else {
			setBackgroundDrawable(unselectedDrawable);
			this.setTypeface(null, Typeface.NORMAL);
		}
	}
	
	public boolean isChosen() {
		return chosen;
	}
}
