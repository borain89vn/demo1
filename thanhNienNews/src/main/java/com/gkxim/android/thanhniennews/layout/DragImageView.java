/**
 * File: DragImageView.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 07-11-2013
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gkxim.android.utils.GKIMLog;

/**
 *
 */
public class DragImageView extends ImageView implements OnTouchListener {

	private static final String TAG = "DragImageView";
	private int mLastAction = -1;
	private int mLastX;
	private int mLastY;

	/**
	 * 07-11-2013
	 */
	public DragImageView(Context context) {
		super(context);
		init();
	}

	/**
	 * 07-11-2013
	 */
	public DragImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * 07-11-2013
	 */
	public DragImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		GKIMLog.lf(getContext(), 2, TAG + "=>init()");
		int vWidth = getWidth();
		int vHeight = getHeight();
		Drawable drawable = getDrawable();
		int dWidth = drawable.getIntrinsicWidth();
		int dHeight = drawable.getIntrinsicHeight();
		GKIMLog.lf(getContext(), 1, TAG + "=>view size [" + vWidth + ", "
				+ vHeight + "], image size [" + dWidth + ", " + dHeight + "]");
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Rect winrect = new Rect();
		getWindowVisibleDisplayFrame(winrect);
		final int windowwidth = winrect.width();
		final int windowheight = winrect.height();

		RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
		final int iconwidth = (int) (lp.width);
		final int iconheight = (int) (lp.height);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
		GKIMLog.lf(getContext(), 2, TAG + "=>onTouch: " + event.getAction());

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastX = (int) event.getRawX();
				mLastY = (int) event.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				if (mLastAction == MotionEvent.ACTION_DOWN) {
					break;
				}
				int x_cord = (int) event.getRawX();
				int y_cord = (int) event.getRawY();
				int dx = x_cord - mLastX;
				int dy = y_cord - mLastY;

				if (dx == 0 && dy == 0) {
					break;
				}
				int leftmargin = layoutParams.leftMargin;
				int topmargin = layoutParams.topMargin;
				
				if ((leftmargin + dx + iconwidth) > windowwidth) {
					leftmargin = windowwidth - iconwidth;
				} else if ((leftmargin + dx) >= 0) {
					leftmargin += dx;
				} 
				if ((topmargin + dy + iconheight) > windowheight) {
					topmargin = windowheight - iconheight;
				} else if ((topmargin + dy) >= 0) {
					topmargin += dy;
				} 
				GKIMLog.lf(null, 1, TAG + "=>[" + windowwidth + ", "
						+ windowheight + "], [" + iconwidth + ", " + iconheight
						+ "], [" + x_cord + ", " + y_cord + "], ["
						+ layoutParams.leftMargin + ", "
						+ layoutParams.topMargin + "], [" + leftmargin + ", "
						+ topmargin + "]");

				layoutParams.leftMargin = leftmargin;
				layoutParams.topMargin = topmargin;
				setLayoutParams(layoutParams);
				setTag((Boolean) true);
				mLastX = x_cord;
				mLastY = y_cord;
				return true;
			case MotionEvent.ACTION_UP:
				mLastAction = MotionEvent.ACTION_UP;
				Boolean tag = (Boolean) getTag();
				if (tag != null && tag.booleanValue()) {
					setTag(null);
					return true;
				}
				break;
			case MotionEvent.ACTION_CANCEL: {
				break;
			}

			case MotionEvent.ACTION_POINTER_UP: {
				break;
			}
		}
		mLastAction = event.getAction();
		return false;
	}

}
