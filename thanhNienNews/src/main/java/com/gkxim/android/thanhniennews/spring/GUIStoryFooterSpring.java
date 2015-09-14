/**
 * File: GUIStoryFooter.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 03-01-2013
 * 
 */
package com.gkxim.android.thanhniennews.spring;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

/**
 * @author Timon Trinh
 */
public class GUIStoryFooterSpring extends LinearLayout implements
		View.OnClickListener {

	private static final String TAG = GUIStoryFooterSpring.class
			.getSimpleName();
	private OnClickListener mOnClickListener = null;
	private ImageButton mIBBack;
	private ImageButton mIBCheck;
	private ImageButton mIBShare;
	private TextView mTvCommentCount = null;
	private TextView mTvFbLikeCount;

	/**
	 * 03-01-2013
	 */
	public GUIStoryFooterSpring(Context context) {
		super(context);
		initListViewItems();
	}

	/**
	 * 03-01-2013
	 */
	public GUIStoryFooterSpring(Context context, AttributeSet attrs) {
		super(context, attrs);
		initListViewItems();
	}

	/**
	 * 03-01-2013
	 */
	@SuppressLint("NewApi")
	public GUIStoryFooterSpring(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initListViewItems();
	}

	private void initListViewItems() {
		GKIMLog.lf(this.getContext(), 0, TAG
				+ "=>initListViewItems from context: "
				+ getContext().getClass().getSimpleName());
		inflate(getContext(), R.layout.bar_storydetail_footer_spring, this);
		mIBBack = (ImageButton) findViewById(R.id.imb_storyfooter_back);
		if (mIBBack != null) {
			mIBBack.setOnClickListener(this);
		}
		ImageButton ibtn = (ImageButton) findViewById(R.id.imb_storyfooter_textsize);
		if (ibtn != null) {
			ibtn.setVisibility(View.VISIBLE);
			ibtn.setOnClickListener(this);
		}
		ibtn = (ImageButton) findViewById(R.id.imb_storyfooter_addcomment);
		if (ibtn != null) {
			ibtn.setOnClickListener(this);
		}
		mIBCheck = (ImageButton) findViewById(R.id.imb_storyfooter_check);
		if (mIBCheck != null) {
			mIBCheck.setOnClickListener(this);
		}
		mIBShare = (ImageButton) findViewById(R.id.imb_storyfooter_share);
		if (mIBShare != null) {
			mIBShare.setOnClickListener(this);
		}
		if (!UIUtils.isTablet(getContext())) {
			mTvCommentCount = (TextView) findViewById(R.id.tv_storyfooter_addcomment_count);
			mTvFbLikeCount = (TextView) findViewById(R.id.tv_storydetail_fblike_count);
			mTvFbLikeCount.setOnClickListener(this);
		}
	}

	public void setOnClickListener(OnClickListener l) {
		mOnClickListener = l;
	}

	@Override
	public void onClick(View v) {
		GKIMLog.lf(this.getContext(), 0, TAG + "=>onClick: " + v.getId());
		if (mOnClickListener != null) {
			mOnClickListener.onClick(v);
		}
	}

	public void setSavedStory(boolean hasShaved) {
		GKIMLog.l(1, TAG + " setSavedStory:" + hasShaved);
		if (mIBCheck != null) {
			mIBCheck.setSelected(hasShaved);
		}
	}

	public void setCommentCountView(int count) {
		if (count < 0) {
			count = 0;
		}
		String strCount = "%1d";
		if (count >= 1000) {
			count = count / 1000;
			strCount = "+%1dk";
		}
		strCount = String.format(strCount, count);
		if (mTvCommentCount != null) {
			mTvCommentCount.setText(strCount);
		}
	}

	public void setFbLikeCountView(int count) {
		if (count < 0) {
			count = 0;
		}
		String strCount = "%1d";
		if (count >= 1000) {
			count = count / 1000;
			strCount = "+%1dk";
		}
		strCount = String.format(strCount, count);
		if (mTvFbLikeCount != null) {
			mTvFbLikeCount.setText(strCount);
		}
	}

	public void setGoneTextSize() {
		ImageButton ibtn = (ImageButton) findViewById(R.id.imb_storyfooter_textsize);
		if (ibtn != null) {
			ibtn.setVisibility(View.INVISIBLE);
		}
	}

	public void setFbLiked(boolean ischeck) {
		if (mTvFbLikeCount != null) {
			if (ischeck) {
				mTvFbLikeCount.setBackgroundResource(R.drawable.ic_storyfooter_fblike_over);
			} else {
				mTvFbLikeCount.setBackgroundResource(R.drawable.ic_storyfooter_fblike);
			}
		}
	}
}
