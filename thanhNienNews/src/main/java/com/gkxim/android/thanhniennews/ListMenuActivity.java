/**
 * File: ListMenuActivity.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 29-11-2012
 * 
 */
package com.gkxim.android.thanhniennews;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.layout.GUIListMenuFragment;
import com.gkxim.android.utils.GKIMLog;

/**
 *
 */
public class ListMenuActivity extends FragmentActivity {

	protected static final String TAG = "ListMenuActivity";
	private GUIListMenuFragment mMenuFrag = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// if (savedInstanceState == null) {
		// // During initial setup, plug in the details fragment.
		// }
		mMenuFrag = new GUIListMenuFragment();
		mMenuFrag.setArguments(getIntent().getExtras());
		getSupportFragmentManager().beginTransaction()
				.add(android.R.id.content, mMenuFrag).commit();
	}

	public void onClick(View v) {
		// if (mMenuFrag != null) {
		// mMenuFrag.onClick(v);
		// }
		if (mOnClickListener != null) {
			mOnClickListener.onClick(v);
		}
	}

	public void setListMenuOnClickListener(OnClickListener l) {
		if (mMenuFrag != null) {
			mMenuFrag.setOnClickListener(l);
		}
	}

	private OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.menu_list_header_ivhome:
				GKIMLog.lf(ListMenuActivity.this, 0, TAG + "=>go TN's home");
				break;
			case R.id.menu_list_header_ivmyhome:
				GKIMLog.lf(ListMenuActivity.this, 0, TAG + "=>go to myhome");
				break;
			case R.id.menu_list_header_ivstored:
				GKIMLog.lf(ListMenuActivity.this, 0, TAG + "=>go storage");
				break;
			case R.id.menu_list_header_ivsearch:
				GKIMLog.lf(ListMenuActivity.this, 0, TAG + "=>go search.");
				break;
			case R.id.menu_list_footer_name:
				GKIMLog.lf(ListMenuActivity.this, 0, TAG
						+ "=>show account info.");
				break;
			case R.id.menu_list_footer_setting:
				GKIMLog.lf(ListMenuActivity.this, 0, TAG
						+ "=>show setting popup.");
				break;
			default:
				GKIMLog.lf(ListMenuActivity.this, 0, TAG
						+ "=>menu click no action.");
				break;
			}
		}
	};
}
