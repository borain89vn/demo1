/**
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;

/**
 * @author Timon Trinh
 * 
 */
public class GUIStoryTextModeDialog extends Dialog {

	private static final String TAG = "GUIStoryTextModeDialog";

	private View.OnClickListener mOnShareClickListener;
	private ToggleButton mTbtn;
	private Typeface mDefaultTF;
	private boolean mTabletVersion = false;

	private View.OnClickListener mDefaultOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			GKIMLog.lf(null, 0, TAG + "=>onClick: "
					+ v.getClass().getSimpleName());
			boolean hasProceed = false;
			if (mOnShareClickListener != null && !hasProceed) {
				mOnShareClickListener.onClick(v);
			}
		}
	};

	/**
	 * @param context
	 */
	public GUIStoryTextModeDialog(Context context) {
		super(context);
		initDialog();
	}

	/**
	 * @param context
	 * @param theme
	 */
	public GUIStoryTextModeDialog(Context context, int theme) {
		super(context, theme);
		initDialog();
	}

	/**
	 * @param context
	 * @param cancelable
	 * @param cancelListener
	 */
	public GUIStoryTextModeDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		initDialog();
	}

	private void initDialog() {
		GKIMLog.lf(getContext(), 0, TAG + "=>initDialog.");
		mTabletVersion = getContext().getResources()
				.getBoolean(R.bool.istablet);
		setCanceledOnTouchOutside(true);
		Window w = getWindow();
		w.requestFeature(Window.FEATURE_NO_TITLE);
		w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		if (mTabletVersion) {
			w.setGravity(Gravity.TOP | Gravity.CENTER);
		} else {
			w.setGravity(Gravity.BOTTOM | Gravity.CENTER);
		}
		setContentView(R.layout.dlg_storydetail_textmode);
		mDefaultTF = TNPreferenceManager.getTNTypeface();
		ImageButton btn = (ImageButton) findViewById(R.id.imb_storyfooter_fontsmaller);
		if (btn != null) {
			btn.setOnClickListener(mDefaultOnClickListener);
		}
		btn = (ImageButton) findViewById(R.id.imb_storyfooter_fontbigger);
		if (btn != null) {
			btn.setOnClickListener(mDefaultOnClickListener);
		}
		
		mTbtn = (ToggleButton) findViewById(R.id.tbtn_storyfooter_textmode);
		if (mTbtn != null) {
			mTbtn.setTypeface(mDefaultTF);
			mTbtn.setOnClickListener(mDefaultOnClickListener);
		}
	}

	public void setOnShareClickListener(View.OnClickListener l) {
		mOnShareClickListener = l;
	}

	/**
	 * @param bChecked
	 */
	public void setToggleChecked(boolean bChecked) {
		if (mTbtn != null) {
			mTbtn.setChecked(bChecked);
		}
	}
}
