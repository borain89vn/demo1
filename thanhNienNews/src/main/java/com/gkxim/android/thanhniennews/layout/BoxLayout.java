/**
 * File: BoxLayout.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 09-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.models.BoxStory;
import com.gkxim.android.thanhniennews.models.IGenericPage;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

/**
 * @author Timon Trinh A BoxLayout is a GUI object which is stand for a Section
 *         Page.
 */
public class BoxLayout extends TableLayout {

	private static final String TAG = "BoxLayout";
	private static final int DEFAULT_NUMBER_MAX_COLUM = 2;
	private static final boolean DEBUG = GKIMLog.DEBUG_ON;

	// Storing number of colum (included span colum) for last inserted row.
	private int mNumberColLastRow = 0;
	private int mNumberMaxColumn = DEFAULT_NUMBER_MAX_COLUM;
	private int mPaddingSide = Integer.MIN_VALUE;
	private View.OnClickListener mOnItemClickListener;
	private View.OnClickListener mOnClickListener;

	private Animation mAnimation;
	private String mSectionTitle = null;
	private String mSectionId = null;
	 private Drawable mBGDrawble1 = null;
	private Drawable mBGDrawble2 = null;

	/**
	 * 09-11-2012
	 */
	public BoxLayout(Context context) {
		super(context);
	}

