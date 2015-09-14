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
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
public class VideoSectionLayout extends TableLayout {

	private static final String TAG = "VideoSectionLayout";

	private Animation mAnimation;
	private int mPaddingSize = 1;
	private String mSectionTitle = null;
	private String mSectionId = null;
	// for handlling boxes into columns.
	private int mMaxColum = 2; // always is 2 for Video's Home layout
	private int mBoxIndex;
	private int mNumberColLastRow;
	private boolean mTabletVersion = false;

	private View.OnClickListener mOnClickListener;
	private View.OnClickListener mOnItemClickListener;

	public class BoxHolder {
		public int boxIndex;
		public int iCol;
		public int iRow;
		public VideoSectionBoxLayout box;

		/**
		 * @return
		 */
		public int getId() {
			if (box != null) {
				return box.getId();
			}
			return 0;
		}
	}

	/**
	 * 09-11-2012
	 */
	public VideoSectionLayout(Context context) {
		super(context);
		initLayout();
	}

	/**
	 * 09-11-2012
	 */
	public VideoSectionLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.BoxLayout);
		// get number of max colum if need
		int maxColumns = a.getInt(
				R.styleable.BoxLayout_maxColumns,
				context.getResources().getInteger(
						R.integer.section_video_page_max_cols));
		a.recycle();
		if (maxColumns > 2) {
			mMaxColum = maxColumns;
		}
		initLayout();
	}

	@TargetApi(11)
	private void initLayout() {
		GKIMLog.lf(this.getContext(), 0, TAG + "=>initBoxLayout: ["
				+ getWidth() + ", " + getHeight() + "].");
		initOnClickListnener();
		TNPreferenceManager.refreshBoxSizeAndGap();
		mTabletVersion = getResources().getBoolean(R.bool.istablet);
		mSectionTitle = "";
		mPaddingSize = TNPreferenceManager.getGapWidth();
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

	public void addBoxPhoneView(VideoSectionBoxLayout boxView) {
		int colSpan = 1;

		if (boxCounts <= 3) {
			mNumberColLastRow = 0;
		} else {
			if (mBoxIndex <= 1) {
				colSpan = 2;
			}
		}
		if (mNumberColLastRow <= 0 || (mNumberColLastRow + colSpan) > mMaxColum) {
			addRow(-1);
			mNumberColLastRow = 0;
		}
		TableRow currentTR = (TableRow) getChildAt(getChildCount() - 1);
		if (currentTR != null) {
			TableRow.LayoutParams lparams = (TableRow.LayoutParams) boxView
					.getLayoutParams();
			if (lparams == null) {
				int w = boxView.getBoxSupposeWidth();
				int h = boxView.getBoxSupposeHeight();
				lparams = new TableRow.LayoutParams(w, h);
			}
			lparams.gravity = Gravity.CENTER_VERTICAL;
			lparams.span = colSpan;
			mNumberColLastRow += colSpan;
			if (mNumberColLastRow <= mMaxColum) {
				lparams.rightMargin = mPaddingSize;
			}
			boxView.setLayoutParams(lparams);
			currentTR.addView(boxView, -1);
			// boxView.setBackgroundColor(R.color.storydetail_text_black);
			if (mBoxIndex == 2 && boxCounts == 3) {
				ImageView mImage = (ImageView) boxView
						.findViewById(R.id.boxview_image);
				FrameLayout.LayoutParams lpImageBox = (FrameLayout.LayoutParams) mImage
						.getLayoutParams();
				int width = lpImageBox.width + lparams.rightMargin;
				int height = lpImageBox.height;
				LinearLayout.LayoutParams lnParam = (LinearLayout.LayoutParams) currentTR
						.getLayoutParams();
				lnParam.width = width;
				lnParam.height = height;
				currentTR.setLayoutParams(lnParam);
				ImageView mIvVideoIconBig = (ImageView) boxView
						.findViewById(R.id.boxview_video_icon_big);
				FrameLayout.LayoutParams lpVideoIcon = (FrameLayout.LayoutParams) mIvVideoIconBig
						.getLayoutParams();
				lpVideoIcon.gravity = Gravity.START;
				lpVideoIcon.leftMargin = width / 2 - lpVideoIcon.width / 2
						- lparams.rightMargin;
			}
			GKIMLog.lf(this.getContext(), 0, TAG + "=>addBoxView added "
					+ " into row: " + (getChildCount() - 1) + " is box: "
					+ boxView.getStoryId() + "[" + boxView.getBoxSupposeWidth()
					+ ", " + boxView.getBoxSupposeHeight() + "].");
		} else {
			GKIMLog.lf(this.getContext(), 5, TAG
					+ "=>addBoxView failed: failed to create a row.");
		}
	}

	private LinearLayout mLinearLayout;
	private boolean isNewRow;
	private boolean isTypeRow1; // include 3 box, 1 box 2x2 and 2 box 1X1

	public void addBoxTabletView(VideoSectionBoxLayout boxView) {
		int boxNumber = mBoxIndex + 1;
		int divide = boxNumber / 5;
		if (divide * 5 + 1 == boxNumber) {
			isNewRow = true;
			isTypeRow1 = true;
		} else if (divide * 5 + 4 == boxNumber) {
			isNewRow = true;
			isTypeRow1 = false;
		} else {
			isNewRow = false;
		}
		if (isNewRow) {
			addRow(-1);
			mLinearLayout = null;
		}
		TableRow currentTR = (TableRow) getChildAt(getChildCount() - 1);
		if (currentTR != null) {
			if (isTypeRow1) {
				if (mLinearLayout == null) {
					LayoutInflater inflater = (LayoutInflater) getContext()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					mLinearLayout = (LinearLayout) inflater.inflate(
							R.layout.video_layout_row_1, null);
					if (mLinearLayout != null) {
						currentTR.addView(mLinearLayout, -1);
					} else {
						UIUtils.showToast(getContext(), TAG
								+ "==> inflat view failed");
						return;
					}
				}
				if (divide * 5 + 1 == boxNumber) {
					LinearLayout linearLayoutFirstItem = (LinearLayout) mLinearLayout
							.findViewById(R.id.first_item);
					if (linearLayoutFirstItem != null) {
						int w = boxView.getBoxSupposeWidth();
						int h = boxView.getBoxSupposeHeight();
						LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
								w, h);
						boxView.setLayoutParams(lParams);
						linearLayoutFirstItem.addView(boxView);
					}
				} else {
					LinearLayout linearLayoutSecondItem = (LinearLayout) mLinearLayout
							.findViewById(R.id.second_item);
					if (linearLayoutSecondItem != null) {
						linearLayoutSecondItem
								.setPadding(mPaddingSize, 0, 0, 0);
						LinearLayout.LayoutParams lnparams = (LinearLayout.LayoutParams) linearLayoutSecondItem
								.getLayoutParams();
						int w = boxView.getBoxSupposeWidth();
						int h = boxView.getBoxSupposeHeight();
						lnparams = new LinearLayout.LayoutParams(w, h);
						if (divide * 5 + 3 == boxNumber) {
							lnparams.topMargin = mPaddingSize;
						}
						boxView.setLayoutParams(lnparams);
						linearLayoutSecondItem.addView(boxView, -1);
					}
				}
			} else {
				if (mLinearLayout == null) {
					mLinearLayout = new LinearLayout(getContext());
					mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
					currentTR.addView(mLinearLayout, -1);
				}
				LinearLayout.LayoutParams lnparams = (LinearLayout.LayoutParams) mLinearLayout
						.getLayoutParams();
				int w = boxView.getBoxSupposeWidth();
				int h = boxView.getBoxSupposeHeight();
				lnparams = new LinearLayout.LayoutParams(w, h);
				lnparams.gravity = Gravity.CENTER_VERTICAL;
				if (divide * 5 == boxNumber) {
					lnparams.leftMargin = mPaddingSize;
				}
				boxView.setLayoutParams(lnparams);
				mLinearLayout.addView(boxView, -1);
			}
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
		result.setPadding(mPaddingSize, mPaddingSize, mPaddingSize, 0);
		if (this.addViewInLayout(result, rowIndex,
				generateDefaultLayoutParams(), true)) {
			GKIMLog.lf(this.getContext(), 5, TAG + "=>added new row at: "
					+ (rowIndex < 0 ? "last" : rowIndex));
			return result;
		}
		GKIMLog.lf(this.getContext(), 5, TAG + "=>added new row falied.");
		return null;
	}

	private TableRow addSpecificRow(int rowIndex) {
		TableRow result = new TableRow(this.getContext());
		result.setPadding(mPaddingSize, mPaddingSize, mPaddingSize, 0);

		LinearLayout.LayoutParams lnParam = new LinearLayout.LayoutParams(400,
				400);

		if (this.addViewInLayout(result, rowIndex, lnParam, true)) {
			GKIMLog.lf(this.getContext(), 5, TAG + "=>added new row at: "
					+ (rowIndex < 0 ? "last" : rowIndex));
			return result;
		}
		GKIMLog.lf(this.getContext(), 5, TAG + "=>added new row falied.");
		return null;
	}

	/**
	 * @Description: Set VSection page content for VideoSectionBoxLayout.
	 *               <p>
	 *               This is the definition time for VideoSectoinBoxLayout which
	 *               will display for a specified Video's section.
	 *               </p>
	 * @param SectionPage
	 *            page
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
		// boxes = new BoxStory[(boxCount - 1) * 3 + 1];
		// System.arraycopy(bt, 0, boxes, 0, boxCount);
		// System.arraycopy(bt, 1, boxes, boxCount, boxCount - 1);
		// System.arraycopy(bt, 1, boxes, boxCount * 2 - 1, boxCount - 1);
		// } else {
		// boxes = new BoxStory[3];
		// boxes[0] = page.getBoxes()[0];
		// boxes[1] = page.getBoxes()[1];
		// boxes[2] = page.getBoxes()[2];
		// }
		boxes = page.getBoxes();
		mSectionId = page.getSectionId();
		mSectionTitle = page.getSectionTitle();
		boxCounts = boxes.length;
		if (boxes == null) {
			return;
		}

		mNumberColLastRow = 0;
		mBoxIndex = 0;
		VideoSectionBoxLayout bV = null;
		for (BoxStory boxStory : boxes) {
			bV = new VideoSectionBoxLayout(this.getContext());
			bV.setOnClickListener(mOnItemClickListener);
			bV.setBoxStory(mBoxIndex, boxStory);
			if (!mTabletVersion) {
				addBoxPhoneView(bV);
			} else {
				addBoxTabletView(bV);
			}
			mBoxIndex += 1;
		}
		long duration = System.currentTimeMillis() - startTime;
		GKIMLog.lf(getContext(), 1, TAG + "=>setPage in: " + duration
				+ " with " + boxes.length + " boxes.");
		if (GKIMLog.DEBUG_ON) {
			UIUtils.showToast(null, "Set page (" + mSectionId + ") in: "
					+ duration + " with " + boxes.length + " boxes.");
		}
	}

	private int boxCounts = 0;

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
					if (v instanceof VideoSectionBoxLayout) {
						VideoSectionBoxLayout bvf = (VideoSectionBoxLayout) v;
						GKIMLog.lf(VideoSectionLayout.this.getContext(), 0, TAG
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
	public void refreshBoxStories(View view, String readStories,
			String idSavedStories) {
		if (readStories == null || readStories.length() == 0 || view == null) {
			return;
		}
		if (view instanceof VideoSectionBoxLayout) {
			VideoSectionBoxLayout bvf = (VideoSectionBoxLayout) view;
			updateBoxStory(readStories, idSavedStories, bvf);
		} else {
			if (view instanceof ViewGroup) {
				GKIMLog.lf(null, 0, TAG + "=>refreshReadStories");
				ViewGroup viewGroup = (ViewGroup) view;
				if (viewGroup != null) {
					int len = viewGroup.getChildCount();
					for (int i = 0; i < len; i++) {
						View view2 = viewGroup.getChildAt(i);
						if (view2 != null) {
							refreshBoxStories(view2, readStories,
									idSavedStories);
						}
					}
				}
			} else {
				return;
			}
		}
	}

	private void updateBoxStory(String readStories, String idSavedStories,
			VideoSectionBoxLayout bvf) {
		String storyid = "";
		int firstReadIndex = -1;
		int firstFavoritedIndex = -1;
		storyid = bvf.getStoryId();
		firstReadIndex = readStories.indexOf(storyid + ",");
		if (firstReadIndex == 0
				|| (firstReadIndex > 0 && readStories.contains("," + storyid
						+ ","))) {
			bvf.setReadBox(true);
			// NOTE: for sure that user can only save a story
			// after
			// read/open it.
			firstFavoritedIndex = idSavedStories.indexOf(storyid + ";");
			if (firstFavoritedIndex == 0
					|| (firstFavoritedIndex > 0 && idSavedStories.contains(";"
							+ storyid + ";"))) {
				bvf.setFavorite(true);
			} else {
				bvf.setFavorite(false);
			}
		}
	}
}
