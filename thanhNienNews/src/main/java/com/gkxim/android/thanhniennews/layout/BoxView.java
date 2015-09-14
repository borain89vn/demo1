/**
 * File: BoxView.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 09-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.models.BoxElement;
import com.gkxim.android.thanhniennews.models.BoxStory;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;

/**
 * @author Timon Trinh
 */
@Deprecated
@SuppressWarnings("unused")
public class BoxView extends RelativeLayout {

	private static final int DEFINE_BOX_COL_MASK = 0x0F;
	private static final int DEFINE_BOX_ROW_MASK = 0xF0;
	private static final int DEFINE_BOX_ELEMENT_RULES_LEFT_TOP = 1;
	private static final int DEFINE_BOX_ELEMENT_RULES_RIGHT_TOP = 2;
	private static final int DEFINE_BOX_ELEMENT_RULES_LEFT_BOTTOM = 3;
	private static final int DEFINE_BOX_ELEMENT_RULES_RIGHT_BOTTOM = 4;
	private static final int DEFINE_BOX_ELEMENT_RULES_OVERLAP_TOP = 5;
	private static final int DEFINE_BOX_ELEMENT_RULES_OVERLAP_BOTTOM = 6;

	private static final String TAG = "BoxView";

	private int mBoxCellSize = -1;

	// Box 1x1 is smallest
	private int mNumberOfColumn = 1;
	private int mNumberOfRow = 1;
	private ImageView mImage = null;
	private TextView mTitle = null;
	private TextView mShortContent = null;

	//

	/**
	 * 09-11-2012
	 */
	public BoxView(Context context) {
		super(context);
		initBoxView();
	}

