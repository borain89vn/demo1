/**
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.VideoSectionActivity;
import com.gkxim.android.thanhniennews.models.BoxStory;
import com.gkxim.android.thanhniennews.models.IGenericPage;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

/**
 * @author Timon
 * 
 */
public class VideoHomeLayout extends RelativeLayout {
	private static final String TAG = VideoHomeLayout.class.getSimpleName();

	private int mPaddingSize = 1;
	private String mSectionId;
	private String mSectionTitle;

	private Animation mAnimation;

	// for handlling boxes into columns.
	private int mMaxColumIndex = 2; // always is 2 for Video's Home layout
	private int mLastColumIndex = -1;
	private int mCellWidth;
	private int mCellHeight;
	private VideoHomeBoxLayout[] mPreColumnBox = null;

	private View.OnClickListener mOnClickListener;
	private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// Handling box click here.
			if (v instanceof VideoHomeBoxLayout) {
				if (TNPreferenceManager.BOX_HAS_TOUCH_ANIMATION) {
					v.startAnimation(mAnimation);
				}
				if (mOnClickListener != null) {
					mOnClickListener.onClick(v);
				}
			}
		}
	};

	public VideoHomeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// initLayout();
	}

	public VideoHomeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// initLayout();
	}

	public VideoHomeLayout(Context context) {
		super(context);
		// initLayout();
	}

	/**
	 * 
	 */
	private void initLayout() {
		GKIMLog.lf(this.getContext(), 0, TAG + "=>initBoxLayout: ["
				+ getWidth() + ", " + getHeight() + "].");
		mSectionTitle = "";

		boolean isTabletVersion = getResources().getBoolean(R.bool.istablet);
		if (isTabletVersion) {
			boolean isPotrait = true;
			int ot = getResources().getConfiguration().orientation;
			switch (ot) {
			case Configuration.ORIENTATION_LANDSCAPE:
				isPotrait = false;
				break;
			case Configuration.ORIENTATION_PORTRAIT:
				isPotrait = true;
				break;
			case Configuration.ORIENTATION_SQUARE:
				isPotrait = true;
				break;
			case Configuration.ORIENTATION_UNDEFINED:
				isPotrait = true;
				break;
			default:
				isPotrait = true;
				break;
			}

			if (isPotrait) {
				processPotrait();
			} else {
				processLandcapse();
			}

		} else {
			processPotrait();
		}
	}

	private void processLandcapse() {
		mPaddingSize = TNPreferenceManager.getGapWidth();
		mCellWidth = TNPreferenceManager.getCellWidthVideoHome();
		// int height = width;
		// int heightTxtBottom = (int) getResources().getDimensionPixelSize(
		// R.dimen.video_home_txt_height);
		// int heightAds = (int) getResources().getDimensionPixelSize(
		// R.dimen.video_home_ads_height);
		// VideoSectionActivity videoSectionActivity = (VideoSectionActivity)
		// getContext();
		// if (videoSectionActivity != null) {
		// heightAds += videoSectionActivity.getGuiheaderheight();
		// }
		// int numberRows = 3;
		// mCellHeight = (int) ((height - heightAds - heightTxtBottom -
		// ((numberRows + 1) * mPaddingSize)) / numberRows);
		mCellHeight = (int) (mCellWidth * 0.8);
		mMaxColumIndex = getResources().getInteger(
				R.integer.section_video_home_max_cols);

		if (TNPreferenceManager.BOX_HAS_TOUCH_ANIMATION) {
			mAnimation = AnimationUtils.loadAnimation(getContext(),
					R.anim.boxclick_tween);
			mAnimation.setFillAfter(false);
		}
	}

	private void processPotrait() {
		mCellWidth = TNPreferenceManager.getCellWidthVideoHome();
		mPaddingSize = TNPreferenceManager.getGapWidth();
		int height = getResources().getDisplayMetrics().heightPixels;
		int heightTxtBottom = (int) getResources().getDimensionPixelSize(
				R.dimen.video_home_txt_height);
		int heightAds = (int) getResources().getDimensionPixelSize(
				R.dimen.video_home_ads_height);
		VideoSectionActivity videoSectionActivity = (VideoSectionActivity) getContext();
		if (videoSectionActivity != null) {
			heightAds += videoSectionActivity.getGuiheaderheight();
		}
		int numberRows = 3;
		mCellHeight = (int) ((height - heightAds - heightTxtBottom - ((numberRows + 1) * mPaddingSize)) / numberRows);
		mMaxColumIndex = getResources().getInteger(
				R.integer.section_video_home_max_cols);

		if (TNPreferenceManager.BOX_HAS_TOUCH_ANIMATION) {
			mAnimation = AnimationUtils.loadAnimation(getContext(),
					R.anim.boxclick_tween);
			mAnimation.setFillAfter(false);
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
		if (mOnClickListener != null) {
			mOnClickListener = null;
		}
		mOnClickListener = l;
	}

	/**
	 * @param gp
	 */
	public void setPage(IGenericPage page) {
		long startTime = System.currentTimeMillis();
		if (page == null) {
			GKIMLog.lf(this.getContext(), 4, TAG
					+ "=>setPage: there is no box.");
			return;
		}
		if (getChildCount() > 0) {
			removeAllViewsInLayout();
		}
		BoxStory[] boxes = null;
		// if (GKIMLog.DEBUG_ON) {
		// BoxStory[] bt = page.getBoxes();
		// int boxCount = page.getBoxStoryCount();
		// boxes = new BoxStory[boxCount * 2 - 1];
		// System.arraycopy(bt, 0, boxes, 0, boxCount);
		// System.arraycopy(bt, 1, boxes, boxCount, boxCount - 1);
		// } else {
		boxes = page.getBoxes();
		// }
		mSectionId = page.getSectionId();
		mSectionTitle = page.getSectionTitle();

		if (boxes == null) {
			return;
		}
		TNPreferenceManager.updateFavoriteBoxStory(boxes);
		initLayout();

		VideoHomeBoxLayout bV = null;
		mLastColumIndex = -1;
		if (mPreColumnBox != null) {
			mPreColumnBox = null;
		}
		mPreColumnBox = new VideoHomeBoxLayout[mMaxColumIndex];
		for (int i = 0; i < boxes.length; i++) {
			BoxStory boxStory = boxes[i];
			if (boxStory != null) {
				bV = new VideoHomeBoxLayout(this.getContext());
				// // bV.setMargin(0, 0, mPaddingSide, 0);
				bV.setOnClickListener(mOnItemClickListener);
				boxStory.setBoxIndex(i+1);
				bV.setBoxStory(boxStory);
				if (boxStory.getBoxIndex() == 1) {
					// right centralize the title
					bV.setTitleAlign(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
				}
				addBoxView(bV);
			}
		}
		long duration = System.currentTimeMillis() - startTime;
		GKIMLog.lf(getContext(), 1, TAG + "=>setPage in: " + duration
				+ " with " + boxes.length + " boxes.");
		if (GKIMLog.DEBUG_ON) {
			UIUtils.showToast(null, "Set page (" + mSectionId + ") in: "
					+ duration + " with " + boxes.length + " boxes.");
		}

	}

	/**
	 * Add box view at latest
	 * 
	 * @param bV
	 */
	private void addBoxView(VideoHomeBoxLayout boxView) {

		int iAddingColum = mLastColumIndex + 1;
		boolean bNeedNewRow = false;
		if ((iAddingColum == mMaxColumIndex)) {
			bNeedNewRow = true;
			iAddingColum = 0;
		}
		GKIMLog.l(0, TAG + "=>addBoxView: " + mLastColumIndex + ", "
				+ bNeedNewRow + ", for box: " + boxView.getId());

		// calculate box's height, cell's width is fixed in specified device;
		RelativeLayout.LayoutParams lp = null;
		// remove parent's right if existed
		// lp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);

		if (mLastColumIndex == -1) { // first box of layout
			lp = generateBoxLayoutParams(1, boxView, 0, 0);
			mPreColumnBox[0] = boxView;
			mLastColumIndex = mMaxColumIndex - 1;
		} else {
			VideoHomeBoxLayout topBox = mPreColumnBox[iAddingColum];
			VideoHomeBoxLayout leftBox = null;
			if (!bNeedNewRow) {
				leftBox = mPreColumnBox[mLastColumIndex];
			}
			if (topBox == null) {
				if (leftBox == null) {// case 3
					lp = generateBoxLayoutParams(3, boxView, 0, 0);
				} else { // case 4
					lp = generateBoxLayoutParams(4, boxView, leftBox.getId(), 0);
				}
			} else { // case 5
				lp = generateBoxLayoutParams(5, boxView, 0, topBox.getId());
			}
			mPreColumnBox[iAddingColum] = boxView;
			mLastColumIndex = iAddingColum;
		}

		// add view with layout param
		addView(boxView, lp);

	}

	/**
	 * Generate RelativeLayout's layout param for box in cases:
	 * <p>
	 * <br/>
	 * Case 1 - top box: align parent's left, right and top, width match parent
	 * <br/>
	 * Case 2 - first left: if no top, then same as 3, otherwise 5 Case 3 - no
	 * top, no left: same as 1, but no full <br/>
	 * Case 4 - no top, has left: right_of <b>Left</b>, align_top <b>Left</b> +
	 * gap <br/>
	 * Case 5 - has top: align_left <b>Top</b>, below <b>Top</b> + gap <br/>
	 * </p>
	 * 
	 * @return
	 */
	private LayoutParams generateBoxLayoutParams(int icase,
			VideoHomeBoxLayout box, int idLeft, int idTop) {
		GKIMLog.l(1, TAG + "=>generateBoxLayoutParams case: " + icase);
		LayoutParams result = new RelativeLayout.LayoutParams(mCellWidth,
				(int) (mCellHeight * box.getPercentBoxHeight()));
		switch (icase) {
		// top box: align parent's left, right and top, width match parent
		case 1:
		case 3:
			result.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			result.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			result.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			// margin left, top and right
			result.leftMargin = result.topMargin = result.rightMargin = mPaddingSize;

			result.width = LayoutParams.MATCH_PARENT;
			// lp.width = mMaxColumIndex * mCellWidth + (mMaxColumIndex + 1)
			// * mPaddingSide;
			break;
		case 2:
			if (idTop > 0) {
				result.addRule(ALIGN_LEFT, idTop);
				result.addRule(BELOW, idTop);
			} else {
				result.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				result.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				result.leftMargin = mPaddingSize;
			}
			result.topMargin = mPaddingSize;
			break;
		case 4:
			result.addRule(RIGHT_OF, idLeft);
			result.addRule(ALIGN_TOP, idLeft);
			result.leftMargin = mPaddingSize;
			break;
		case 5:
			result.addRule(ALIGN_LEFT, idTop);
			result.addRule(BELOW, idTop);
			result.topMargin = mPaddingSize;
			break;
		default:
			// to the bottom of the screen
			result.addRule(ALIGN_PARENT_BOTTOM);
			result.addRule(ALIGN_PARENT_RIGHT);
			result.bottomMargin = mPaddingSize;
			break;
		}
		return result;
	}

	/**
	 * @param readStories
	 * @param idSavedStories
	 */
	public void refreshBoxStories(String readStories, String idSavedStories) {
		// TODO Auto-generated method stub

	}
}
