/**
 * File: BoxViewFrameLayout.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 21-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import java.util.Random;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.VideoSectionActivity;
import com.gkxim.android.thanhniennews.models.BoxElement;
import com.gkxim.android.thanhniennews.models.BoxStory;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 *
 */
public class VideoSectionBoxLayout extends FrameLayout {

	private static final int ID_SET_VIDEOSECIONBOX_ID = 0x7E000000;
	private static final int DEFINE_BOX_ELEMENT_RULES_STRETCH = 0;
	private static final int DEFINE_BOX_ELEMENT_RULES_LEFT_TOP = 1;
	private static final int DEFINE_BOX_ELEMENT_RULES_RIGHT_TOP = 2;
	private static final int DEFINE_BOX_ELEMENT_RULES_LEFT_BOTTOM = 3;
	private static final int DEFINE_BOX_ELEMENT_RULES_RIGHT_BOTTOM = 4;
	private static final int DEFINE_BOX_ELEMENT_RULES_OVERLAP_TOP = 5;
	private static final int DEFINE_BOX_ELEMENT_RULES_OVERLAP_BOTTOM = 6;
	private static final int DEFINE_BOX_ELEMENT_RULES_OVERLAP_LEFT = 7;
	private static final int DEFINE_BOX_ELEMENT_RULES_OVERLAP_RIGHT = 8;

	private static final String TAG = VideoSectionBoxLayout.class
			.getSimpleName();
	private int mBoxCellSize = -1;
	private int mNumberOfColumn = 1;
	private int mNumberOfRow = 1;

	private TextView mTitle = null;
	private ImageView mImage = null;
	private TextView mShortContent = null;
	private ImageView mIvFavorited = null;
	private ImageView mIvVideoIconBig = null;
	private ImageView mIvVideoIconSmallLeft = null;

	private boolean mNeedBackground = false;
	private boolean mReadBox = false;
	private int mMultiPaddingNum = 1;
	private Drawable mForegroundDrawable;
	private Rect mCachedBounds = new Rect();

	private int mStoryBoxWidth;
	private int mStoryBoxHeight;
	private int mGapWidth;
	private boolean mFavorited = false;
	private boolean mVideoIcon = false;
	private LinearLayout mLLTitle;
	private BoxStory mBoxStory;
	private int mBoxLayoutIndex;

	// private ColorFilter mColorFilter = null;

