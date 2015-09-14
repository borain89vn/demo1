/**
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import java.util.Random;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.models.BoxElement;
import com.gkxim.android.thanhniennews.models.BoxStory;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @author HP
 * 
 */
public class VideoHomeBoxLayout extends FrameLayout {
	private static final String TAG = VideoHomeBoxLayout.class.getSimpleName();
	private static final int ID_SET_VIDEOHOMEBOX_ID = 0x6A000000;

	private TextView mTitle;
	private ImageView mIvImage;
	private ImageView mIvVideoIconSmallLeft;
	private String mSectionRef;
	private String mSectionTitle;
	private BoxStory mBoxStory;
	private LinearLayout mLLTitle;
	private Typeface mTNTypeFace = null;

	public VideoHomeBoxLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initLayout();
	}

	public VideoHomeBoxLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLayout();
	}

	public VideoHomeBoxLayout(Context context) {
		super(context);
		initLayout();
	}

	private void initLayout() {
		// init video home's box
		mTNTypeFace = TNPreferenceManager.getTNTypefaceBOLD();
		if (mTNTypeFace == null) {
			mTNTypeFace = Typeface.DEFAULT_BOLD;
		}
		inflate(this.getContext(), R.layout.boxview_videohome, this);
		this.setAlwaysDrawnWithCacheEnabled(true);
		mTitle = (TextView) this.findViewById(R.id.boxview_title);
		mIvImage = (ImageView) this.findViewById(R.id.boxview_image);
		mIvVideoIconSmallLeft = (ImageView) this
				.findViewById(R.id.boxview_video_icon_small_left);
		mLLTitle = (LinearLayout) findViewById(R.id.boxview_ll_title);

	}

	private int generateBoxIdFromSectionId() {
		// if (!TextUtils.isEmpty(mSectionRef)) {
		// try {
		// return (int) (ID_SET_VIDEOHOMEBOX_ID + Integer
		// .parseInt(mSectionRef));
		// } catch (NumberFormatException e) {
		// GKIMLog.l(
		// 4,
		// TAG
		// + "=>generateBoxIdFromSectionId, NumberFormatException: "
		// + e.getMessage());
		// }
		// }
		return (int) (ID_SET_VIDEOHOMEBOX_ID + ((new Random().nextInt(4000)) + 1));
	}

	/**
	 * @param boxStory
	 */
	public void setBoxStory(BoxStory boxStory) {
		if (boxStory != null) {
			mBoxStory = boxStory;
			mSectionRef = mBoxStory.getSectionRefId();
			mSectionTitle = TNPreferenceManager
					.getSectionTitleFromPref(mSectionRef);
			setId(generateBoxIdFromSectionId());

			mTitle.setTypeface(mTNTypeFace);
			mTitle.setText(mSectionTitle);

			BoxElement[] bes = mBoxStory.getBoxElement();
			if (bes != null && bes.length >= 2) {
				for (BoxElement be : bes) {
					if (be.getType() == BoxElement.BOXELEMENT_TYPE_IMAGE) {
						ImageLoader.getInstance().displayImage(be.getContent(),
								mIvImage);
					} else if (be.getType() == BoxElement.BOXELEMENT_TYPE_TITLE) {
						String title = be.getContent().toUpperCase();
						mTitle.setText(title);
					}
				}
			}

			// set section's background color and Icon
			int version = android.os.Build.VERSION.SDK_INT;
			if (version < 16) {
				mLLTitle.setBackgroundDrawable(TNPreferenceManager
						.getSecMediaBackgroundDrawable(mSectionRef));
			} else {
				mLLTitle.setBackground(TNPreferenceManager
						.getSecMediaBackgroundDrawable(mSectionRef));
			}
			FrameLayout.LayoutParams lp = (LayoutParams) mLLTitle
					.getLayoutParams();
			lp.width = TNPreferenceManager.getCellWidthVideoHome();
			mLLTitle.setLayoutParams(lp);
			mIvVideoIconSmallLeft.setImageDrawable(TNPreferenceManager
					.getSecMediaIcon(mSectionRef));

			if (GKIMLog.DEBUG_ON) {
				TextView tv = (TextView) findViewById(R.id.boxview_video_index);
				if (tv != null) {
					tv.setText(String.valueOf(mBoxStory.getBoxIndex()));
				}
			}
		}
	}

	public String getSectionId() {
		if (mBoxStory != null) {
			return mBoxStory.getSectionRefId();
		}
		return null;
	}

	/**
	 * Get box's height in percent [0.5 - 1.5] from box's data.
	 * 
	 * @return [0.5 - 1.5]
	 */
	public float getPercentBoxHeight() {
		if (mBoxStory != null) {

			switch (mBoxStory.getBoxIndex()) {
			case 1:
				return 1.0f;
			case 2:
				return 1.2f;
			case 3:
				return 0.8f;
			case 4:
				return 0.8f;
			case 5:
				return 1.2f;
			default:
				return 1.0f;
			}
			// int iBh = mBoxStory.getBoxStoryHeight();
			// if (iBh == 0) {
			// iBh = 100;
			// }
			// if (iBh > 2) {
			// return (float) ((iBh * 1.0f) / 100.0f);
			// } else {
			// return (float) (iBh * 1.0f);
			// }
		}
		return 1.0f;
	}

	/**
	 * @return
	 */
	public float getPercentBoxWidth() {
		if (mBoxStory != null) {
			int iBw = mBoxStory.getBoxStoryWidth();
			if (iBw == 0) {
				iBw = 100;
			}
			if (iBw > 2) {
				return (float) ((iBw * 1.0f) / 100.0f);
			} else {
				return (float) (iBw * 1.0f);
			}
		}
		return 1.0f;
	}

	/**
	 * center right alignment for section's title
	 * 
	 * @param i
	 */
	public void setTitleAlign(int gravity) {
		if (mLLTitle != null) {
			FrameLayout.LayoutParams lp = (LayoutParams) mLLTitle
					.getLayoutParams();
			lp.gravity = gravity;
			lp.width = TNPreferenceManager.getCellWidthVideoHome()
					- getResources().getDimensionPixelSize(
							R.dimen.video_home_top_box_align);
			lp.height = getResources().getDimensionPixelSize(
					R.dimen.video_home_top_box_sheight);
			mLLTitle.setLayoutParams(lp);
		}
	}
}
