package com.knx.framework.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.knx.framework.R;

public class ARiseDialogTwoButton extends Dialog {
	
	private int themeColor;
	private float cornerRadius;
	private int strokeWidth;
	
	private LinearLayout dialogLayout;
	private TextView title;
	private View separator;
	private TextView message;
	private Button buttonYes;
	private Button buttonNo;
	
	public ARiseDialogTwoButton(Context context) {
		super(context);
		
		themeColor = getContext().getResources().getColor(R.color.default_theme_color);
		cornerRadius = 5.f;
		strokeWidth = 1;
		
		prepareViews();
		
		this.setCancelable(true);
	}
	
	public ARiseDialogTwoButton(Context context, int theme) {
		super(context, theme);
		
		themeColor = getContext().getResources().getColor(R.color.default_theme_color);
		cornerRadius = 5.f;
		strokeWidth = 1;
		
		prepareViews();
		
		this.setCancelable(true);
	}
	
	/**
	 * A helper method for preparing views used in the dialog.
	 */
	private void prepareViews() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		setContentView(R.layout.dialog_two_buttons);
		
		dialogLayout = (LinearLayout) findViewById(R.id.dialog_two_buttons_layout);
		title = (TextView) findViewById(R.id.dialog_two_buttons_title);
		separator = (View) findViewById(R.id.dialog_two_buttons_separator);
		message = (TextView) findViewById(R.id.dialog_two_buttons_message);
		buttonNo = (Button) findViewById(R.id.dialog_two_buttons_button_no);
		buttonYes = (Button) findViewById(R.id.dialog_two_buttons_button_yes);
		
		updateViews();
	}
	
	/**
	 * A helper method for updating views used in the dialog after some changes.
	 */
	private void updateViews() {
		dialogLayout.setBackgroundDrawable(generateFillGradientDrawable(cornerRadius, Color.WHITE));
		title.setTextColor(themeColor);
		separator.setBackgroundColor(themeColor);
		buttonNo.setTextColor(themeColor);
		buttonNo.setBackgroundDrawable(generateOutlineGradientDrawable(cornerRadius, strokeWidth, themeColor));
		buttonYes.setTextColor(Color.WHITE);
		buttonYes.setBackgroundDrawable(generateFillGradientDrawable(cornerRadius, themeColor));
		
		// if there is no title or title is empty, the dialog automatically hides it and the separator
		if (title.getText() != null && title.getText().length() > 0) {
			title.setVisibility(View.VISIBLE);
			separator.setVisibility(View.VISIBLE);
			
			float density = getContext().getResources().getDisplayMetrics().density;
			((LinearLayout.LayoutParams) message.getLayoutParams()).setMargins(
					(int) (30 * density), (int) (20 * density),
					(int) (30 * density), (int) (20 * density));
		} else {
			title.setVisibility(View.GONE);
			separator.setVisibility(View.GONE);
			
			float density = getContext().getResources().getDisplayMetrics().density;
			((LinearLayout.LayoutParams) message.getLayoutParams()).setMargins(
					(int) (20 * density), (int) (20 * density),
					(int) (20 * density), (int) (20 * density));
		}
	}
	
	public void setTitleText(String text) {
		title.setText(text);
		
		updateViews();
	}
	
	public void setMessageText(String text) {
		message.setText(text);
		
		updateViews();
	}
	
	public void setMessageSpan(SpannableStringBuilder ssb) {
		message.setText(ssb, BufferType.SPANNABLE);
		
		updateViews();
	}
	
	public void setButtonNo(String text, View.OnClickListener listener) {
		buttonNo.setText(text);
		buttonNo.setOnClickListener(listener);
		
		updateViews();
	}
	
	public void setButtonYes(String text, View.OnClickListener listener) {
		buttonYes.setText(text);
		buttonYes.setOnClickListener(listener);
		
		updateViews();
	}
	
	public void setThemeColor(int newThemeColor) {
		themeColor = newThemeColor;
		
		updateViews();
	}
	
	public void setCornerRadius(float radius) {
		cornerRadius = radius;
		
		updateViews();
	}
	
	public void setStrokeWidth(int width) {
		strokeWidth = width;
		
		updateViews();
	}
	
	private GradientDrawable generateFillGradientDrawable(float cornerRadius, int backgroundColor) {
		GradientDrawable gradientDrawable = new GradientDrawable();
		gradientDrawable.setCornerRadius(cornerRadius * getContext().getResources().getDisplayMetrics().density);
		gradientDrawable.setColor(backgroundColor);
		return gradientDrawable;
	}
	
	private GradientDrawable generateOutlineGradientDrawable(float cornerRadius, int sWidth, int strokeColor) {
		GradientDrawable gradientDrawable = new GradientDrawable();
		gradientDrawable.setCornerRadius(cornerRadius * getContext().getResources().getDisplayMetrics().density);
		gradientDrawable.setColor(Color.TRANSPARENT);
		int strokeWidthInDIP;
		if (sWidth * getContext().getResources().getDisplayMetrics().density - ((int) (sWidth * getContext().getResources().getDisplayMetrics().density)) <= 0.5f) {
			strokeWidthInDIP = (int) (sWidth * getContext().getResources().getDisplayMetrics().density);
		} else {
			strokeWidthInDIP = (int) (sWidth * getContext().getResources().getDisplayMetrics().density) + 1;
		}
		gradientDrawable.setStroke(strokeWidthInDIP, strokeColor);
		return gradientDrawable;
	}
}
