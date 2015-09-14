package com.gkxim.android.thanhniennews.layout;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.gkim.thanhniennews.R;
import com.gkxim.android.utils.GKIMLog;


public class GUIPNSSuccessDialog extends Dialog {
	
	private static final String TAG = GUIPNSSuccessDialog.class.getSimpleName();
	private android.view.View.OnClickListener mOnBtnClickListener = new android.view.View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_section_dlg_exit_confirmyes:
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
	public GUIPNSSuccessDialog(Context context) {
		super(context);
		initLayout(context);
	}

	/**
	 * @param context
	 */
	private void initLayout(Context context) {
		GKIMLog.lf(getContext(), 1, TAG + "=>initDialog");
		setTitle(R.string.app_name);
		setContentView(R.layout.dlg_pns_success);
		Button btn = (Button) findViewById(R.id.btn_section_dlg_exit_confirmyes);
		btn.setOnClickListener(mOnBtnClickListener );
	}


}
