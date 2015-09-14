package com.knx.framework.main.history;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

// follow: https://github.com/lopspower/CircularImageView
public class HistoryThumbnailImageView extends ImageView {
	
	private int canvasSize;
	private Paint paint;
	
	public HistoryThumbnailImageView(Context cxt) {
		super(cxt);
		
		paint = new Paint();
	}

	public HistoryThumbnailImageView(Context cxt, AttributeSet attrs) {
		super(cxt, attrs);
		
		paint = new Paint();
	}

	public HistoryThumbnailImageView(Context cxt, AttributeSet attrs, int defStyle) {
		super(cxt, attrs, defStyle);
		
		paint = new Paint();
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		Bitmap image = drawableToBitmap(getDrawable());
		
		// init shader
		if (image != null) {
			canvasSize = canvas.getWidth();
			if (canvas.getHeight() < canvasSize)
			canvasSize = canvas.getHeight();
	
			BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(image, canvasSize, canvasSize, false), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
			paint.setShader(shader);
	
			// circleCenter is the x or y of the view's center
			// radius is the radius in pixels of the cirle to be drawn
			// paint contains the shader that will texture the shape
			int circleCenter = canvasSize/ 2;
			canvas.drawCircle(circleCenter, circleCenter, canvasSize / 2, paint);
		}
	}
	
	private Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable == null) {
			return null;
		} else if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}
		
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
		drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		
		return bitmap;
	}
}