	/**
	 * 09-11-2012
	 */
	public BoxLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.BoxLayout);
		// get number of max colum if need
		int maxColumns = a.getInt(R.styleable.BoxLayout_maxColumns,
				DEFAULT_NUMBER_MAX_COLUM);
		if (maxColumns > DEFAULT_NUMBER_MAX_COLUM) {
			mNumberMaxColumn = maxColumns;
		}
		initBoxLayout();
	}

	@TargetApi(11)
	private void initBoxLayout() {
		// NOTE: initialize Boxlayout if need
		// set boxlayout's padding, this should be implied in XML layoyut
		// this.setPadding(leftRightpadding, getPaddingTop(), leftRightpadding,
		// getPaddingBottom());
		GKIMLog.lf(this.getContext(), 0, TAG + "=>initBoxLayout: ["
				+ getWidth() + ", " + getHeight() + "].");
		initOnClickListnener();
		mSectionTitle = "";
		mPaddingSide = TNPreferenceManager.getGapWidth();
		if (mPaddingSide < 0) {
			mPaddingSide = getResources().getInteger(R.integer.middlepadding);
		}
		if (TNPreferenceManager.BOX_HAS_TOUCH_ANIMATION) {
			mAnimation = AnimationUtils.loadAnimation(getContext(),
					R.anim.boxclick_tween);
			mAnimation.setFillAfter(false);
		}
	}

	/**
	 * @return the Section's title
	 */
	public String getSectionTitle() {
		return mSectionTitle;
	}

	/**
	 * @return the Section's id
	 */
	public String getSectionId() {
		return mSectionId;
	}

	public void setNumberMaxColumn(int numberMaxColumn) {
		this.mNumberMaxColumn = numberMaxColumn;
	}

	public void setBackGroundDrawble1(Drawable drawable) {
		 mBGDrawble1 = drawable;
	}

	public void setBackGroundDrawble2(Drawable drawable) {
		mBGDrawble2 = drawable;
	}

	public void addBoxView(View view) {
		BoxViewFrameLayout boxView = (BoxViewFrameLayout) view;
		int colSpan = boxView.getNumberOfColumSpan();
		TableRow currentTR = null;
		if (mNumberColLastRow <= 0
				|| (mNumberColLastRow + colSpan) > mNumberMaxColumn) {
			addRow(-1);
			mNumberColLastRow = 0;
		}
		currentTR = (TableRow) getChildAt(getChildCount() - 1);
		if (currentTR != null) {
			TableRow.LayoutParams lparams = (TableRow.LayoutParams) boxView
					.getLayoutParams();
			if (lparams == null) {
				int w = boxView.getBoxSupposeWidth();
				int h = boxView.getBoxSupposeHeight();
				lparams = new TableRow.LayoutParams(w, h);
			}
			lparams.gravity = Gravity.CENTER_VERTICAL;

			if (colSpan > 1) {
				lparams.span = colSpan;
				GKIMLog.lf(this.getContext(), 0, TAG + "=>addBoxView: span= "
						+ colSpan + ", in colCount= " + mNumberColLastRow);
			}
			mNumberColLastRow += colSpan;
			if (mNumberColLastRow <= mNumberMaxColumn) {
				lparams.rightMargin = mPaddingSide;
			}
			boxView.setLayoutParams(lparams);
			currentTR.addView(boxView, -1);
			GKIMLog.lf(this.getContext(), 0, TAG + "=>addBoxView added "
					+ " into row: " + (getChildCount() - 1) + " is box: "
					+ boxView.getStoryId() + "[" + boxView.getBoxSupposeWidth()
					+ ", " + boxView.getBoxSupposeHeight() + "].");
		} else {
			GKIMLog.lf(this.getContext(), 5, TAG
					+ "=>addBoxView failed: failed to create a row.");
		}
	}

	/**
	 * @Description: Privately add new row into table at index. -1 if the row
	 *               should be added at last.
	 * @param rowIndex
	 * @return The instance of new row if succeed, otherwise null.
	 */
	private TableRow addRow(int rowIndex) {
		TableRow result = new TableRow(this.getContext());
		result.setPadding(mPaddingSide, mPaddingSide, mPaddingSide, 0);
		if (this.addViewInLayout(result, rowIndex,
				generateDefaultLayoutParams(), true)) {
			GKIMLog.lf(this.getContext(), 5, TAG + "=>added new row at: "
					+ (rowIndex < 0 ? "last" : rowIndex));
			return result;
		}
		GKIMLog.lf(this.getContext(), 5, TAG + "=>added new row falied.");
		return null;
	}

	public int getBoxPaddingSide() {
		if (mPaddingSide == Integer.MIN_VALUE) {
			mPaddingSide = TNPreferenceManager.getGapWidth();
			if (mPaddingSide < 0) {
				mPaddingSide = getResources().getInteger(
						R.integer.middlepadding);
			}
		}
		return mPaddingSide;
	}

	/**
	 * @Description: Set Section page content for BoxLayout.
	 *               <p>
	 *               This is the definition time for BoxLayout which will
	 *               display for a specified Section.
	 *               </p>
	 * @param SectionPage
	 *            page
	 */
	public void setPage(IGenericPage object) {
		long startTime = System.currentTimeMillis();
		if (object == null) {
			GKIMLog.lf(this.getContext(), 4, TAG
					+ "=>setPage: there is no box.");
			return;
		}
		if (getChildCount() > 0) {
			removeAllViewsInLayout();
		}
		BoxStory[] boxes = null;
		mSectionId = object.getSectionId();
		mSectionTitle = object.getSectionTitle();
		mNumberColLastRow = 0;
		mNumberMaxColumn = object.getLayoutWidth();
		GKIMLog.l(1, TAG + " mNumberMaxColumn:" + mNumberMaxColumn);
		boxes = object.getBoxes();

		if (mSectionTitle == null || mSectionTitle.length() <= 0) {
			mSectionTitle = TNPreferenceManager
					.getSectionTitleFromPref(mSectionId);
		}
		// Definition for a specified Section
		mBGDrawble1 = TNPreferenceManager.getBackgroundDrawable1(mSectionId);
		mBGDrawble2 = TNPreferenceManager.getBackgroundDrawable2(mSectionId);

		// boolean bLoggedIn = TNPreferenceManager.checkLoggedIn();
		if (boxes == null) {
			return;
		}
		TNPreferenceManager.updateFavoriteBoxStory(boxes);
		BoxViewFrameLayout bV = null;
		for (BoxStory boxStory : boxes) {
			bV = new BoxViewFrameLayout(this.getContext());
			// bV.setMargin(0, 0, mPaddingSide, 0);
			bV.setOnClickListener(mOnItemClickListener);
			if (/*
				 * !bLoggedIn ||
				 */TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
					.equalsIgnoreCase(mSectionId)) {
				boxStory.setFavorite(false);
			}
			
			
			if (TNPreferenceManager.hasReadStory(boxStory.getStoryId())) {
				GKIMLog.l(1,
						TAG + " setPage setReadBox :" + boxStory.getStoryId());
				bV.setReadBox(true);
			}
			bV.setBoxStory(boxStory);
			if (bV.isNeedGardientBackground()) {
				Drawable bgDr = TNPreferenceManager
						.getBackgroundDrawable1(boxStory.getSectionRefId());
				if (!bV.isBGSection()) {
					bgDr = mBGDrawble2;
				}
				bV.setBoxBackground(bgDr);
			}
			addBoxView(bV);
		}
		long duration = System.currentTimeMillis() - startTime;
		GKIMLog.lf(getContext(), 1, TAG + "=>setPage in: " + duration
				+ " with " + boxes.length + " boxes.");
		if (DEBUG) {
			UIUtils.showToast(null, "Set page (" + mSectionId + ") in: "
					+ duration + " with " + boxes.length + " boxes.");
		}
	}

	/**
	 * @Description: Handling for boxView clicked.
	 */
	private void initOnClickListnener() {
		GKIMLog.lf(null, 0, TAG + "=>initOnClickListnener.");
		if (mOnItemClickListener == null) {
			mOnItemClickListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Handling boxview click here.
					if (v instanceof BoxViewFrameLayout) {
						BoxViewFrameLayout bvf = (BoxViewFrameLayout) v;
						GKIMLog.lf(BoxLayout.this.getContext(), 0, TAG
								+ "=>clicked: " + bvf.getBoxIndex() + ", "
								+ bvf.getStoryId());
						if (TNPreferenceManager.BOX_HAS_TOUCH_ANIMATION) {
							v.startAnimation(mAnimation);
						}
						if (mOnClickListener != null) {
							mOnClickListener.onClick(v);
						}
					}
				}
			};
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
	 * @param readStories
	 */
	public void refreshBoxStories(String readStories, String idSavedStories) {
		if (readStories == null || readStories.length() == 0) {
			return;
		}
		GKIMLog.lf(null, 0, TAG + "=>refreshReadStories");
		int len = getChildCount();
		for (int i = 0; i < len; i++) {
			TableRow av = (TableRow) getChildAt(i);
			if (av != null) {
				int lenr = av.getChildCount();
				String storyid = "";
				int firstReadIndex = -1;
				int firstFavoritedIndex = -1;
				for (int j = 0; j < lenr; j++) {
					BoxViewFrameLayout bvf = (BoxViewFrameLayout) av
							.getChildAt(j);
					storyid = bvf.getStoryId();
					firstReadIndex = readStories.indexOf(storyid + ",");
					if (firstReadIndex == 0
							|| (firstReadIndex > 0 && readStories.contains(","
									+ storyid + ","))) {
						bvf.setReadBox(true);
						// NOTE: for sure that user can only save a story after
						// read/open it.
						firstFavoritedIndex = idSavedStories.indexOf(storyid
								+ ";");
						if (firstFavoritedIndex == 0
								|| (firstFavoritedIndex > 0 && idSavedStories
										.contains(";" + storyid + ";"))) {
							bvf.setFavorite(true);
						} else {
							bvf.setFavorite(false);
						}
					}
				}
			}
		}
	}
}
