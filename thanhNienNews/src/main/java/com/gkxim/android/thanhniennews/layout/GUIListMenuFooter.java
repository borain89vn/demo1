/**
 * File: GUIListMenuFooter.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 28-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;

/**
 * A Linear horizontall layout.
 */
public class GUIListMenuFooter extends LinearLayout implements View.OnClickListener {

	private static final String TAG = "GUIListMenuFooter";
	private TextView mTVAccount;
	private ImageView mIVSetting;
	private Drawable[] mPaddingDrawable = null;
	private OnClickListener mOnClickListener = null;
	
	/**
	 * 28-11-2012
	 */
	public GUIListMenuFooter(Context context) {
		super(context);
		initLayout();
	}

	/**
	 * 28-11-2012
	 */
	public GUIListMenuFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLayout();
	}

	/**
	 * 28-11-2012
	 */
	public GUIListMenuFooter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initLayout();
	}

	private void initLayout() {
		GKIMLog.lf(this.getContext(), 0, TAG + "=>initView from: " + getContext().getClass().getSimpleName());
		inflate(getContext(), R.layout.menu_list_footer_layout, this);
		initViewItems();
	}

	private void initViewItems() {
		mTVAccount = (TextView) findViewById(R.id.menu_list_footer_name);
		if (mTVAccount!= null) {
			mTVAccount.setTypeface(TNPreferenceManager.getTNTypeface(), Typeface.BOLD);
			mTVAccount.setOnClickListener(this);
		}
		mIVSetting = (ImageView) findViewById(R.id.menu_list_footer_setting);
		if (mIVSetting!= null) {
			mIVSetting.setOnClickListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see android.view.View#setOnClickListener(android.view.View.OnClickListener)
	 */
	@Override
	public void setOnClickListener(OnClickListener l) {
		mOnClickListener = l;
	}

	public void setAvatar(Drawable bmpAvartar) {
		if (mTVAccount != null) {
			mPaddingDrawable[0] = bmpAvartar;
			mTVAccount.setCompoundDrawables(mPaddingDrawable[0],
					mPaddingDrawable[1], mPaddingDrawable[2],
					mPaddingDrawable[3]);
		}
	}
	
	public void setAccountName(String accountName) {
		if (mTVAccount != null ) {
			mTVAccount.setText(accountName);
		}
	}
	
	public void setAccount(String accountName, Drawable avartar) {
		if (mTVAccount != null) {
			mTVAccount.setText(accountName);
			mPaddingDrawable[0] = avartar;
			mTVAccount.setCompoundDrawables(mPaddingDrawable[0],
					mPaddingDrawable[1], mPaddingDrawable[2],
					mPaddingDrawable[3]);
		}
	}
	
	@Override
	public void onClick(View v) {
		GKIMLog.lf(getContext(), 0, TAG + "=>onClick: " + v.getId());
		if (mOnClickListener!= null) {
			mOnClickListener.onClick(v);
		}
	}
}
