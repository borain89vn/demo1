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

public class ARiseDialogOneButton extends Dialog {
	
	private int themeColor;
	private float cornerRadius;
	
	private LinearLayout dialogLayout;
	private TextView title;
	private View separator;
	private TextView message;
	private Button button;

	public ARiseDialogOneButton(Context context) {
		super(context);
		
		themeColor = getContext().getResources().getColor(R.color.default_theme_color);
		cornerRadius = 5.f;
		
		prepareViews();
		
		this.setCancelable(true);
	}
	
	public ARiseDialogOneButton(Context context, int theme) {
		super(context, theme);
		
		themeColor = getContext().getResources().getColor(R.color.default_theme_color);
		cornerRadius = 5.f;
		
		prepareViews();
		
		this.setCancelable(true);
	}
	
	/**
	 * A helper method for preparing views used in the dialog.
	 */
	private void prepareViews() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		setContentView(R.layout.dialog_one_button);
		
		dialogLayout = (LinearLayout) findViewById(R.id.dialog_one_button_layout);
		title = (TextView) findViewById(R.id.dialog_one_button_title);
		separator = (View) findViewById(R.id.dialog_one_button_separator);
		message = (TextView) findViewById(R.id.dialog_one_button_message);
		button = (Button) findViewById(R.id.dialog_one_button_button);
		
		updateViews();
	}
	
	/**
	 * A helper method for updating views used in the dialog after some changes.
	 */
	private void updateViews() {
		dialogLayout.setBackgroundDrawable(generateFillGradientDrawable(cornerRadius, Color.WHITE));
		title.setTextColor(themeColor);
		separator.setBackgroundColor(themeColor);
		button.setBackgroundDrawable(generateFillGradientDrawable(cornerRadius, themeColor));
		
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
	
	public void setButtonText(String text) {
		button.setText(text);
		
		updateViews();
	}
	
	public void setButtonOnClickListener(View.OnClickListener listener) {
		button.setOnClickListener(listener);
	}
	
	public void setThemeColor(int newThemeColor) {
		themeColor = newThemeColor;
		
		updateViews();
	}
	
	public void setCornerRadius(float radius) {
		cornerRadius = radius;
		
		updateViews();
	}
	
	private GradientDrawable generateFillGradientDrawable(float cornerRadius, int backgroundColor) {
		GradientDrawable gradientDrawable = new GradientDrawable();
		gradientDrawable.setCornerRadius(cornerRadius * getContext().getResources().getDisplayMetrics().density);
		gradientDrawable.setColor(backgroundColor);
		return gradientDrawable;
	}
}
