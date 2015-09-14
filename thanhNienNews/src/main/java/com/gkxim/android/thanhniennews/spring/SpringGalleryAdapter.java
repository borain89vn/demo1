/**
 * @author Nam.Nguyen
 * @Date:Jan 14, 2014
 */
package com.gkxim.android.thanhniennews.spring;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.utils.GKIMLog;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @author Nam.Nguyen
 * 
 */
public class SpringGalleryAdapter extends BaseAdapter {

	private static final String TAG = SpringGalleryAdapter.class
			.getSimpleName();

	private Context mContext;
	private ArrayList<CBoxes> mListData;
	private boolean mTabletVersion;
	private View.OnClickListener mClickListener;
	private int mBoxwidth;
	private int mGapwidth;
	private ImageView icon;

	@SuppressLint("NewApi")
	public SpringGalleryAdapter(Context cxt, ArrayList<CBoxes> lst,
			View.OnClickListener clickListener) {
		this.mContext = cxt;
		this.mListData = lst;
		mTabletVersion = (mContext.getResources().getBoolean(R.bool.istablet));
		mClickListener = clickListener;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListData.size();
	}

	@Override
	public CBoxes getItem(int position) {
		// TODO Auto-generated method stub
		return mListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return getCustomDropDownView(position, convertView, parent);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return getCustomDropDownView(position, convertView, parent);
	}

	public View getCustomDropDownView(final int position, View convertView,
			ViewGroup parent) {
		// TODO Auto-generated method stub
		GKIMLog.l(1, TAG + " getCustomDropDownView:" + position);
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View res = convertView;
		if (res == null) {
			res = inflater.inflate(R.layout.galleryview_item, parent, false);
		}
		CBoxes items = getItem(position);
		if (items != null) {
			if (items.getElements().size() > 0) {
				GKIMLog.l(1, TAG + " items:"
						+ items.getElements().get(0).getContent());
				icon = (ImageView) res.findViewById(R.id.picture);
				icon.setTag(items);
				icon.setLayoutParams(new FrameLayout.LayoutParams(mBoxwidth,
						mBoxwidth));
				ImageLoader.getInstance().displayImage(
						items.getElements().get(0).getContent(), icon);
				icon.setTag(items);
				icon.setOnClickListener(mClickListener);

				ImageView albumIcon = (ImageView) res
						.findViewById(R.id.album_icon);
				if (items.getCount_image() > 1) {
					albumIcon.setVisibility(View.VISIBLE);
				} else {
					albumIcon.setVisibility(View.GONE);
				}
			}

		}
		return res;
	}

	public void setBoxGapWidth(int boxwidth, int gapwidth) {
		mBoxwidth = boxwidth;
		mGapwidth = gapwidth;
	}

}
