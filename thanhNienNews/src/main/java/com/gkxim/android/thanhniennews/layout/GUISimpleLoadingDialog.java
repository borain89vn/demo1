package com.gkxim.android.thanhniennews.layout;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import com.gkim.thanhniennews.R;

/**
 * @author Timon
 * 
 */
public class GUISimpleLoadingDialog extends AlertDialog {

	/**
	 * @param context
	 */
	public GUISimpleLoadingDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCancelable(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dlg_loading_progress);
	}

}
