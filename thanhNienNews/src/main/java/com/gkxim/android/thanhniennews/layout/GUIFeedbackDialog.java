/**
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Timon
 * 
 */
public class GUIFeedbackDialog extends Dialog {
	private static final String TAG = GUIFeedbackDialog.class.getSimpleName();

	private ProgressDialog mProgressDialog;

	private EditText mEdEmail;
	private EditText mEdContent;
	private RatingBar mRating;
	private ImageView mIvSend;

	private OnDownloadCompletedListener mOnDownloadComplete = new OnDownloadCompletedListener() {

		@Override
		public void onCompleted(Object key, String result) {
			GKIMLog.lf(null, 0, TAG + "=>onCompleted: " + key.toString());
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			if (result == null || result.length() <= 0) {
				GKIMLog.lf(getContext(), 1, TAG + "=>sendFeedback() failed");
				mIvSend.setEnabled(true);
				return;
			} else {
				Gson gson = new GsonBuilder().registerTypeAdapter(
						GenericResponse.class,
						new GenericResponse.GenericResponseConverter()).create();
				GenericResponse gr = gson.fromJson(result, GenericResponse.class);
				if (gr != null ) {
					if (gr.isSucceed()) {
						GKIMLog.lf(getContext(), 1, TAG + "=>sendFeedback() succeed");
						UIUtils.showToast(getContext(), getContext().getResources()
								.getString(R.string.dlg_user_feedback_thanks));
					}else {
						UIUtils.showToast(getContext(), TNPreferenceManager
								.getErrorMessageFromCode(gr.resultCode));
					}
				} else {
					UIUtils.showToast(getContext(), getContext().getResources()
							.getString(R.string.dlg_feedback_failed));
				}
			}
			dismiss();
		}

		@Override
		public String doInBackgroundDebug(Object... params) {
			return null;
		}
	};

	public GUIFeedbackDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		initDialog();
	}

	public GUIFeedbackDialog(Context context, int theme) {
		super(context, theme);
		initDialog();
	}

	public GUIFeedbackDialog(Context context) {
		super(context);
		initDialog();
	}

	/**
	 * 
	 */
	private void initDialog() {
		GKIMLog.lf(getContext(), 1, TAG + "=>initDialog");
		Window w = getWindow();
		w.requestFeature(Window.FEATURE_NO_TITLE);
		w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		setContentView(R.layout.dlg_feedback);

		mEdEmail = (EditText) findViewById(R.id.ed_feedback_name);
		mEdContent = (EditText) findViewById(R.id.ed_feedback_content);
		mRating = (RatingBar) findViewById(R.id.ratebar_feedback_rate);
		mIvSend = (ImageView) findViewById(R.id.img_feedback_send);
		if (mIvSend != null) {
			mIvSend.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mIvSend.setEnabled(false);
					sendFeedback();
				}
			});
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mProgressDialog = new ProgressDialog(getContext());
		mProgressDialog.setTitle(R.string.please_wait);
	}

	@Override
	protected void onStop() {
		if (mIvSend != null) {
			mIvSend.setOnClickListener(null);
		}
		super.onStop();
	}

	/**
	 * 
	 */
	protected void sendFeedback() {
		String email = mEdEmail.getText().toString();
		String content = mEdContent.getText().toString();
		int rating = Math.round(mRating.getRating());
		if (!validateEmailFormat(email)) {
			UIUtils.showToast(getContext(), getContext().getResources()
					.getString(R.string.dlg_user_feedback_unmatch_email));
			mIvSend.setEnabled(true);
			return;
		}
		if (content == null || content.length() == 0) {
			UIUtils.showToast(getContext(), getContext().getResources()
					.getString(R.string.dlg_user_feedback_empty_content));
			mIvSend.setEnabled(true);
			return;
		}
		DataDownloader feedbacksender = new DataDownloader(mOnDownloadComplete);
		String uid = TNPreferenceManager.getUserId();
		feedbacksender.addDownload(RequestDataFactory.makeUserFeedbackRequest(
				uid, email, content, String.valueOf(rating)));
		GKIMLog.lf(getContext(), 1, TAG + "=>sendFeedback(): " + rating + ", "
				+ email + ", " + content);

	}

	protected boolean validateEmailFormat(String strEmail) {
		if (UIUtils.hasFroyo()) {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(strEmail)
					.matches();
		} else {
			boolean isValid = false;
			// String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
			String expression = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
			CharSequence inputStr = strEmail;

			Pattern pattern = Pattern.compile(expression,
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(inputStr);
			if (matcher.matches()) {
				isValid = true;
			}
			return isValid;
		}
	}
}
