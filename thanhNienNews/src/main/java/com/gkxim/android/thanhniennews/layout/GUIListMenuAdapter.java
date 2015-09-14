/**
 * File: GUIListMenuAdapter.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 28-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;

public class GUIListMenuAdapter extends BaseAdapter {

    private static final String TAG = "GUIListMenuAdapter";

    //static definition for WEB_EVENT item by TNM's campaign: WorldCup 2014
    public static final boolean HAS_ENABLE_WEBEVENT = false;

    private Context mContext = null;
    private ArrayList<DataHolder> mMenuItems = null;
    private int mParentId;
    private Typeface mTNTypeFace = null;

	public class DataHolder {
		public String id;
		public String title;
		int hoverColor;
		Drawable leftIcon;
		int leftPadding;
		ColorStateList colorStateList;
		ViewHolder vh;

		/**
		 * 28-12-2012
		 */
		public DataHolder(String pid, String text, int phoverColor,
				Drawable pleftIcon, int pleftPadding) {
			this.id = pid;
			this.title = text;
			this.hoverColor = phoverColor;
			this.leftIcon = pleftIcon;
			this.leftPadding = pleftPadding;
			this.colorStateList = TNPreferenceManager
					.getSectionColorStateList(pid);
		}
	}

	public class ViewHolder {
        ImageView webevent = null;
		ImageView icon = null;
		TextView title = null;
		public String id = null;
	}

	/**
	 * 28-11-2012
	 */
	public GUIListMenuAdapter(Context context) {
		super();
		mContext = context;
		mTNTypeFace = TNPreferenceManager.getTNTypefaceBOLD();
		if (mTNTypeFace == null) {
			mTNTypeFace = Typeface.DEFAULT_BOLD;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		int size = 0;
		if (mMenuItems != null) {
			size += mMenuItems.size();
		}
		GKIMLog.lf(null, 0, TAG + "=>getCount: " + size);
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		if (mMenuItems != null) {
			return mMenuItems.get(position);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// NOTE: is must be parentId instead of section id for onClick
		// handling.
		return mParentId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DataHolder dh = mMenuItems.get(position);
		
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.menu_list_row, null);
			if (parent != null) {
				mParentId = parent.getId();
			}
			dh.vh = new ViewHolder();
			dh.vh.webevent = (ImageView) convertView
					.findViewById(R.id.menu_row_webevent);
            dh.vh.icon = (ImageView) convertView
					.findViewById(R.id.menu_row_icon);
			dh.vh.title = (TextView) convertView
					.findViewById(R.id.menu_row_title);
//			dh.vh.title.setTextSize(mContext.getResources().getDimension(R.dimen.menu_list_item_text_size));
			dh.vh.title.setTypeface(mTNTypeFace);
			convertView.setTag(dh.vh);
		} else {
			dh.vh = (ViewHolder) convertView.getTag();
		}
		GKIMLog.lf(null, 0, TAG + "=>getView: " + position + ", data: "
				+ dh.title + ", id: " + dh.id);
        setShowWebEvent(dh.vh, HAS_ENABLE_WEBEVENT & (position == 0));
		dh.vh.id = dh.id;
		dh.vh.icon.setBackgroundDrawable(dh.leftIcon);
		if (GKIMLog.DEBUG_ON) {
			dh.vh.title.setText(dh.title + " (" + dh.id + ")");
		} else {
			dh.vh.title.setText(dh.title);
		}
		return convertView;
	}

	public void addMenuSection(String secId, String textTitle,
			int colorTextHover, Drawable sectionIcon, int textLeftPadding) {
		if ((secId == null || secId.length() <= 0)
				|| (textTitle == null || textTitle.length() <= 0)) {
			return;
		}
		if (mMenuItems == null) {
			mMenuItems = new ArrayList<DataHolder>();
		}
		if (sectionIcon == null) {
			sectionIcon = mContext.getResources().getDrawable(
					R.drawable.ic_lauching);
		}
		mMenuItems.add(new DataHolder(secId, textTitle, colorTextHover,
				sectionIcon, textLeftPadding));
	}

	public void clear() {
		if (mMenuItems != null) {
			mMenuItems.clear();
		}
	}

    private void setShowWebEvent(ViewHolder vh, boolean bShow) {
        if (vh != null) {
            try {
                if (bShow) {
                    vh.webevent.setVisibility(View.VISIBLE);
                    vh.icon.setVisibility(View.GONE);
                    vh.title.setVisibility(View.GONE);
                } else {
                    vh.webevent.setVisibility(View.GONE);
                    vh.icon.setVisibility(View.VISIBLE);
                    vh.title.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                //Some view might be null
                GKIMLog.l(4, TAG + "=>setShowWebEvent exception: " + e.getMessage());
            }
        }
    }
}
