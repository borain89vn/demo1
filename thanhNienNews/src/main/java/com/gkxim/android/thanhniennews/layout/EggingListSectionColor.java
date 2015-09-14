/**
 * File: EggingListSectionColor.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 21-12-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.models.SectionTemplate;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

/**
 * @author Timon Trinh
 */
public class EggingListSectionColor extends Activity {

	private GridView mGrid;
	private ImageAdapter mAdapter;
	private View mLastSelectedItem = null;
	private OnClickListener mDefaultOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mLastSelectedItem != null) {
				mLastSelectedItem.setSelected(false);
			}
			mLastSelectedItem = v;
			mLastSelectedItem.setSelected(true);
			int id = v.getId();
			if (id == 3) {
				TNPreferenceManager.changeDomain(3);
				// RequestDataFactory.changeDomain(3);
			} else if (id == 2) {
				TNPreferenceManager.changeDomain(1);
				// RequestDataFactory.changeDomain(1);
			} else if (id == 4) {
				TNPreferenceManager.mSectionModeFlip = !TNPreferenceManager.mSectionModeFlip;
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_egging_sectioncolor);
		mGrid = (GridView) findViewById(R.id.grv_egging_sectioncolor);
		mAdapter = new ImageAdapter(this);
		mGrid.setAdapter(mAdapter);

		initContent();
	}

	@SuppressWarnings("deprecation")
	private void initContent() {
		String[] sectionIds = TNPreferenceManager.getSectionIDs();
		if (sectionIds != null && sectionIds.length > 0) {
			TextView tv1 = null;
			TextView tv2 = null;
			HashMap<String, Drawable> hashIconsNormal = TNPreferenceManager
					.getSectionIconsNormal();
			HashMap<String, Drawable> hashIconsOver = TNPreferenceManager
					.getSectionIconsOver();
			GKIMLog.lf(null, 0, "Eggs: " + hashIconsNormal.size() + ", "
					+ hashIconsOver.size());
			for (String id : sectionIds) {
				SectionTemplate sTemplate = TNPreferenceManager
						.getSectionTemplateFromPref(id);
				if (sTemplate != null) {
					tv1 = new TextView(this);
					tv1.setText(sTemplate.getSectionTitle());
					tv1.setTextSize(15);
					tv1.setTextColor(sTemplate.getSectionColor());
					tv1.setHeight(100);
					tv1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
					tv1.setCompoundDrawablesWithIntrinsicBounds(
							hashIconsNormal.get(id), null, null, null);
					tv2 = new TextView(this);
					tv2.setText(id);
					tv2.setTextSize(20);
					tv2.setTextColor((int) sTemplate.getSectionColor());
					tv2.setHeight(100);
					tv2.setGravity(Gravity.CENTER_HORIZONTAL
							| Gravity.CENTER_VERTICAL);
					tv2.setCompoundDrawablesWithIntrinsicBounds(
							hashIconsOver.get(id), null, null, null);

					if (UIUtils.hasJellyBean()) {
						tv1.setBackground(TNPreferenceManager
								.getBackgroundDrawable1(id));
						tv2.setBackground(TNPreferenceManager
								.getBackgroundDrawable2(id));
					} else {
						tv1.setBackgroundDrawable(TNPreferenceManager
								.getBackgroundDrawable1(id));
						tv2.setBackgroundDrawable(TNPreferenceManager
								.getBackgroundDrawable2(id));
					}

				}
				mAdapter.addTextView(tv1);
				mAdapter.addTextView(tv2);
			}
		}
		// xid, imei, uid, devicetype
		Drawable dfd = TNPreferenceManager.getBackgroundDrawable2("");
		TextView tvxid = new TextView(this);
		tvxid.setText(TNPreferenceManager.getXtifyId());
		tvxid.setTextSize(20);
		tvxid.setHeight(100);
		tvxid.setTextColor(Color.WHITE);

		TextView tvimei = new TextView(this);
		tvimei.setText(TNPreferenceManager.getDeviceIMEI());
		tvimei.setTextSize(20);
		tvimei.setHeight(100);
		tvimei.setTextColor(Color.WHITE);

		if (UIUtils.hasJellyBean()) {
			tvxid.setBackground(dfd);
			tvimei.setBackground(dfd);
		} else {
			tvxid.setBackgroundDrawable(dfd);
			tvimei.setBackgroundDrawable(dfd);
		}
		mAdapter.addTextView(tvxid);
		mAdapter.addTextView(tvimei);

		HashMap<Long, Drawable> hashSIcons = TNPreferenceManager
				.getSmileyIcons();
		if (hashSIcons != null && hashSIcons.size() > 0) {
			Set<Long> set = hashSIcons.keySet();
			for (long longId : set) {

				TextView tv = new TextView(this);
				tv.setText(String.valueOf(longId));
				tv.setTextSize(20);
				tv.setHeight(100);
				tv.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
				tv.setCompoundDrawablesWithIntrinsicBounds(
						hashSIcons.get(longId), null, null, null);
				if (UIUtils.hasJellyBean()) {
					tv.setBackground(dfd);
				} else {
					tv.setBackgroundDrawable(dfd);
				}
				mAdapter.addTextView(tv);

			}
		}

		// xid, imei, uid, devicetype
		TextView tvdomain1 = new TextView(this);
		tvdomain1.setText("tnmcms");
		tvdomain1.setTextSize(20);
		tvdomain1.setHeight(100);
		tvdomain1.setTextColor(Color.WHITE);
		tvdomain1.setId(2);
		tvdomain1.setOnClickListener(mDefaultOnClickListener);

		TextView tvdomain2 = new TextView(this);
		tvdomain2.setText("tnmcms1");
		tvdomain2.setTextSize(20);
		tvdomain2.setHeight(100);
		tvdomain2.setTextColor(Color.WHITE);
		tvdomain2.setId(3);
		tvdomain2.setOnClickListener(mDefaultOnClickListener);

		TextView tvChangeFlip = new TextView(this);
		if (TNPreferenceManager.mSectionModeFlip) {
			tvChangeFlip.setText("Add more boxes");
		} else {
			tvChangeFlip.setText("Flip new page.");
		}
		tvChangeFlip.setTextSize(20);
		tvChangeFlip.setHeight(100);
		tvChangeFlip.setTextColor(Color.WHITE);
		tvChangeFlip.setId(4);
		tvChangeFlip.setOnClickListener(mDefaultOnClickListener);

		if (UIUtils.hasJellyBean()) {
			tvdomain1.setBackground(dfd);
			tvdomain2.setBackground(dfd);
			tvChangeFlip.setBackground(dfd);
		} else {
			tvdomain1.setBackgroundDrawable(dfd);
			tvdomain2.setBackgroundDrawable(dfd);
			tvChangeFlip.setBackgroundDrawable(dfd);
		}
		mAdapter.addTextView(tvdomain1);
		mAdapter.addTextView(tvdomain2);
		mAdapter.addTextView(tvChangeFlip);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@TargetApi(16)
	@Override
	protected void onStart() {
		if (mAdapter != null) {

			if (!mAdapter.isEmpty()) {
				mAdapter.notifyDataSetChanged();
			}
		}
		super.onStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onAttachedToWindow()
	 */
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}

	public class ImageAdapter extends BaseAdapter {

		private ArrayList<Bitmap> mBitmaps;
		private ArrayList<Drawable> mDrawables = null;
		private ArrayList<View> mTextViews;

		public ImageAdapter(Context c) {
		}

		public int getCount() {
			// return mThumbIds.length;
			// int size = 0;
			// if (mBitmaps != null) {
			// size += mBitmaps.size();
			// }
			// if (mDrawables != null) {
			// size += mDrawables.size();
			// }
			// return size;
			if (mTextViews != null) {
				return mTextViews.size();
			}
			return 0;
		}

		public Bitmap getItem(int position) {
			if (mBitmaps == null) {
				return BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_launcher);
			}
			return mBitmaps.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// ImageView imageView;
			// if (convertView == null) {
			// imageView = new ImageView(mContext);
			// imageView.setLayoutParams(new GridView.LayoutParams(200, 120));
			// imageView.setAdjustViewBounds(true);
			// imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			// imageView.setPadding(10, 10, 10, 10);
			// } else {
			// if (convertView instanceof TextView) {
			// return convertView;
			// }
			// imageView = (ImageView) convertView;
			// }
			// if (position % 3 == 0 && mTextViews != null
			// && (mTextViews.size() > ((position / 3) + 1))) {
			// GKIMLog.lf(null, 0, "grid, text, " + position +": " +
			// ((TextView)mTextViews.get(position / 3)).getText());
			//
			// } else {
			// if ((position - (position / 3 + 1)) < mDrawables.size()) {
			// Drawable drbl = mDrawables.get(position
			// - (position / 3 + 1));
			// imageView.setImageDrawable(null);
			// imageView.setBackgroundDrawable(drbl);
			// }
			// }
			// return imageView;
			return mTextViews.get(position);
		}

		public void addBitmap(Bitmap bitmap) {
			if (mBitmaps == null) {
				mBitmaps = new ArrayList<Bitmap>();
			}
			mBitmaps.add(bitmap);
		}

		public void addDrawable(Drawable drawable) {
			if (drawable == null) {
				return;
			}
			if (mDrawables == null) {
				mDrawables = new ArrayList<Drawable>();
			}
			mDrawables.add(drawable);
		}

		public void addTextView(View textView) {
			if (textView == null) {
				return;
			}
			if (mTextViews == null) {
				mTextViews = new ArrayList<View>();
			}
			mTextViews.add(textView);
		}

		public void clear() {
			if (mBitmaps != null) {
				mBitmaps.clear();
			}
		}

	}

}
