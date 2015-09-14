/**
 * File: OverrideScrollView.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 29-12-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

/**
 * @author Timon Trinh
 */
@TargetApi(9)
public class OverrideScrollView extends ScrollView {

	private static final String TAG = "OverrideScrollView";
	private String mTAG;
	private OnOverScrolledListener mScrolledOutListener = new OnOverScrolledListener() {

		@Override
		public boolean onScrolledOut(View view, boolean outOfX, boolean outOfY,
				int scrollX, int scrollY) {
			GKIMLog.lf(null, 1, mTAG + "=>onScrolledOut: (" + scrollX + ", "
					+ scrollY + "), (" + outOfX + ", " + outOfY + ") - "
					+ view.getClass().getSimpleName());
			return false;
		}

		@Override
		public boolean onReleasedScrollOut(View view, boolean outOfX,
				boolean outOfY, int headX, int headY) {
			GKIMLog.lf(null, 1, mTAG + "=>onReleasedScrollOut: (" + outOfX
					+ ", " + outOfY + ")), (" + headX + ", " + headY + ") - "
					+ view.getClass().getSimpleName());
			return false;
		}
	};
	private boolean mScrolledOutX = false;
	private boolean mScrolledOutY = false;

	// int Out mode: -1 unspecified, 0-head out, 1-tail out.
	private int mOutXMode = -1;
	private int mOutYMode = -1;

	public interface OnOverScrolledListener {
		boolean onScrolledOut(View view, boolean outOfX, boolean outOfY,
				int scrollX, int scrollY);

		boolean onReleasedScrollOut(View view, boolean outOfX, boolean outOfY,
				int headX, int headY);
	}

	/**
	 * 29-12-2012
	 */
	public OverrideScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mTAG = TAG + " " + this.hashCode() + "(" + context.getClass().getSimpleName() + ")";
	}

	/**
	 * 29-12-2012
	 */
	public OverrideScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTAG = TAG + " " + this.hashCode()+ "(" + context.getClass().getSimpleName() + ")";
	}

	/**
	 * 29-12-2012
	 */
	public OverrideScrollView(Context context) {
		super(context);
		mTAG = TAG + " " + this.hashCode()+ "(" + context.getClass().getSimpleName() + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ScrollView#onOverScrolled(int, int, boolean, boolean)
	 */
	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
			boolean clampedY) {
		boolean hasProcessed = false;
		if ((clampedY || clampedX)) {
			if (mScrolledOutListener != null) {
				// mHasThrowX = clampedX;
				// mHasThrowY = clampedY;
				boolean hasChanged = false;
				if (clampedX && !mScrolledOutX) {
					mScrolledOutX = clampedX;
					hasChanged = true;
				}
				if (clampedY && !mScrolledOutY) {
					mScrolledOutY = clampedY;
					hasChanged = true;
				}
				if (hasChanged) {
					hasProcessed = mScrolledOutListener.onScrolledOut(this,
							clampedX, clampedY, scrollX, scrollY);
					if (clampedX) {
						if (scrollX <= 0) {
							mOutXMode = 0;
						} else {
							mOutXMode = 1;
						}
					}
					if (clampedY) {
						if (scrollY <= 0) {
							mOutYMode = 0;
						} else {
							mOutYMode = 1;
						}
					}
				}

			}
		}
		GKIMLog.lf(null, 0, mTAG + "=>onOverScrolled: " + hasProcessed);
		if (!hasProcessed) {
			super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
		}
//		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ScrollView#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = UIUtils.hasGingerbread() ? ev.getActionMasked() : ev
				.getAction();
		GKIMLog.lf(null, 0, mTAG + "=>onTouchEvent: " + action);
		switch (action) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			// do nothing
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			if (mScrolledOutX || mScrolledOutY) {
				if (mScrolledOutListener != null) {
					mScrolledOutListener.onReleasedScrollOut(
							this, mScrolledOutX, mScrolledOutY, mOutXMode,
							mOutYMode);
				}
				mScrolledOutX = false;
				mScrolledOutY = false;
				mOutXMode = -1;
				mOutYMode = -1;
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * @param mScrolledOutListener
	 *            the mScrolledOutListener to set
	 */
	public void setOnScrolledOutListener(
			OnOverScrolledListener mScrolledOutListener) {
		this.mScrolledOutListener = mScrolledOutListener;
	}

}
