/**
 * File: BoxViewFrameLayout.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 21-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.models.BoxElement;
import com.gkxim.android.thanhniennews.models.BoxStory;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 *
 */
public class BoxViewFrameLayout extends FrameLayout {

	private static final int DEFINE_BOX_COL_MASK = 0x0F;
	private static final int DEFINE_BOX_ROW_MASK = 0xF0;
	private static final int DEFINE_BOX_ELEMENT_RULES_STRETCH = 0;
	private static final int DEFINE_BOX_ELEMENT_RULES_LEFT_TOP = 1;
	private static final int DEFINE_BOX_ELEMENT_RULES_RIGHT_TOP = 2;
	private static final int DEFINE_BOX_ELEMENT_RULES_LEFT_BOTTOM = 3;
	private static final int DEFINE_BOX_ELEMENT_RULES_RIGHT_BOTTOM = 4;
	private static final int DEFINE_BOX_ELEMENT_RULES_OVERLAP_TOP = 5;
	private static final int DEFINE_BOX_ELEMENT_RULES_OVERLAP_BOTTOM = 6;
	private static final int DEFINE_BOX_ELEMENT_RULES_OVERLAP_LEFT = 7;
	private static final int DEFINE_BOX_ELEMENT_RULES_OVERLAP_RIGHT = 8;

	private static final String TAG = BoxViewFrameLayout.class.getSimpleName();
	private String specialCharacter = "";
	private int mBoxCellSize = -1;
	private int mNumberOfColumn = 1;
	private int mNumberOfRow = 1;

	private TextView mTitle = null;
	private ImageView mImage = null;
	private TextView mShortContent = null;
	private ImageView mIvFavorited = null;
	private ImageView mIvVideoIconBig = null;
	private ImageView mIvVideoIconSmallLeft = null;
	private ImageView mImageLatLng = null;
	private LinearLayout mLocationWrapper = null;

	private boolean mNeedBackground = false;
	private boolean mReadBox = false;
	private int mMultiPaddingNum = 1;
	private Drawable mForegroundDrawable;
	private Rect mCachedBounds = new Rect();

	private int mStoryBoxWidth;
	private int mStoryBoxHeight;
	private float mScaleDensity = 1f;
	private int mGapWidth;
	private boolean mFavorited = false;
	private boolean mVideoIcon = false;
	private boolean mHasItems = false;

	// private ColorFilter mColorFilter = null;

