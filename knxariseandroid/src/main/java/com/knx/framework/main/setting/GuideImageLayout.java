package com.knx.framework.main.setting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.knx.framework.R;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.utils.ARiseUtils;

public class GuideImageLayout extends RelativeLayout {
	
	private View dimBackground;
	private ImageView imageView;

	public GuideImageLayout(Context cxt) {
		super(cxt);
		initLayoutParams();
		initDimBackground();
		initImageView();
		
		setClickable(true); // VERY IMPORTANT: set clickable = true to prevent click event is passed to behind view
	}
	
	public GuideImageLayout(Context cxt, AttributeSet attrs) {
		super(cxt, attrs);
		initLayoutParams();
		initDimBackground();
		initImageView();
		
		setClickable(true); // VERY IMPORTANT: set clickable = true to prevent click event is passed to behind view
	}
	
	public GuideImageLayout(Context cxt, AttributeSet attrs, int defStyle) {
		super(cxt, attrs, defStyle);
		initLayoutParams();
		initDimBackground();
		initImageView();
		
		setClickable(true); // VERY IMPORTANT: set clickable = true to prevent click event is passed to behind view
	}
	
	private void initLayoutParams() {
		
		int width = getContext().getResources().getDisplayMetrics().widthPixels;
		int height = getContext().getResources().getDisplayMetrics().heightPixels - ARiseUtils.getStatusBarHeightInContext(getContext());
		
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
		setLayoutParams(layoutParams);
	}
	
	private void initDimBackground() {
		dimBackground = new View(getContext());
		dimBackground.setBackgroundColor(getContext().getResources().getColor(R.color.dim_background));
		dimBackground.setVisibility(View.GONE);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		dimBackground.setLayoutParams(layoutParams);
		
		dimBackground.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((SettingPage) getContext()).onClosingGuideImageView();
			}
		});
		
		addView(dimBackground);
	}
	
	private void initImageView() {
		imageView = new ImageView(getContext());
		imageView.setBackgroundColor(Color.WHITE);
		imageView.setVisibility(View.GONE);

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.setMargins(40, 40, 40, 40);
		imageView.setLayoutParams(layoutParams);
		
		// set this to prevent click event is passed to dim background view
		imageView.setClickable(true);
		
		addView(imageView);
	}
	
	public void setGuideImage(String imageName, int margin, int cornerRadius) {
		String uri = "@drawable/" + imageName;
		int imageResource = getResources().getIdentifier(uri, null, ARiseConfigs.PACKAGE_NAME);
		if (imageResource == 0) {
			
		} else {
			
			int maxWidth = getContext().getResources().getDisplayMetrics().widthPixels - 2 * margin;
			int maxHeight = getContext().getResources().getDisplayMetrics().heightPixels - 2 * margin;
			
			Drawable drawable = getContext().getResources().getDrawable(imageResource);
			float imageActualRatio = (float) drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
			
			if (((float) maxWidth / maxHeight) > imageActualRatio) { // full height
				int renderWidth = (int) (maxHeight * imageActualRatio);
				int renderHeight = maxHeight;
				
				RelativeLayout.LayoutParams imgLayout = new RelativeLayout.LayoutParams(renderWidth, renderHeight);
				imgLayout.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
				imageView.setLayoutParams(imgLayout);
			} else {
				int renderWidth = maxWidth;
				int renderHeight = (int) (maxWidth / imageActualRatio);
				
				RelativeLayout.LayoutParams imgLayout = new RelativeLayout.LayoutParams(renderWidth, renderHeight);
				imgLayout.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
				imageView.setLayoutParams(imgLayout);
			}
			
			imageView.setBackgroundColor(Color.TRANSPARENT);
			imageView.setImageBitmap(generateRoundedBitmap(imageResource, imageView.getLayoutParams().width, imageView.getLayoutParams().height));
		}
	}
	
	public void showGuideImageView() {
		AlphaAnimation fadeInAnimation = new AlphaAnimation(0, 1);
		fadeInAnimation.setDuration(300);
		fadeInAnimation.setRepeatCount(0);
		fadeInAnimation.setFillAfter(true);
		fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				dimBackground.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {}
		});
		
		ScaleAnimation popOutAnimation = new ScaleAnimation(0, 1, 0, 1,
				0.5f * (imageView.getLayoutParams().width),
				0.5f * (imageView.getLayoutParams().height));
		popOutAnimation.setDuration(300);
		popOutAnimation.setRepeatCount(0);
		popOutAnimation.setFillAfter(true);
		popOutAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				imageView.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {}
		});
		
		dimBackground.startAnimation(fadeInAnimation);
		imageView.startAnimation(popOutAnimation);
	}
	
	private Bitmap generateRoundedBitmap(int resId, int width, int height) {
		Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), resId);
		
		// resize bitmap
		float scaleWidth = (float) width * getContext().getResources().getDisplayMetrics().density / bmp.getWidth(); 
		float scaleHeight = (float) height * getContext().getResources().getDisplayMetrics().density / bmp.getHeight();
		
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		
		Bitmap resizedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		Bitmap output = Bitmap.createBitmap(resizedBmp.getWidth(), resizedBmp.getHeight(), Bitmap.Config.ARGB_8888);
		
		Canvas canvas = new Canvas(output);
		
		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, resizedBmp.getWidth(), resizedBmp.getHeight());
		RectF rectF = new RectF(rect);
		
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawRoundRect(rectF, 10 * getContext().getResources().getDisplayMetrics().density, 10 * getContext().getResources().getDisplayMetrics().density, paint);
		
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(resizedBmp, rect, rect, paint);
		
		return output;
	}
}