	/**
	 * 09-11-2012
	 */
	public BoxView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initBoxView();
	}

	/**
	 * 09-11-2012
	 */
	public BoxView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initBoxView();
	}

	public int getNumberOfColumSpan() {
		return mNumberOfColumn;
	}

	public int getNumberOfRowSpan() {
		return mNumberOfRow;
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
	
	private void initBoxView() {

		mBoxCellSize = TNPreferenceManager.getBoxSize();
		if (mBoxCellSize < 0) {
			mBoxCellSize = this.getContext().getResources()
					.getInteger(R.integer.boxcellsize);
		}
		inflateBoxView();

	}

	private void inflateBoxView() {
		inflate(this.getContext(), R.layout.boxview, this);
		// least textview should appeared
		mTitle = (TextView) this.findViewById(R.id.boxview_title);
		// mImage = (ImageView) findViewById(R.id.boxview_image);
		// mShortContent = (TextView) findViewById(R.id.boxview_shortcontent);
		GKIMLog.lf(this.getContext(), 0, TAG + "=>inflateBoxView: ["
				+ getWidth() + ", " + getHeight() + "].");
	}

//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
////		GKIMLog.lf(this.getContext(), 0, TAG + "=>onMeasure: " + mTitle.getText());
//		int modeW = MeasureSpec.getMode(widthMeasureSpec);
//		int modeH = MeasureSpec.getMode(heightMeasureSpec);
//		int sizeW = MeasureSpec.getSize(widthMeasureSpec);
//		int sizeH = MeasureSpec.getSize(heightMeasureSpec);
//		BoxStory tag = (BoxStory) getTag();
//		int calW = mBoxCellSize * mNumberOfColumn + 2
//				* (mNumberOfColumn - 1) * getPaddingLeft();
//		int calH = mBoxCellSize * mNumberOfRow + 2 * (mNumberOfRow - 1)
//				* getPaddingTop();
//		calW = resolveSize(calW, widthMeasureSpec);
//		calH = resolveSize(calH, heightMeasureSpec);
//		GKIMLog.lf(this.getContext(), 0, TAG + "=>onMeasure: " + tag.getBoxIndex() + "[" + sizeW + ", "
//				+ sizeH + " - [" + modeW + ", " + modeH + "] -> [" + calW + ", " + calH + "].");
////		if (mImage != null) {
////			
//////			mImage.measure(calW, calH);
////		}
////		if (mTitle != null) {
////			GKIMLog.lf(this.getContext(), 0, TAG + "=>mTitle: [" + mTitle.getWidth() + ", " + mTitle.getHeight());
//////			mTitle.measure(calW, calH);
////		}
////		if (mShortContent != null) {
////			GKIMLog.lf(this.getContext(), 0, TAG + "=>mShortContent: [" + mShortContent.getWidth() + ", " + mShortContent.getHeight());
//////			mShortContent.measure(calW, calH);
////		}
//		setMeasuredDimension(calW, calH);
//	}

	public void setBoxStory(BoxStory boxStory) {
		GKIMLog.lf(this.getContext(), 0, TAG + "=>setBoxStory.");
		if (boxStory != null) {
			int storyLayout = boxStory.getLayout();
			mNumberOfColumn = storyLayout & DEFINE_BOX_COL_MASK;
			mNumberOfRow = ((storyLayout & DEFINE_BOX_ROW_MASK) >> 4);
			boolean hasImage = false;
			boolean hasTitle = false;
			boolean hasShortCont = false;

			if (boxStory.hasElementType(BoxElement.BOXELEMENT_TYPE_IMAGE)) {
				mImage = (ImageView) this.findViewById(R.id.boxview_image);
				if (mImage != null) {
//					if (!mImage.getParent().equals(this)) {
//						ViewGroup vg = (ViewGroup) mImage.getParent();
//						vg.removeView(mImage);
//						this.addView(mImage, 0);
//					}
					BoxElement[] imageBoxes = boxStory
							.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_IMAGE);
					if (imageBoxes != null) {
						BoxElement elm = imageBoxes[0];
						applyRules(mImage, elm);
					}

					hasImage = true;
				} else {
					GKIMLog.lf(this.getContext(), 4, TAG
							+ "=>setBoxStory failed on find ImageView.");
				}
			}
			if (boxStory
					.hasElementType(BoxElement.BOXELEMENT_TYPE_SHORTCONTENT)) {
				mShortContent = (TextView) this
						.findViewById(R.id.boxview_shortcontent);
				if (mShortContent != null) {
//					if (!mShortContent.getParent().equals(this)) {
//						ViewGroup vg = (ViewGroup) mShortContent.getParent();
//						vg.removeView(mShortContent);
//						this.addView(mShortContent, 2);
//					}
					BoxElement[] shortTextElms = boxStory
							.getBoxElementbyType(BoxElement.BOXELEMENT_TYPE_SHORTCONTENT);
					if (shortTextElms != null) {
						// TODO: for loop
						BoxElement elm = shortTextElms[0];
//						mShortContent.setTextColor(elm.getTextColor());
//						mShortContent.setTextSize(elm.getTextSize());
//						mShortContent.setTypeface(UIUtils.getDefaultTypeFace(
//								this.getContext().getApplicationContext(), 0),
//								elm.getTextType());
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
						// if (!mTitle.getParent().equals(this)) {
						// ViewGroup vg = (ViewGroup) mTitle.getParent();
						// vg.removeView(mTitle);
						// this.addView(mTitle, 1);
						// }
						// TODO: for loop
						BoxElement elm = titleElms[0];
//						mTitle.setTextColor(elm.getTextColor());
//						mTitle.setTextSize(elm.getTextSize());
//						mTitle.setTypeface(UIUtils.getDefaultTypeFace(this
//								.getContext().getApplicationContext(), 0), elm
//								.getTextType());
						mTitle.setText(elm.getContent());
						applyRules(mTitle, elm);
						hasTitle = true;
					}
				} else {
					GKIMLog.lf(this.getContext(), 4, TAG
							+ "=>setBoxStory failed on find title TextView.");
				}
			}

			GKIMLog.lf(this.getContext(), 5, TAG + "=>setBoxStory, type: "
					+ storyLayout + ", row: " + mNumberOfRow + ", col: "
					+ mNumberOfColumn + ", with "
					+ (hasImage ? "imgage, " : "")
					+ (hasTitle ? "title, " : "")
					+ (hasShortCont ? "short content." : ""));
			this.setTag(boxStory);
		}
	}

	private void applyRules(View view, BoxElement elm) {

		int alignmentInBox = elm.getAlignmentInBox();
		int elmW = elm.getWidthCell();
		RelativeLayout.LayoutParams lp = (LayoutParams) view.getLayoutParams();
		
		lp.width = mBoxCellSize * elmW + 2 * (elmW - 1) * getPaddingLeft();
		lp.height = mBoxCellSize;
		lp.alignWithParent = true;
		switch (alignmentInBox) {
		case DEFINE_BOX_ELEMENT_RULES_LEFT_TOP:
			// default;
			// lp.addRule(ALIGN_PARENT_LEFT);
			// lp.addRule(ALIGN_PARENT_TOP);
			break;
		case DEFINE_BOX_ELEMENT_RULES_RIGHT_TOP:
			lp.addRule(ALIGN_PARENT_RIGHT);
			lp.addRule(ALIGN_PARENT_TOP);
			break;
		case DEFINE_BOX_ELEMENT_RULES_LEFT_BOTTOM:
			lp.addRule(ALIGN_PARENT_LEFT);
			lp.addRule(ALIGN_PARENT_BOTTOM);
			break;
		case DEFINE_BOX_ELEMENT_RULES_RIGHT_BOTTOM:
			lp.addRule(ALIGN_PARENT_RIGHT);
			lp.addRule(ALIGN_PARENT_BOTTOM);
			break;
		case DEFINE_BOX_ELEMENT_RULES_OVERLAP_TOP:
			// lp.addRule(ALIGN_PARENT_LEFT);
			// lp.addRule(ALIGN_PARENT_RIGHT);
			lp.addRule(ALIGN_PARENT_TOP);
			lp.width = mBoxCellSize * mNumberOfColumn;
			break;
		case DEFINE_BOX_ELEMENT_RULES_OVERLAP_BOTTOM:
			// lp.addRule(ALIGN_PARENT_LEFT);
			// lp.addRule(ALIGN_PARENT_RIGHT);
			lp.addRule(ALIGN_PARENT_BOTTOM);
			lp.width = mBoxCellSize * mNumberOfColumn;
			break;
		case 0:
			lp.width = mBoxCellSize * mNumberOfColumn + 2
					* (mNumberOfColumn - 1) * getPaddingLeft();
			lp.height = mBoxCellSize * mNumberOfRow + 2 * (mNumberOfRow - 1)
					* getPaddingTop();
			lp.addRule(ALIGN_PARENT_LEFT);
			lp.addRule(ALIGN_PARENT_TOP);
			break;
		default: 
			lp.addRule(ALIGN_PARENT_LEFT);
			lp.addRule(ALIGN_PARENT_TOP);
			break;
		}
		GKIMLog.lf(this.getContext(), 0, TAG + "=>applyRules for: "
				+ view.getClass().getSimpleName() + ": " + lp.debug(""));
		view.setLayoutParams(lp);
		view.setVisibility(VISIBLE);
	}

}