	/**
	 * 21-11-2012
	 */
	public BoxViewFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initBoxView();
	}

	/**
	 * 21-11-2012
	 */
	public BoxViewFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initBoxView();
	}

	/**
	 * 21-11-2012
	 */
	public BoxViewFrameLayout(Context context) {
		super(context);
		initBoxView();
	}

	@SuppressLint("InlinedApi")
	private void initBoxView() {
		mGapWidth = TNPreferenceManager.getGapWidth();
		if (mGapWidth < 0) {
			mGapWidth = getResources().getInteger(R.integer.middlepadding);
		}
		mBoxCellSize = TNPreferenceManager.getBoxSize();
		if (mBoxCellSize < 0) {
			mBoxCellSize = this.getContext().getResources()
					.getInteger(R.integer.boxcellsize);
		}
		mScaleDensity = getResources().getDisplayMetrics().scaledDensity;
		// Keep this for next release
		// ColorMatrix cm = new ColorMatrix();
		// cm.setSaturation(0);
		// mColorFilter = new ColorMatrixColorFilter(cm);

		// mTextMaxLength = getContext().getResources().getInteger(
		// R.integer.box_textmaxlength_title);
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

	private void inflateBoxView() {
		inflate(this.getContext(), R.layout.boxview, this);
		this.setAlwaysDrawnWithCacheEnabled(true);
		mTitle = (TextView) this.findViewById(R.id.boxview_title);
		specialCharacter = getContext().getResources().getString(
				R.string.special_character);
		mIvFavorited = (ImageView) this.findViewById(R.id.boxview_favorited);
		mIvVideoIconBig = (ImageView) this
				.findViewById(R.id.boxview_video_icon_big);
		mIvVideoIconSmallLeft = (ImageView) this
				.findViewById(R.id.boxview_video_icon_small_left);
		mLocationWrapper = (LinearLayout) this.findViewById(R.id.box_wrapper);

		mImageLatLng = (ImageView) this.findViewById(R.id.boxview_lat_lng);
	}

	public int getNumberOfColumSpan() {
		return mNumberOfColumn;
	}

	public int getNumberOfRowSpan() {
		return mNumberOfRow;
	}

	public int getBoxSupposeWidth() {
		int calWidth = (mBoxCellSize * mNumberOfColumn + mMultiPaddingNum
				* (mNumberOfColumn - 1) * mGapWidth);
		return calWidth;
		// return Math.max(mStoryBoxWidth, calHeight);
	}

	public int getBoxSupposeHeight() {
		return (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
				* (mNumberOfRow - 1) * getPaddingTop());
		// int calHeight = (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
		// * (mNumberOfRow - 1) * getPaddingTop());
		// return Math.max(mStoryBoxHeight, calHeight);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.FrameLayout#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getBoxSupposeWidth(), getBoxSupposeHeight());
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
	
	public BoxStory getBoxStory(){
		return mBoxStory;
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

	public boolean isHasItems() {
		return mHasItems;
	}
	
	private BoxStory mBoxStory;

	public void setBoxStory(BoxStory boxStory) {
		GKIMLog.lf(this.getContext(), 0, TAG + "=>setBoxStory.");
		mBoxStory = boxStory;
		if (boxStory != null) {
			boolean isSpecalBox = false;
			int index = boxStory.getBoxIndex();
			if (index == 3) {
				BoxElement[] titleElms = boxStory
						.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_TITLE);
				if (titleElms != null && titleElms.length > 0) {
					BoxElement elm = titleElms[0];
					if (elm.getWeathercontent() != null
							&& elm.getWeatherimg() != null) {
						isSpecalBox = true;
						mImageLatLng.setVisibility(VISIBLE);
						mLocationWrapper.setVisibility(VISIBLE);
						// mLocationWrapper.setBackgroundColor(Color.CYAN);
						mTitle.setVisibility(GONE);
					}

				}
			}
			int storyLayout = boxStory.getLayout();
			mNumberOfColumn = storyLayout & DEFINE_BOX_COL_MASK;
			mNumberOfRow = ((storyLayout & DEFINE_BOX_ROW_MASK) >> 4);
			mStoryBoxWidth = boxStory.getBoxStoryWidth();
			mStoryBoxHeight = boxStory.getBoxStoryHeight();
			GKIMLog.l(1, TAG + " setBoxStory :" + boxStory.getStoryId());
			// if mStoryBoxWidth, mStoryBoxHeight = 0
			boolean hasImage = false;
			boolean hasTitle = false;
			boolean hasShortCont = false;
			mFavorited = boxStory.isFavourited();
			mVideoIcon = boxStory.getVideo() > 0 ? true : false;
			mHasItems = boxStory.isHasItems();

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
						applyRules(mImage, elm);
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
						applyRules(mShortContent, elm);
						hasShortCont = true;
					}
				} else {
					GKIMLog.lf(this.getContext(), 4, TAG
							+ "=>setBoxStory failed on find short TextView.");
				}
			}
			if (boxStory.hasElementType(BoxElement.BOXELEMENT_TYPE_TITLE)) {
				if (isSpecalBox) {

					FrameLayout.LayoutParams lp = (LayoutParams) mImageLatLng
							.getLayoutParams();
					lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
					lp.leftMargin = (int) (mImage.getLayoutParams().width * 2.2f / 3);
					mImageLatLng.setLayoutParams(lp);

					if (mLocationWrapper != null) {
						BoxElement[] titleElms = boxStory
								.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_TITLE);
						if (titleElms != null) {

							TextView textViewWeather = (TextView) findViewById(R.id.boxview_weather);
							TextView textViewProvince = (TextView) findViewById(R.id.boxview_province);
							ImageView imageViewWeather = (ImageView) findViewById(R.id.boxview_img_weather);

							BoxElement elm = titleElms[0];

							ImageLoader.getInstance().displayImage(
									elm.getWeatherimg(), imageViewWeather);

							// DisplayImageOptions options = new
							// DisplayImageOptions.Builder()
							// .imageScaleType(
							// ImageScaleType.EXACTLY_STRETCHED)
							// .cacheInMemory(true).build();
							//
							// ImageLoader.getInstance().displayImage(
							// elm.getWeatherimg(), imageViewWeather,
							// options);

							textViewWeather
									.setTextSize(getResources()
											.getDimensionPixelSize(
													R.dimen.section_box_title_textsize));
							textViewProvince
									.setTextSize(getResources()
											.getDimensionPixelSize(
													R.dimen.section_box_title_textsize));

							textViewWeather.setTypeface(UIUtils
									.getDefaultTypeFace(this.getContext()
											.getApplicationContext(), 0), elm
									.getTextType());

							textViewProvince.setTypeface(UIUtils
									.getDefaultTypeFace(this.getContext()
											.getApplicationContext(), 0), elm
									.getTextType());

							// Fix: don't change color text when user readed
							if (mReadBox) {
								textViewWeather
										.setTextColor(getResources().getColor(
												R.color.box_hasread_textcolor));
								textViewProvince
										.setTextColor(getResources().getColor(
												R.color.box_hasread_textcolor));

							} else {
								textViewWeather
										.setTextColor(elm.getTextColor());
								textViewProvince.setTextColor(elm
										.getTextColor());

							}
							applyRules(mLocationWrapper, elm);
							String title = elm.getContent();
							textViewProvince.setText(title);
							String titleWeather = elm.getWeathercontent();
							textViewWeather.setText(titleWeather
									+ (char) 0x00B0 + "C");

							hasTitle = true;

							float textSize = TNPreferenceManager
									.getTextSizeBoxView();
							if (textSize > 0) {
								textViewProvince.setTextSize(
										TypedValue.COMPLEX_UNIT_PX, textSize);
								textViewWeather.setTextSize(
										TypedValue.COMPLEX_UNIT_PX, textSize);
							}
							// adjustTextLength(textViewProvince);
							// adjustTextLength(textViewWeather);
						}

					}
				} else {
					if (mTitle != null) {
						BoxElement[] titleElms = boxStory
								.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_TITLE);
						if (titleElms != null) {
							BoxElement elm = titleElms[0];
							mTitle.setTextSize(getResources()
									.getDimensionPixelSize(
											R.dimen.section_box_title_textsize));
							mTitle.setTypeface(UIUtils.getDefaultTypeFace(this
									.getContext().getApplicationContext(), 0),
									elm.getTextType());
							// Fix: don't change color text when user readed
							if (mReadBox) {
								mTitle.setTextColor(getResources().getColor(
										R.color.box_hasread_textcolor));
							} else {
								mTitle.setTextColor(elm.getTextColor());
							}
							applyRules(mTitle, elm);
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
						}
					} else {
						GKIMLog.lf(
								this.getContext(),
								4,
								TAG
										+ "=>setBoxStory failed on find title TextView.");
					}
				}
			}
			refreshFavorite();
			refreshVideoIcon();
			// finally update the boxView's size
			GKIMLog.lf(this.getContext(), 5, TAG + "=>setBoxStory, type: "
					+ storyLayout + ", row: " + mNumberOfRow + ", col: "
					+ mNumberOfColumn + ", with "
					+ (hasImage ? "imgage, " : "")
					+ (hasTitle ? "title, " : "")
					+ (hasShortCont ? "short content." : ""));

			this.setTag(boxStory);

