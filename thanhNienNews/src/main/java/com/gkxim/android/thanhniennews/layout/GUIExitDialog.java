package com.gkxim.android.thanhniennews.layout;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.gkim.thanhniennews.R;
import com.gkxim.android.utils.GKIMLog;

/**
 * @author Timon
 *
 */
public class GUIExitDialog extends Dialog {
	
	private static final String TAG = GUIExitDialog.class.getSimpleName();
	private OnClickListener mConfrimedYesListener;
	private android.view.View.OnClickListener mOnBtnClickListener = new android.view.View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_section_dlg_exit_confirmyes:
				if (mConfrimedYesListener != null) {
					mConfrimedYesListener.onClick(GUIExitDialog.this, R.id.btn_section_dlg_exit_confirmyes);
				}
				break;
			case R.id.btn_section_dlg_exit_confirmno:
				dismiss();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * @param context
	 */
	public GUIExitDialog(Context context) {
		super(context);
		initLayout(context);
	}

	/**
	 * @param context
	 */
	private void initLayout(Context context) {
		GKIMLog.lf(getContext(), 1, TAG + "=>initDialog");
		setTitle(R.string.toast_title);
		setContentView(R.layout.dlg_section_exit);
		Button btn = (Button) findViewById(R.id.btn_section_dlg_exit_confirmyes);
		btn.setOnClickListener(mOnBtnClickListener );
		btn = (Button) findViewById(R.id.btn_section_dlg_exit_confirmno);
		btn.setOnClickListener(mOnBtnClickListener );
	}

	/**
	 * @param onClickListener
	 */
	public void setConfirmedExit(
			android.content.DialogInterface.OnClickListener onClickListener) {
		mConfrimedYesListener = onClickListener;
		
	}

}
