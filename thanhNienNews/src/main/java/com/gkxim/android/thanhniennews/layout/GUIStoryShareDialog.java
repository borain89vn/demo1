package com.gkxim.android.thanhniennews.layout;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.utils.GKIMLog;

/**
 * @author Timon Trinh
 * 
 */
public class GUIStoryShareDialog extends Dialog {

	private static final String TAG = "GUIStoryShareDialog";
	private boolean mTabletVersion = false;
	private View.OnClickListener mOnShareClickListener;
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
	public GUIStoryShareDialog(Context context) {
		super(context);
		initDialog();
	}

	/**
	 * @param context
	 * @param theme
	 */
	public GUIStoryShareDialog(Context context, int theme) {
		super(context, theme);
		initDialog();
	}

	/**
	 * @param context
	 * @param cancelable
	 * @param cancelListener
	 */
	public GUIStoryShareDialog(Context context, boolean cancelable,
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
		// w.setLayout(
		// WindowManager.LayoutParams.MATCH_PARENT,
		// (int) getContext().getResources().getDimension(
		// R.dimen.menu_setting_dialog_height));
		if (mTabletVersion) {
			w.setGravity(Gravity.TOP);
		} else {
			w.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
		}
		setContentView(R.layout.dlg_storydetail_share);

		ImageView btn = (ImageView) findViewById(R.id.imv_storydetail_shareby_email);
		if (btn != null) {
			btn.setOnClickListener(mDefaultOnClickListener);
		}
		btn = (ImageView) findViewById(R.id.imv_storydetail_shareby_facebook);
		if (btn != null) {
			btn.setOnClickListener(mDefaultOnClickListener);
		}
		btn = (ImageView) findViewById(R.id.imv_storydetail_shareby_twitter);
		if (btn != null) {
			btn.setOnClickListener(mDefaultOnClickListener);
		}
		btn = (ImageView) findViewById(R.id.imv_storydetail_dlg_shareby_comment);
		if (btn != null) {
			btn.setOnClickListener(mDefaultOnClickListener);
		}

	}

	public void setOnShareClickListener(View.OnClickListener l) {
		mOnShareClickListener = l;
	}

	public void showFromView(View v) {
	}
}
