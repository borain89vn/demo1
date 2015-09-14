/**
 * File: GUIHeader.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 26-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

/**
 *
 */
public class GUIHeaderVideo extends LinearLayout implements View.OnClickListener {

	private static final String TAG = "GUIHeaderVideo";
	private static final CharSequence DATEFORMAT_STRING_PORT = "dd.MM.yyyy";
	private static final boolean HAS_SUPPORT_DATE = false;
	private ImageButton mIBMenu;
	private TextView mDate;
	private ImageButton mIBHome;
	private OnClickListener mOnClickListener = null;
	private ImageView mLogo;

	/**
	 * 26-11-2012
	 */
	@SuppressLint("NewApi")
	public GUIHeaderVideo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGUIHeader();
	}

	/**
	 * 26-11-2012
	 */
	public GUIHeaderVideo(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGUIHeader();
	}

	/**
	 * 26-11-2012
	 */
	public GUIHeaderVideo(Context context) {
		super(context);
		initGUIHeader();
	}

	private void initGUIHeader() {
		inflate(this.getContext(), R.layout.bar_header_video, this);
		mIBMenu = (ImageButton) findViewById(R.id.header_ib_menu);
		mIBMenu.setOnClickListener(this);
		mIBHome = (ImageButton) findViewById(R.id.video_header_ib_home);
		mIBHome.setOnClickListener(this);
		mLogo = (ImageView) findViewById(R.id.header_iv_logo);
		mLogo.setOnClickListener(this);
		
		if (HAS_SUPPORT_DATE) {
			mDate = (TextView) findViewById(R.id.header_tv_date);
			mDate.setTypeface(TNPreferenceManager.getTNTypeface());
			if (UIUtils.isTablet(getContext())) {
				mDate.setVisibility(VISIBLE);
				Date d = new Date();
				String theday = "";
				if (UIUtils.isLandscape(getContext())) {
					Locale l = new Locale("en");
					String daybefore = getVNDay((String) (new SimpleDateFormat(
							"EEE", l)).format(d));
					if (daybefore != null && daybefore.length() > 0) {
						theday += daybefore + ", ";
					}
					theday += DateFormat.format(DATEFORMAT_STRING_PORT, d);
				} else {
					theday = (String) DateFormat.format(DATEFORMAT_STRING_PORT,
							new Date());
				}
				mDate.setText(theday);
				mDate.setTypeface(TNPreferenceManager.getTNTypefaceBOLD());
			} else {
				mDate.setVisibility(GONE);
			}
		}
		//NOTE: Comment out after Tet 2014
        if(TNPreferenceManager.SECTION_SPRING) {
            boolean mTabletVersion = UIUtils.isTablet(getContext());
            if (!mTabletVersion) {
                ImageView imgv = (ImageView) findViewById(R.id.imgv_horse_phone);
                imgv.setVisibility(View.VISIBLE);
            } else {
                ImageView imgv1 = (ImageView) findViewById(R.id.imgv_horse_tablet);
                imgv1.setVisibility(View.VISIBLE);
            }
        }
	}

	/**
	 * @param mDate
	 *            The date to set
	 */
	public void setDate(String date) {
		if (HAS_SUPPORT_DATE) {
			if (mDate != null && mDate.getVisibility() == View.VISIBLE) {
				mDate.setText(date);
			} else {
				GKIMLog.lf(this.getContext(), 0, TAG
						+ "=>Date field has not available for this device.");
			}
		}
	}

	/**
	 * @param mDate
	 *            The date in long milisecond.
	 */
	public void setDate(long date) {
		Date d = new Date(date);
		String strDate = DateFormat.format(DATEFORMAT_STRING_PORT, d)
				.toString();
		setDate(strDate);
	}

	@Override
	public void onClick(View v) {
		GKIMLog.lf(getContext(), 0, TAG + "=>cliked: " + v.getId() + "("
				+ v.getClass().getSimpleName() + ").");
		boolean hasProcessed = false;
		switch (v.getId()) {
			case R.id.header_ib_menu:
				hasProcessed = showGUIListMenu();
				break;
			default:
				break;
		}
		if (mOnClickListener != null && !hasProcessed) {
			mOnClickListener.onClick(v);
		}
	}

	private boolean showGUIListMenu() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.View#setOnClickListener(android.view.View.OnClickListener)
	 */
	@Override
	public void setOnClickListener(OnClickListener l) {
		mOnClickListener = l;
	}

	private String getVNDay(String format) {
		String[] arrDays = getResources()
				.getStringArray(R.array.string_vn_days);
		String[] arrEnDays = getResources().getStringArray(
				R.array.string_en_days);
		String inLow = format.toLowerCase();
		if (arrEnDays[2].contains(inLow)) {
			return arrDays[2];
		} else if (arrEnDays[3].contains(inLow)) {
			return arrDays[3];
		} else if (arrEnDays[4].contains(inLow)) {
			return arrDays[4];
		} else if (arrEnDays[5].contains(inLow)) {
			return arrDays[5];
		} else if (arrEnDays[6].contains(inLow)) {
			return arrDays[6];
		} else if (arrEnDays[7].contains(inLow)) {
			return arrDays[7];
		} else if (arrEnDays[8].contains(inLow)) {
			return arrDays[8];
		}
		return "";
	}

	public int getIBMenuHeight(){
		if(mIBMenu != null){
			return mIBMenu.getMeasuredHeight();
		}
		return 0;
	}
}