	/**
	 * 21-11-2012
	 */
	public VideoSectionBoxLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initBoxView();
	}

	/**
	 * 21-11-2012
	 */
	public VideoSectionBoxLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initBoxView();
	}

	public BoxStory getBoxStory() {
		return mBoxStory;
	}

	/**
	 * 21-11-2012
	 */
	public VideoSectionBoxLayout(Context context) {
		super(context);
		initBoxView();
	}

	@SuppressLint("InlinedApi")
	private void initBoxView() {
		mTabletVersion = getResources().getBoolean(R.bool.istablet);
		mGapWidth = TNPreferenceManager.getGapWidth();
		if (mGapWidth < 0) {
			mGapWidth = getResources().getInteger(R.integer.middlepadding);
		}
		mBoxCellSize = TNPreferenceManager.getCellWidthVideoHome();
		if (mBoxCellSize < 0) {
			mBoxCellSize = this.getContext().getResources()
					.getInteger(R.integer.boxcellsize);
		}
		// Retrieve the drawable resource assigned to the
		// android.R.attr.selectableItemBackground
		// theme attribute from the current theme.
		if (UIUtils.hasHoneycomb()) {
			TypedArray a = getContext().obtainStyledAttributes(
					new int[] { android.R.attr.selectableItemBackground });
			mForegroundDrawable = a.getDrawable(0);
			a.recycle();
		} else {
			mForegroundDrawable = new ColorDrawable(Color.CYAN);
		}
		inflateBoxView();
	}

	public int getGapWidth() {
		return mGapWidth;
	}

	private void inflateBoxView() {
		inflate(this.getContext(), R.layout.boxview_video, this);
		this.setAlwaysDrawnWithCacheEnabled(true);
		mLLTitle = (LinearLayout) this
				.findViewById(R.id.boxview_video_ll_title);
		mTitle = (TextView) this.findViewById(R.id.boxview_title);
		mIvFavorited = (ImageView) this.findViewById(R.id.boxview_favorited);
		mIvVideoIconBig = (ImageView) this
				.findViewById(R.id.boxview_video_icon_big);
		mIvVideoIconBig.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBoxStory != null && mBoxStory.getStoryId() != null
						&& mBoxStory.getStoryId().length() > 0) {
					VideoSectionActivity videoSectionActivity = (VideoSectionActivity) getContext();
					if (videoSectionActivity != null) {
						videoSectionActivity.loadStory(mBoxStory.getStoryId());
					}
				} else {
					UIUtils.showToast(getContext(), "have no story id");
				}
			}
		});
		mIvVideoIconSmallLeft = (ImageView) this
				.findViewById(R.id.boxview_video_icon_small_left);
	}

	public int getNumberOfColumSpan() {
		return mNumberOfColumn;
	}

	public int getNumberOfRowSpan() {
		return mNumberOfRow;
	}

	public int getBoxSupposeWidth() {
		if (!mTabletVersion) {
			return (mBoxCellSize * mNumberOfColumn + mMultiPaddingNum
					* (mNumberOfColumn - 1) * mGapWidth);
		} else {
			return (mBoxCellSize * mNumberOfColumn);
		}
	}

	public int getBoxSupposeHeight() {
		if (!mTabletVersion) {
			return (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
					* (mNumberOfRow - 1) * getPaddingTop());
		} else {
			return (mBoxCellSize * mNumberOfRow + (mNumberOfRow - 1)
					* mGapWidth + mMultiPaddingNum * (mNumberOfRow - 1)
					* getPaddingTop());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.FrameLayout#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// setMeasuredDimension(getBoxSupposeWidth(), getBoxSupposeHeight());
	}

	public boolean isNeedGardientBackground() {
		return mNeedBackground;
	}

	public boolean isBGSection() {
		BoxStory tag = (BoxStory) getTag();
		if (tag != null) {
			return tag.isBGOnSection();
		}
		return false;
	}

	public String getBoxIndex() {
		BoxStory tag = (BoxStory) getTag();
		if (tag != null) {
			return String.valueOf(tag.getBoxIndex());
		}
		return "";
	}

	public String getStoryId() {
		BoxStory tag = (BoxStory) getTag();
		if (tag != null) {
			return tag.getStoryId();
		}
		return "";
	}

	public boolean getStoryChecked() {
		BoxStory tag = (BoxStory) getTag();
		if (tag != null) {
			return tag.isFavourited();
		}
		return false;
	}

	public String getSectionRefId() {
		BoxStory tag = (BoxStory) getTag();
		if (tag != null) {
			return tag.getSectionRefId();
		}
		return "";
	}

	public void setBoxStory(int iLayoutIndex, BoxStory boxStory) {
		mBoxLayoutIndex = iLayoutIndex;
		mBoxStory = boxStory;
		if (mTabletVersion) {
			setBoxTabletStory(boxStory);
		} else {
			setBoxStory(boxStory);
		}
	}

	private boolean mTabletVersion = false;

	private void setBoxTabletStory(BoxStory boxStory) {
		GKIMLog.lf(this.getContext(), 0, TAG + "=>setBoxStory.");
		if (boxStory != null) {
			int width = getResources().getDisplayMetrics().widthPixels;
			mBoxCellSize = (width - (2 + 1) * mGapWidth) / 3;
			int boxNumber = mBoxLayoutIndex + 1;
			int divide = boxNumber / 5;
			if (divide * 5 + 1 == boxNumber) {
				mNumberOfColumn = 2;
				mNumberOfRow = 2;
			} else if (divide * 5 == boxNumber) {
				mNumberOfColumn = 2;
				mNumberOfRow = 1;
			} else {
				mNumberOfColumn = 1;
				mNumberOfRow = 1;
			}
			int storyLayout = boxStory.getLayout();
			GKIMLog.l(1, TAG + " setBoxStory :" + boxStory.getStoryId());
			boolean hasImage = false;
			boolean hasTitle = false;
			boolean hasShortCont = false;
			mFavorited = boxStory.isFavourited();
			mVideoIcon = boxStory.getVideo() > 0 ? true : false;

			if (boxStory.hasElementType(BoxElement.BOXELEMENT_TYPE_IMAGE)) {
				mImage = (ImageView) this.findViewById(R.id.boxview_image);
				if (mImage != null) {
					BoxElement[] imageBoxes = boxStory
							.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_IMAGE);
					if (imageBoxes != null) {
						BoxElement elm = imageBoxes[0];
						// NOTE: start loading from ImageFetcher
						// use only 1 time when setBoxStory.
						ImageLoader.getInstance().displayImage(
								elm.getContent(), mImage);
						elm.setWidthCell(1);
						applyTabletRules(mImage, elm);
						mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
					}
					hasImage = true;
				} else {
					GKIMLog.lf(this.getContext(), 4, TAG
							+ "=>setBoxStory failed on find ImageView.");
				}
			} else {
				mNeedBackground = true;
			}
			if (boxStory
					.hasElementType(BoxElement.BOXELEMENT_TYPE_SHORTCONTENT)) {
				mShortContent = (TextView) this
						.findViewById(R.id.boxview_shortcontent);
				if (mShortContent != null) {
					BoxElement[] shortTextElms = boxStory
							.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_SHORTCONTENT);
					if (shortTextElms != null) {
						BoxElement elm = shortTextElms[0];
						mShortContent.setTextColor(elm.getTextColor());
						// Note: tweak text size on xmls
						// mShortContent.setTextSize(elm.getTextSize());
						mShortContent.setTextSize(getResources()
								.getDimensionPixelSize(
										R.dimen.section_box_title_textsize));
						mShortContent.setTypeface(UIUtils.getDefaultTypeFace(
								this.getContext().getApplicationContext(), 0),
								elm.getTextType());
						mShortContent.setText(elm.getContent());
						elm.setWidthCell(1);
						applyTabletRules(mShortContent, elm);
						hasShortCont = true;
					}
				} else {
					GKIMLog.lf(this.getContext(), 4, TAG
							+ "=>setBoxStory failed on find short TextView.");
				}
			}
			if (boxStory.hasElementType(BoxElement.BOXELEMENT_TYPE_TITLE)) {
				if (mTitle != null) {
					BoxElement[] titleElms = boxStory
							.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_TITLE);
					if (titleElms != null) {
						BoxElement elm = titleElms[0];
						mTitle.setTextSize(getResources()
								.getDimensionPixelSize(
										R.dimen.section_boxvideo_title_textsize));
						mTitle.setTypeface(UIUtils.getDefaultTypeFace(this
								.getContext().getApplicationContext(), 0), elm
								.getTextType());
						// Fix: don't change color text when user read
						if (mReadBox) {
							mTitle.setTextColor(getResources().getColor(
									R.color.box_hasread_textcolor));
						} else {
							mTitle.setTextColor(elm.getTextColor());
						}

						if (divide * 5 == boxNumber) {
							elm.setAlignmentInBox(8);
						} else {
							elm.setAlignmentInBox(6);
						}
						elm.setWidthCell(1);
						applyTabletRules(mLLTitle, elm);
						String title = elm.getContent();
						mTitle.setText(title);

						hasTitle = true;

						float textSize = TNPreferenceManager
								.getTextSizeBoxView();
						if (textSize > 0) {
							mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
									textSize);
						}
						adjustTextLength(mTitle);
						updatelayoutIconVideo();
					}
				} else {
					GKIMLog.lf(this.getContext(), 4, TAG
							+ "=>setBoxStory failed on find title TextView.");
				}
			}

			mLLTitle.setBackgroundColor(boxStory.getBackground1Color());
			this.setBackgroundColor(boxStory.getBackground1Color());
			// finally update the boxView's size
			GKIMLog.lf(this.getContext(), 5, TAG + "=>setBoxStory, type: "
					+ storyLayout + ", row: " + mNumberOfRow + ", col: "
					+ mNumberOfColumn + ", with "
					+ (hasImage ? "imgage, " : "")
					+ (hasTitle ? "title, " : "")
					+ (hasShortCont ? "short content." : ""));
			setId(generateBoxIdFromSectionId(boxStory));
			this.setTag(boxStory);
		}
	}

	private void setBoxStory(BoxStory boxStory) {
		GKIMLog.lf(this.getContext(), 0, TAG + "=>setBoxStory.");
		if (boxStory != null) {
			int storyLayout = boxStory.getLayout();
			mNumberOfColumn = (mBoxLayoutIndex > 1) ? 1 : getResources()
					.getInteger(R.integer.section_video_page_max_cols);
			mNumberOfRow = (mBoxLayoutIndex > 0) ? 1 : 2;
			// mStoryBoxWidth = boxStory.getBoxStoryWidth();
			// mStoryBoxHeight = boxStory.getBoxStoryHeight();
			GKIMLog.l(1, TAG + " setBoxStory :" + boxStory.getStoryId());
			boolean hasImage = false;
			boolean hasTitle = false;
			boolean hasShortCont = false;
			mFavorited = boxStory.isFavourited();
			mVideoIcon = boxStory.getVideo() > 0 ? true : false;

			if (boxStory.hasElementType(BoxElement.BOXELEMENT_TYPE_IMAGE)) {
				mImage = (ImageView) this.findViewById(R.id.boxview_image);
				if (mImage != null) {
					BoxElement[] imageBoxes = boxStory
							.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_IMAGE);
					if (imageBoxes != null) {
						BoxElement elm = imageBoxes[0];
						// NOTE: start loading from ImageFetcher
						// use only 1 time when setBoxStory.
						ImageLoader.getInstance().displayImage(
								elm.getContent(), mImage);
						if (mBoxLayoutIndex == 0) {
							elm.setAlignmentInBox(7);
							elm.setWidthCell(2);
						} else if (mBoxLayoutIndex == 1) {
							elm.setAlignmentInBox(-1);
							elm.setWidthCell(1);
						} else {
							elm.setAlignmentInBox(7);
							elm.setWidthCell(1);
						}
						applyRules(mImage, elm);
						mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
					}
					hasImage = true;
				} else {
					GKIMLog.lf(this.getContext(), 4, TAG
							+ "=>setBoxStory failed on find ImageView.");
				}
			} else {
				mNeedBackground = true;
				if (mBoxLayoutIndex == 1) {
					mNeedBackground = false;
					mImage = (ImageView) this.findViewById(R.id.boxview_image);
					mImage.setImageResource(R.drawable.ic_launcher);
					if (mImage != null) {
						BoxElement elm = new BoxElement();
						elm.setAlignmentInBox(-1);
						elm.setWidthCell(1);
						applyRules(mImage, elm);
						hasImage = true;
						mImage.setVisibility(INVISIBLE);
					}
				}
			}
			if (boxStory
					.hasElementType(BoxElement.BOXELEMENT_TYPE_SHORTCONTENT)) {
				mShortContent = (TextView) this
						.findViewById(R.id.boxview_shortcontent);
				if (mShortContent != null) {
					BoxElement[] shortTextElms = boxStory
							.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_SHORTCONTENT);
					if (shortTextElms != null) {
						BoxElement elm = shortTextElms[0];
						mShortContent.setTextColor(elm.getTextColor());
						// Note: tweak text size on xmls
						// mShortContent.setTextSize(elm.getTextSize());
						mShortContent.setTextSize(getResources()
								.getDimensionPixelSize(
										R.dimen.section_box_title_textsize));
						mShortContent.setTypeface(UIUtils.getDefaultTypeFace(
								this.getContext().getApplicationContext(), 0),
								elm.getTextType());
						mShortContent.setText(elm.getContent());

						applyRules(mShortContent, elm);
						hasShortCont = true;
					}
				} else {
					GKIMLog.lf(this.getContext(), 4, TAG
							+ "=>setBoxStory failed on find short TextView.");
				}
			}
			if (boxStory.hasElementType(BoxElement.BOXELEMENT_TYPE_TITLE)) {
				if (mTitle != null) {
					BoxElement[] titleElms = boxStory
							.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_TITLE);
					if (titleElms != null) {
						BoxElement elm = titleElms[0];
						mTitle.setTextSize(getResources()
								.getDimensionPixelSize(
										R.dimen.section_boxvideo_title_textsize));
						mTitle.setTypeface(UIUtils.getDefaultTypeFace(this
								.getContext().getApplicationContext(), 0), elm
								.getTextType());
						// Fix: don't change color text when user read
						if (mReadBox) {
							mTitle.setTextColor(getResources().getColor(
									R.color.box_hasread_textcolor));
						} else {
							mTitle.setTextColor(elm.getTextColor());
						}
						if (mBoxLayoutIndex == 1) {
							elm.setWidthCell(1);
							elm.setAlignmentInBox(8);
						} else {
							elm.setAlignmentInBox(6);
						}
						applyRules(mLLTitle, elm);
						String title = elm.getContent();
						mTitle.setText(title);
						hasTitle = true;

						float textSize = TNPreferenceManager
								.getTextSizeBoxView();
						if (mBoxLayoutIndex == 0) {
							if (textSize > 0) {
								mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
										textSize * 1.25f);
							}
						} else if (mBoxLayoutIndex == 1) {
							if (textSize > 0) {
								mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
										textSize * 1.125f);
							}
						} else {
							if (textSize > 0) {
								mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
										textSize);
							}
						}

						// - getResources()
						// .getInteger(
						// R.integer.extract_txt_size_height

						adjustTextLength(mTitle);
						updatelayoutIconVideo();
					}
				} else {
					GKIMLog.lf(this.getContext(), 4, TAG
							+ "=>setBoxStory failed on find title TextView.");
				}
			}

			mLLTitle.setBackgroundColor(boxStory.getBackground1Color());
			this.setBackgroundColor(boxStory.getBackground1Color());
			// finally update the boxView's size
			GKIMLog.lf(this.getContext(), 5, TAG + "=>setBoxStory, type: "
					+ storyLayout + ", row: " + mNumberOfRow + ", col: "
					+ mNumberOfColumn + ", with "
					+ (hasImage ? "imgage, " : "")
					+ (hasTitle ? "title, " : "")
					+ (hasShortCont ? "short content." : ""));
			setId(generateBoxIdFromSectionId(boxStory));
			this.setTag(boxStory);
		}
	}

	private void updatelayoutIconVideo() {
		// update for video ion big
		LayoutParams lpImageBox;
		if (mImage != null) {
			lpImageBox = (LayoutParams) mImage.getLayoutParams();
		} else {
			lpImageBox = (LayoutParams) mLLTitle.getLayoutParams();
		}

		int width = lpImageBox.width;
		int height = lpImageBox.height;

		FrameLayout.LayoutParams lpLLTitle = (LayoutParams) mLLTitle
				.getLayoutParams();

		int heightPortion = height - lpLLTitle.height;

		if (heightPortion == 0) {
			height = getBoxSupposeHeight();
			heightPortion = height - lpLLTitle.height;
		}

		FrameLayout.LayoutParams lpVideoIcon = (LayoutParams) mIvVideoIconBig
				.getLayoutParams();

		if (lpLLTitle.height > height / 2) {
			lpVideoIcon.gravity = Gravity.CENTER_VERTICAL;
			lpVideoIcon.leftMargin = width / 2 - lpVideoIcon.width / 2;
		}

		if (Math.abs(heightPortion) > height / 3) {
			lpVideoIcon.gravity = Gravity.CENTER_HORIZONTAL;
			lpVideoIcon.topMargin = heightPortion / 2 - lpVideoIcon.height / 2;
		}
		mIvVideoIconBig.setLayoutParams(lpVideoIcon);
		mIvVideoIconBig.setVisibility(VISIBLE);
	}

	@SuppressLint("InlinedApi")
	private void applyTabletRules(View view, BoxElement elm) {
		int alignmentInBox = elm.getAlignmentInBox();
		int elmW = elm.getWidthCell();
		boolean bOverlapTB = false;
		boolean bOverlapLR = false;
		FrameLayout.LayoutParams lp = (LayoutParams) view.getLayoutParams();

		switch (alignmentInBox) {
		case DEFINE_BOX_ELEMENT_RULES_RIGHT_TOP:
			if (UIUtils.hasICS()) {
				lp.gravity = Gravity.END;
			} else {
				lp.gravity = Gravity.RIGHT | Gravity.TOP;
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_LEFT_BOTTOM:
			if (UIUtils.hasICS()) {
				lp.gravity = Gravity.START;
			} else {
				lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_RIGHT_BOTTOM:
			if (UIUtils.hasICS()) {
				lp.gravity = Gravity.END;
			} else {
				lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_OVERLAP_TOP:
			bOverlapTB = true;
			lp.gravity = Gravity.TOP;
			break;
		case DEFINE_BOX_ELEMENT_RULES_OVERLAP_BOTTOM:
			bOverlapTB = true;
			lp.gravity = Gravity.BOTTOM;
			if (view instanceof TextView) {
				((TextView) view).setGravity(Gravity.BOTTOM);
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_OVERLAP_LEFT:
			bOverlapLR = true;
			if (UIUtils.hasICS()) {
				lp.gravity = Gravity.START;
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_OVERLAP_RIGHT:
			bOverlapLR = true;
			if (UIUtils.hasICS()) {
				lp.gravity = Gravity.END;
			} else {
				lp.gravity = Gravity.RIGHT | Gravity.TOP;
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_STRETCH:
			lp.width = getBoxSupposeWidth();
			lp.height = getBoxSupposeHeight();
			break;
		case DEFINE_BOX_ELEMENT_RULES_LEFT_TOP:
		default:
			lp.gravity = Gravity.LEFT | Gravity.TOP;
			break;
		}

		if (bOverlapTB) {
			lp.width = mBoxCellSize * mNumberOfColumn + mMultiPaddingNum
					* (mNumberOfColumn - 1);
			int boxNumber = mBoxLayoutIndex + 1;
			int divide = boxNumber / 5;
			if (divide * 5 + 1 == boxNumber) {
				lp.height = (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
						* (mNumberOfRow - 1) * getPaddingTop()) / 4;
			} else {
				lp.height = (int) ((2 * (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
						* (mNumberOfRow - 1) * getPaddingTop())) / 5.4f);
			}

		} else if (bOverlapLR) {
			lp.width = mBoxCellSize * elmW + mMultiPaddingNum * (elmW - 1);
			lp.height = (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
					* (mNumberOfRow - 1) * getPaddingTop());
		} else if (alignmentInBox != DEFINE_BOX_ELEMENT_RULES_STRETCH) {
			lp.width = mBoxCellSize * elmW + mMultiPaddingNum * (elmW - 1);
			lp.height = mBoxCellSize;
		}

		if ((view instanceof ImageView)
				&& alignmentInBox != DEFINE_BOX_ELEMENT_RULES_STRETCH) {
			((ImageView) view).setScaleType(ScaleType.FIT_XY);
			if (mNumberOfColumn > 1) {
				mNeedBackground = true;
			}
			// NOTE: image could be able to "eat" the padding in box.
			int boxNumber = mBoxLayoutIndex + 1;
			int divide = boxNumber / 5;
			if (divide * 5 == boxNumber) {
				lp.width = mBoxCellSize * elmW + mMultiPaddingNum * (elmW);
			} else {
				lp.width = mBoxCellSize * mNumberOfColumn * elmW
						+ mMultiPaddingNum * (elmW);
			}

		} else if (view instanceof TextView) {
			TextView tv = (TextView) view;
			if (TNPreferenceManager.isIgnoreTextGravity()) {
				tv.setGravity(Gravity.LEFT | Gravity.TOP);
			} else {
				tv.setGravity(lp.gravity);
			}
			// mWidthText = lp.width;
			// mHeightText = lp.height;
			tv.setPadding(mGapWidth, mGapWidth / 2, mGapWidth, mGapWidth);

		}

		view.setLayoutParams(lp);
		view.setVisibility(VISIBLE);
	}

	@SuppressLint("InlinedApi")
	private void applyRules(View view, BoxElement elm) {
		int alignmentInBox = elm.getAlignmentInBox();
		int elmW = elm.getWidthCell();
		boolean bOverlapTB = false;
		boolean bOverlapLR = false;
		FrameLayout.LayoutParams lp = (LayoutParams) view.getLayoutParams();

		switch (alignmentInBox) {
		case DEFINE_BOX_ELEMENT_RULES_RIGHT_TOP:
			if (UIUtils.hasICS()) {
				lp.gravity = Gravity.END;
			} else {
				lp.gravity = Gravity.RIGHT | Gravity.TOP;
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_LEFT_BOTTOM:
			if (UIUtils.hasICS()) {
				lp.gravity = Gravity.START;
			} else {
				lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_RIGHT_BOTTOM:
			if (UIUtils.hasICS()) {
				lp.gravity = Gravity.END;
			} else {
				lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_OVERLAP_TOP:
			bOverlapTB = true;
			lp.gravity = Gravity.TOP;
			break;
		case DEFINE_BOX_ELEMENT_RULES_OVERLAP_BOTTOM:
			bOverlapTB = true;
			lp.gravity = Gravity.BOTTOM;
			if (view instanceof TextView) {
				((TextView) view).setGravity(Gravity.BOTTOM);
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_OVERLAP_LEFT:
			bOverlapLR = true;
			if (UIUtils.hasICS()) {
				lp.gravity = Gravity.START;
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_OVERLAP_RIGHT:
			bOverlapLR = true;
			if (UIUtils.hasICS()) {
				lp.gravity = Gravity.END;
			} else {
				lp.gravity = Gravity.RIGHT | Gravity.TOP;
			}
			break;
		case DEFINE_BOX_ELEMENT_RULES_STRETCH:
			lp.width = getBoxSupposeWidth();
			lp.height = getBoxSupposeHeight();
			break;
		case DEFINE_BOX_ELEMENT_RULES_LEFT_TOP:
		default:
			lp.gravity = Gravity.LEFT | Gravity.TOP;
			break;
		}

		if (bOverlapTB) {
			lp.width = mBoxCellSize * mNumberOfColumn + mMultiPaddingNum
					* (mNumberOfColumn - 1) * mGapWidth;
			if (mBoxLayoutIndex == 0) {
				lp.height = (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
						* (mNumberOfRow - 1) * getPaddingTop()) / 4;
			} else {
				lp.height = (int) ((2 * (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
						* (mNumberOfRow - 1) * getPaddingTop())) / 5.5f);
			}
			// lp.height = (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
			// * (mNumberOfRow - 1) * getPaddingTop()) / 3;
		} else if (bOverlapLR) {
			lp.width = mBoxCellSize * elmW + mMultiPaddingNum * (elmW - 1)
					* mGapWidth;
			lp.height = (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
					* (mNumberOfRow - 1) * getPaddingTop());
		} else if (alignmentInBox != DEFINE_BOX_ELEMENT_RULES_STRETCH) {
			lp.width = mBoxCellSize * elmW + mMultiPaddingNum * (elmW - 1)
					* mGapWidth;
			lp.height = mBoxCellSize;
		}

		if ((view instanceof ImageView)
				&& alignmentInBox != DEFINE_BOX_ELEMENT_RULES_STRETCH) {
			((ImageView) view).setScaleType(ScaleType.FIT_XY);
			if (mNumberOfColumn > 1) {
				mNeedBackground = true;
			}
			// NOTE: image could be able to "eat" the padding in box.
			lp.width = mBoxCellSize * elmW + mMultiPaddingNum * (elmW)
					* mGapWidth;
		} else if (view instanceof TextView) {
			TextView tv = (TextView) view;
			if (TNPreferenceManager.isIgnoreTextGravity()) {
				tv.setGravity(Gravity.LEFT | Gravity.TOP);
			} else {
				tv.setGravity(lp.gravity);
			}
			// mWidthText = lp.width;
			// mHeightText = lp.height;
			tv.setPadding(mGapWidth, mGapWidth / 2, mGapWidth, mGapWidth);

		}

		view.setLayoutParams(lp);
		view.setVisibility(VISIBLE);
	}

	@TargetApi(16)
	@SuppressWarnings("deprecation")
	public void setBoxBackground(Drawable background) {
		if (background == null) {
			background = TNPreferenceManager
					.getBackgroundDrawable1(getSectionRefId());
		}
		if (UIUtils.hasJellyBean()) {
			setBackground(background);
		} else {
			setBackgroundDrawable(background);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.FrameLayout#drawableStateChanged()
	 */
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mForegroundDrawable.isStateful()) {
			mForegroundDrawable.setState(getDrawableState());
		}
		// Trigger a redraw.
		invalidate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.FrameLayout#onSizeChanged(int, int, int, int)
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mCachedBounds.set(0, 0, w, h);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		int w = getMeasuredWidth();
		int h = getMeasuredHeight();
		GKIMLog.lf(null, 0, TAG + "=>onDraw(" + this.getBoxIndex() + "): " + w
				+ ", " + h + " on: " + mStoryBoxWidth + "," + mStoryBoxHeight);
		// refreshColorTitle();
		// refreshFavorite();
		if (TNPreferenceManager.BOX_HAS_TOUCH_HIGHLIGHT) {
			mForegroundDrawable.setBounds(mCachedBounds);
			mForegroundDrawable.draw(canvas);
		}
		if (mNeedBackground) {
			Drawable d = getBackground();
			if (d != null) {
				d.setBounds(0, 0, w, h);
				d.draw(canvas);
			}
		}

		if (mImage != null) {
			Drawable d = mImage.getDrawable();
			if (d != null) {
				// if (mReadBox) {
				// d.setColorFilter(mColorFilter);
				// }
				// d.setBounds(0, 0, mImage.getWidth(), mImage.getHeight());
			}
		}
	}

	/**
	 * @param b
	 */
	public void setReadBox(boolean bRead) {
		mReadBox = bRead;
		// Fix: don't change color text when user readed
		refreshColorTitle();
	}

	/**
	 * @param f
	 */
	public void setFavorite(boolean fv) {
		mFavorited = fv;
		refreshFavorite();
	}

	public void setVideoIcon(boolean videoIcon) {
		mVideoIcon = videoIcon;
		refreshVideoIcon();
	}

	private void refreshColorTitle() {
		if (mTitle == null) {
			return;
		}
		if (mReadBox) {
			mTitle.setTextColor(getResources().getColor(
					R.color.box_hasread_textcolor));
		}
	}

	public void refreshFavorite() {
		if (mIvFavorited != null) {
			if (mFavorited) {
				mIvFavorited.setVisibility(VISIBLE);
			} else {
				mIvFavorited.setVisibility(GONE);
			}
		}
	}

	public void refreshVideoIcon() {
		if (mTitle != null) {
			GKIMLog.l(1, TAG + "refreshVideoIcon :" + mVideoIcon + " >>:"
					+ mTitle.getText() + "  " + mNumberOfColumn + " :"
					+ mNumberOfRow);
		}
		if (mVideoIcon) {
			if (mNumberOfColumn <= 2) {
				mIvVideoIconBig.setVisibility(View.INVISIBLE);
				mIvVideoIconSmallLeft.setVisibility(View.VISIBLE);
			} else {
				mIvVideoIconBig.setVisibility(View.VISIBLE);
				mIvVideoIconSmallLeft.setVisibility(View.INVISIBLE);
			}
		} else {
			mIvVideoIconBig.setVisibility(View.INVISIBLE);
			mIvVideoIconSmallLeft.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * @param textview
	 * @return
	 */
	private String adjustTextLength(TextView tv) {
		if (tv == null) {
			return "";
		}
		String input = tv.getText().toString();
		String result = input;
		String newtext = input;
		int lastSpaceIndex = 0;
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) tv
				.getLayoutParams();
		Paint p = tv.getPaint();
		Rect bound = new Rect();
		p.getTextBounds(input, 0, input.length() - 1, bound);
		float textHeight = p.getTextSize();
		float multiLine = (((lp.height - tv.getPaddingTop() - tv
				.getPaddingBottom()) / textHeight));
		float availableW = (lp.width - tv.getPaddingLeft() - tv
				.getPaddingRight()) * (multiLine + 0.2f);

		do {

			newtext = (String) TextUtils.ellipsize(result, tv.getPaint(),
					availableW, TruncateAt.END);
			if (!newtext.equals(result)) {
				lastSpaceIndex = newtext.lastIndexOf(" ");
				if (lastSpaceIndex > 0) {
					result = newtext.substring(0, lastSpaceIndex) + "...";
				} else {
					break;
				}
			} else {
				break;
			}
		} while (false);

		GKIMLog.lf(null, 0, TAG + "=>adjust title from: " + input + " to: "
				+ result);
		tv.setText(result);
		return result;
	}

	/**
	 * @return
	 */
	public float getPercentBoxHeight() {
		if (mBoxStory != null) {
			int iBh = mBoxStory.getBoxStoryHeight();
			if (iBh == 0) {
				iBh = mBoxCellSize;
			}
			if (iBh > 2) {
				return (float) ((iBh * 100.0f) / mBoxCellSize);
			} else {
				return (float) (iBh * 1.0f);
			}
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
				iBw = mBoxCellSize;
			}
			if (iBw > 2) {
				return (float) ((iBw * 100.0f) / mBoxCellSize);
			} else {
				return (float) (iBw * 1.0f);
			}
		}
		return 1.0f;
	}

	private int generateBoxIdFromSectionId(BoxStory boxStory) {
		int id = (int) (ID_SET_VIDEOSECIONBOX_ID + ((new Random().nextInt(4000)) + 1));
		try {
			id = Integer.parseInt(boxStory.getStoryId());
		} catch (NumberFormatException e) {
		}
		return id;
	}
}
