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
import android.widget.Button;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;

/**
 * @author Timon
 *
 */
public class GUIMenuSettingDialog extends Dialog {
	private static final String TAG = GUIMenuSettingDialog.class
			.getSimpleName();

	private boolean mTabletVersion;
	private View.OnClickListener mOnShareClickListener;
	private View.OnClickListener mDefaultOnClickListener = getDefaultOnClickListener();

	private Button mBtnLogout;

	/**
	 * @param context
	 */
	public GUIMenuSettingDialog(Context context) {
		super(context);
		initLayout(context);
	}

	@Override
	protected void onStart() {
		GKIMLog.lf(getContext(), 1, TAG + "=>onStart");
		if (mBtnLogout != null) {
			if (TNPreferenceManager.checkLoggedIn()) {
				mBtnLogout.setVisibility(View.VISIBLE);
			} else {
				mBtnLogout.setVisibility(View.GONE);
			}
		}
		super.onStart();
	}

	public void setOnShareClickListener(View.OnClickListener l) {
		mOnShareClickListener = l;
	}

	private android.view.View.OnClickListener getDefaultOnClickListener() {
		return (new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GKIMLog.lf(null, 0, TAG + "=>onClick: "
						+ v.getClass().getSimpleName());
				boolean hasProceed = false;
				if (mOnShareClickListener != null && !hasProceed) {
					mOnShareClickListener.onClick(v);
				}
			}
		});
	}

	/**
	 * @param context
	 */
	private void initLayout(Context context) {
		GKIMLog.lf(getContext(), 1, TAG + "=>initLayout from: " + context);
		mTabletVersion = getContext().getResources()
				.getBoolean(R.bool.istablet);
		setCanceledOnTouchOutside(true);
		
		Window w = getWindow();
		w.requestFeature(Window.FEATURE_NO_TITLE);
		w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		if (mTabletVersion) {
			w.setGravity(Gravity.BOTTOM | Gravity.LEFT);
		}else {
			w.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
		}
		
		setContentView(R.layout.dlg_menu_setting);
		
		Typeface tf = TNPreferenceManager.getTNTypeface();
		Button btnSupport = (Button) findViewById(R.id.btn_menu_setting_support);
		if (btnSupport != null) {
			btnSupport.setTypeface(tf);
			btnSupport.setOnClickListener(mDefaultOnClickListener);
		}
		Button btnPolicy = (Button) findViewById(R.id.btn_menu_setting_policy);
		if (btnPolicy != null) {
			btnPolicy.setTypeface(tf);
			btnPolicy.setOnClickListener(mDefaultOnClickListener);
		}
		Button btnFeedback = (Button) findViewById(R.id.btn_menu_setting_feedback);
		if (btnFeedback != null) {
			btnFeedback.setTypeface(tf);
			btnFeedback.setOnClickListener(mDefaultOnClickListener);
		}
		Button btnPNSCheck = (Button) findViewById(R.id.btn_menu_setting_pnscheck);
		if (btnPNSCheck != null) {
			btnPNSCheck.setTypeface(tf);
			btnPNSCheck.setOnClickListener(mDefaultOnClickListener);
		}
		mBtnLogout = (Button) findViewById(R.id.btn_menu_setting_logout);
		if (mBtnLogout != null) {
			mBtnLogout.setTypeface(tf);
			mBtnLogout.setOnClickListener(mDefaultOnClickListener);
		}
	}

}
