/**
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.gkim.thanhniennews.R;
import com.gkxim.android.utils.GKIMLog;

/**
 * @author HP
 * 
 */
public class GUIVideoOptionDialog extends Dialog {

	private static final String TAG = GUIVideoOptionDialog.class
			.getSimpleName();

	private int mSelectedOption = 0;// 0 = cancelled, 1 = camera, 2 = sdcard

	private View.OnClickListener mDefaultOnClickListener = getOnClickListener();

	/**
	 * @param context
	 */
	public GUIVideoOptionDialog(Context context) {
		super(context);
		initDialog();
	}

	private android.view.View.OnClickListener getOnClickListener() {
		return (new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GKIMLog.lf(null, 0, TAG + "=>onClick: "
						+ v.getClass().getSimpleName());
				switch (v.getId()) {
				case R.id.btn_photo_option_camera:
					mSelectedOption = 1;
					break;
				case R.id.btn_photo_option_gallery:
					mSelectedOption = 2;
					break;
				default:
					mSelectedOption = 0;
					break;
				}
				dismiss();
			}
		});
	}

	public void initDialog() {
		GKIMLog.lf(getContext(), 0, TAG + "=>initDialog.");
		setTitle(R.string.dlg_photo_option_title);
		setCanceledOnTouchOutside(true);
		Window w = getWindow();
		w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		// w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		// w.setBackgroundDrawableResource(R.drawable.bg_storycomment_compose_dlg);

		setContentView(R.layout.dlg_video_option);
		Button btn = (Button) findViewById(R.id.btn_photo_option_camera);
		if (btn != null) {
			btn.setOnClickListener(mDefaultOnClickListener);
		}
		btn = (Button) findViewById(R.id.btn_photo_option_gallery);
		if (btn != null) {
			btn.setOnClickListener(mDefaultOnClickListener);
		}
	}

	public int getSelectedOption() {
		int selectOption = mSelectedOption;
		mSelectedOption = 0;
		return selectOption;
	}
}