//			if (isSpecalBox) {

//				TextView textViewWeather = (TextView) findViewById(R.id.boxview_weather);
//				TextView textViewProvince = (TextView) findViewById(R.id.boxview_province);
//				ImageView imageViewWeather = (ImageView) findViewById(R.id.boxview_img_weather);
//
//				textViewProvince.measure(MEASURED_SIZE_MASK, MEASURED_SIZE_MASK);
//				textViewWeather.measure(MEASURED_SIZE_MASK, MEASURED_SIZE_MASK);
//				int heightBox = mImage.getLayoutParams().height;
//				int heightTXt1 = textViewWeather.getMeasuredHeight();
//				int heightTxt2 = textViewProvince.getMeasuredHeight();
//				
//				
//				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) imageViewWeather
//						.getLayoutParams();
//				lp.height = heightBox-heightTXt1-heightTxt2 - lp.topMargin;
//				imageViewWeather.setLayoutParams(lp);
//			}

		}
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
			lp.height = (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
					* (mNumberOfRow - 1) * getPaddingTop()) / 2;
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

			tv.setPadding(mGapWidth * 3 / 4, mGapWidth / 2, mGapWidth * 3 / 4,
					mGapWidth);
		} else if (view instanceof LinearLayout) {
			LinearLayout tv = (LinearLayout) view;
			tv.setPadding(mGapWidth * 3 / 4, mGapWidth / 2, mGapWidth * 3 / 4,
					mGapWidth);
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
		// int padding = getResources().getInteger(R.integer.middlepadding);
		// int width = mBoxCellSize * mNumberOfColumn + mMultiPaddingNum
		// * (mNumberOfColumn - 1) * padding;
		// int height = (mBoxCellSize * mNumberOfRow + mMultiPaddingNum
		// * (mNumberOfRow - 1) * getPaddingTop());
		// GKIMLog.lf(null, 0, TAG + "=>setBoxBackground: " + width + "," +
		// height);
		// // background.setBounds(0, 0, width, height);
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
				d.setBounds(0, 0, mImage.getWidth(), mImage.getHeight());
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
		if (mFavorited) {
			mIvFavorited.setVisibility(VISIBLE);
		} else {
			mIvFavorited.setVisibility(GONE);
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
		LayoutParams lp = (LayoutParams) tv.getLayoutParams();
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

		// if (result.trim().length() != subText.trim().length()) {
		// // while (subText.length() > 48) {
		// // lastSpaceIndex = subText.lastIndexOf(" ");
		// // if (lastSpaceIndex > 0) {
		// // subText = subText.substring(0, lastSpaceIndex);
		// // }
		// // }
		// result = subText + specialCharacter;
		// }
		GKIMLog.lf(null, 0, TAG + "=>adjust title from: " + input + " to: "
				+ result);
		tv.setText(result);
		return result;
	}
}
