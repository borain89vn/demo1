/**
 * File: GUIListMenuFragment.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 27-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.gkim.thanhniennews.R;
import com.gkxim.android.utils.GKIMLog;

/**
 *
 */
public class GUIListMenuFragment extends Fragment {

	protected static final String TAG = "GUIListMenuFragment";
	private GUIListMenuListView mList = null;
	private GUIListMenuAdapter mAdapter = null;

	private OnClickListener mOnClickListener = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GKIMLog.l(0, TAG + "=>onCreate.");
		mAdapter = new GUIListMenuAdapter(getActivity());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// if (container == null) {
		// // Currently in a layout without a container, so no
		// // reason to create our view.
		// return null;
		// }
		GKIMLog.l(0, TAG + "=>onCreateView.");
		View result = inflater
				.inflate(R.layout.menu_list_fragment, null, false);
		;
		mList = (GUIListMenuListView) result.findViewById(R.id.menu_list);
		initListViewItems();
		return result;
	}

	private void initListViewItems() {
		if (mAdapter != null) {
			mList.setAdapter(mAdapter);
		}
//		mAdapter.addDrawable(getResources().getDrawable(
//				R.drawable.ic_menu_list_media));
	}

	// /* (non-Javadoc)
	// * @see android.support.v4.app.Fragment#getView()
	// */
	// @Override
	// public View getView() {
	// if (mList != null) {
	// return mList;
	// }
	// return null;
	// }

	public void onClick(View v) {
		if (mOnClickListener != null) {
			mOnClickListener.onClick(v);
		}
	}

	/**
	 * @param mOnClickListener
	 *            the mOnClickListener to set
	 */
	public void setOnClickListener(OnClickListener l) {
		this.mOnClickListener = l;
	}

}
