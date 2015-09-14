/**
 * File: GUIListMenuHeader.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 28-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;

/**
 *
 */
public class GUIListMenuHeader extends LinearLayout implements
		View.OnClickListener {

	private static final String TAG = "GUIListMenuHeader";
	private OnClickListener mOnclickListener = null;
	private Button mIVHome;
	private Button mIVMyHome;
	private Button mIVStored;
	private ImageView mIVSearch;
	private EditText mETSearch;
	private OnClickListener mDefaultOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			GKIMLog.lf(null, 0, TAG + "=>onDefaultClick: " + v.getId());
			if (mOnclickListener != null) {
				mOnclickListener.onClick(v);
			}
		}
	};

	/**
	 * 28-11-2012
	 */
	public GUIListMenuHeader(Context context) {
		super(context);
		initView();
	}

	/**
	 * 28-11-2012
	 */
	public GUIListMenuHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	/**
	 * 28-11-2012
	 */
	public GUIListMenuHeader(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private void initView() {
		GKIMLog.lf(this.getContext(), 0, TAG + "=>initView from: "
				+ getContext().getClass().getSimpleName());
		inflate(getContext(), R.layout.menu_list_header_layout, this);
		setOrientation(VERTICAL);
		initViewItems();
	}

	private void initViewItems() {
		
		Typeface tNTypeFace = TNPreferenceManager.getTNTypefaceBOLD();
		mIVHome = (Button) findViewById(R.id.menu_list_header_ivhome);
		if (mIVHome != null) {
			mIVHome.setOnClickListener(mDefaultOnClickListener);
		}
		mIVMyHome = (Button) findViewById(R.id.menu_list_header_ivmyhome);
		if (mIVMyHome != null) {
			mIVMyHome.setOnClickListener(mDefaultOnClickListener);
		}
		mIVStored = (Button) findViewById(R.id.menu_list_header_ivstored);
		if (mIVStored != null) {
			mIVStored.setOnClickListener(mDefaultOnClickListener);
		}
		mIVHome.setTypeface(tNTypeFace);
		mIVMyHome.setTypeface(tNTypeFace);
		mIVStored.setTypeface(tNTypeFace);
		mIVSearch = (ImageView) findViewById(R.id.menu_list_header_ivsearch);
		if (mIVSearch != null) {
			mIVSearch.setOnClickListener(mDefaultOnClickListener);
		}
		mETSearch = (EditText) findViewById(R.id.et_menu_list_header_search_bar);
		if (mETSearch != null) {
			mETSearch.setTypeface(TNPreferenceManager.getTNTypeface(), Typeface.NORMAL);
			mETSearch.setTextSize(16);
			mETSearch.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (v.equals(mETSearch)) {
						if (hasFocus) {
							mETSearch.setEms(20);
						} else {
							mETSearch.setEms(5);
						}
					}
				}
			});

			mETSearch.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if (v.equals(mETSearch)
							&& EditorInfo.IME_ACTION_SEARCH == actionId) {
						if (mETSearch != null
								&& mETSearch.getText().length() > 0) {
							String strToSearch = mETSearch.getText().toString();
							TNPreferenceManager.setContentToSearch(strToSearch);
							mDefaultOnClickListener.onClick(mIVSearch);
							InputMethodManager imm = (InputMethodManager) getContext()
									.getSystemService(
											Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(getWindowToken(), 0);
						}
					}
					return false;
				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		GKIMLog.lf(this.getContext(), 0, TAG + "=>onClick: " + v.getId());
		if (mOnclickListener != null) {
			mOnclickListener.onClick(v);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.View#setOnClickListener(android.view.View.OnClickListener)
	 */
	@Override
	public void setOnClickListener(OnClickListener l) {
		mOnclickListener = l;
	}

}
