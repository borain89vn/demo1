package com.knx.framework.main.setting;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knx.framework.R;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.helper.LangPref;

public class AboutView extends RelativeLayout {
	
	private ImageView aboutViewLogo;
	private TextView sdkName;
	private TextView sdkVersion;
	
	public AboutView(Context cxt) {
		super(cxt);
		initLayout();
		initImageView();
		initSDKName();
		initSDKVersion();
		setVisibility(View.GONE);
	}
	
	public AboutView(Context cxt, AttributeSet attrs) {
		super(cxt, attrs);
		initLayout();
		initImageView();
		initSDKName();
		initSDKVersion();
		setVisibility(View.GONE);
	}
	
	public AboutView(Context cxt, AttributeSet attrs, int defStyle) {
		super(cxt, attrs, defStyle);
		initLayout();
		initImageView();
		initSDKName();
		initSDKVersion();
		setVisibility(View.GONE);
	}
	
	private void initLayout() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int width = displayMetrics.widthPixels - 2 * 20;
		int height = width * 3 / 4;
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
		layoutParams.width = width;
		layoutParams.height = height;
		layoutParams.setMargins(20, 20, 20, 0);
		setLayoutParams(layoutParams);
	}
	
	private void initImageView() {
		aboutViewLogo = new ImageView(getContext());
		int size = getLayoutParams().height * 3 / 5;
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(size, size);
		layoutParams.setMargins(0, getLayoutParams().height / 10, 0, 0);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		aboutViewLogo.setLayoutParams(layoutParams);
		aboutViewLogo.setBackgroundResource(R.drawable.go_button);
		
		addView(aboutViewLogo);
	}
	
	private void initSDKName() {
		sdkName = new TextView(getContext());
		int height = getLayoutParams().height / 5;
		sdkName.setGravity(Gravity.CENTER);
		sdkName.setText(LangPref.TXTSDKNAME);
		sdkName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
		sdkName.setTextColor(Color.WHITE);
		sdkName.setTypeface(null, Typeface.BOLD);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
		layoutParams.setMargins(0, getLayoutParams().height * 7 / 10, 0, 0);
		sdkName.setLayoutParams(layoutParams);
		
		addView(sdkName);
	}
	
	private void initSDKVersion() {
		sdkVersion = new TextView(getContext());
		int height = getLayoutParams().height / 10;
		sdkVersion.setGravity(Gravity.CENTER);
		sdkVersion.setText(LangPref.TXTVERSION + " " + ARiseConfigs.SDK_VERSION);
		sdkVersion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		sdkVersion.setTextColor(Color.WHITE);
		sdkVersion.setTypeface(Typeface.create("san-serif-light", Typeface.ITALIC));
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
		layoutParams.setMargins(0, getLayoutParams().height * 9 / 10, 0, 0);
		sdkVersion.setLayoutParams(layoutParams);
		
		addView(sdkVersion);
	}
	
	public void setAboutViewLogo(String filename) {
		String uri = "@drawable/" + ARiseConfigs.LOGO;
		int logoResourceID = getResources().getIdentifier(uri, null, ARiseConfigs.PACKAGE_NAME);
		
		if (logoResourceID == 0) { // there is no logo, hide all views of instruction
			aboutViewLogo.setVisibility(View.GONE);
			((RelativeLayout.LayoutParams) sdkName.getLayoutParams()).setMargins(0, getLayoutParams().height * 7 / 20, 0, 0);
			((RelativeLayout.LayoutParams) sdkVersion.getLayoutParams()).setMargins(0, getLayoutParams().height * 11 / 20, 0, 0);
		} else { // set instruction text and logo
			aboutViewLogo.setBackgroundResource(logoResourceID);
		}
	}
	
	public void setAboutViewLogo(int resId) {
		aboutViewLogo.setBackgroundResource(resId);
	}
}
