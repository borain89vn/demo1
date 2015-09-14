/**
 * File: GUIAccountDialog.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 09-01-2013
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.SectionActivity;
import com.gkxim.android.thanhniennews.StoryDetailActivity;
import com.gkxim.android.thanhniennews.StoryDetailFragmentActivity;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.models.UserAccount;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.social.FacebookShare;
import com.gkxim.android.thanhniennews.social.SocialHelper;
import com.gkxim.android.thanhniennews.social.SocialShare;
import com.gkxim.android.thanhniennews.spring.StoryDetailSpringFragmentActivity;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Timon Trinh
 */
public class GUIAccountDialog extends Dialog {

	private static final String TAG = "GUIAccountDialog";
	private static final int RESULT_CODE_SUCCESS = 0;
	private Typeface mDefaultTF;
	private int mLayoutId = -1;
	private UserAccount mUserAccount = null;
	private EditText mETEmail;
	private EditText mETPass;
	private EditText mETPass2;
	private ProgressDialog mProgressDialog;
	private String mSubmitPass;
	private UserAccount mOldUA;
	private Context mContext;
	private GUIListMenuVideoListView mViewToken;

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			GKIMLog.lf(null, 0, TAG + "=>onClick: "
					+ v.getClass().getSimpleName());
			switch (v.getId()) {
			case R.id.tv_dlg_account_login_register:
				switchLayoutToRegisterDialog();
				break;
			case R.id.tv_dlg_account_login_text_forgot_password:
				switchLayoutForgotPasswordDialog();
				break;
			case R.id.id_bt_close_dialog:
				dismiss();
				break;

			case R.id.id_dlg_create_back:
				inflateLayout(R.layout.dlg_account_login);
				break;
			case R.id.btn_dlg_account_login_dologin:
				if (mLayoutId == R.layout.dlg_account_login) {
					EditText edEmail = (EditText) findViewById(R.id.ed_dlg_account_login_email);
					EditText edPass = (EditText) findViewById(R.id.ed_dlg_account_password);
					String strEmail = edEmail.getText().toString();
					mSubmitPass = edPass.getText().toString();
					if (!validateFieldsEmailPass(strEmail, mSubmitPass)) {
						break;
					}
					if (mProgressDialog != null && !mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}
					RequestData req = RequestDataFactory
							.makeAccountLoginRequest(strEmail, mSubmitPass);
					mTNAccountDownloader.addDownload(req);
				}
				break;

			case R.id.btn_dlg_account_forgot_pass_send:
				if (mLayoutId == R.layout.dlg_account_forgot_password) {
					String strEmail = mETEmail.getText().toString();
					// if (!validateEmailFormat(strEmail)) {
					// break;
					// }
					if (mProgressDialog != null && !mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}
					RequestData req = RequestDataFactory
							.makeAccountForgotPasswordRequest(strEmail);
					mTNAccountDownloader.addDownload(req);
				}
				break;

			case R.id.tv_dlg_account_register_linkfb:
				setCanceledOnTouchOutside(false);
				if (mLayoutId == R.layout.dlg_account_login) {
					GKIMLog.log("Call login facebook");
                    if(StoryDetailSpringFragmentActivity.mActivity != null){
                        SocialHelper helperLogout = SocialHelper.getInstance(
                                StoryDetailSpringFragmentActivity.mActivity, 1);
                        // FIXME: add callback for listening result.
                        helperLogout.logout();
                        helperLogout = null;
                        TNPreferenceManager.setLoginFBState(1);
                        SocialHelper helper = SocialHelper.getInstance(StoryDetailSpringFragmentActivity.mActivity, 1);
                        // Call login and shoult be show dialog to enter facebook
                        // account.
                        helper.loginApp(createMessageToPostStory());
                        // dismiss();
                    }else{
                        SocialHelper helperLogout = SocialHelper.getInstance(
                                mContext, 1);
                        // FIXME: add callback for listening result.
                        helperLogout.logout();
                        helperLogout = null;
                        TNPreferenceManager.setLoginFBState(1);
                        SocialHelper helper = SocialHelper.getInstance(mContext, 1);
                        // Call login and shoult be show dialog to enter facebook
                        // account.
                        helper.loginApp(createMessageToPostStory());
                        // dismiss();
                    }

				}
				break;
			case R.id.btn_dlg_account_register_doregister:
				if (mLayoutId == R.layout.dlg_account_register) {
					EditText edName = (EditText) findViewById(R.id.ed_dlg_account_register_name);
					EditText edLastName = (EditText) findViewById(R.id.ed_dlg_account_register_lastname);
					String strEmail = mETEmail.getText().toString();
					String strPass = mETPass.getText().toString();
					String strPass2 = mETPass2.getText().toString();
					String strName = edName.getText().toString();
					String strLastName = edLastName.getText().toString();

					if (!strPass2.equals(strPass)) {
						UIUtils.showToast(
								getContext(),
								getContext()
										.getResources()
										.getString(
												R.string.dlg_account_error_register_unmatchpass));
						break;
					}

					if (mProgressDialog != null && !mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}
					mSubmitPass = strPass;
					RequestData req = RequestDataFactory
							.makeAccountRegisterRequest(strEmail, strPass,
									strName, strLastName);
					mTNAccountDownloader.addDownload(req);
				}
				break;
			case R.id.btn_dlg_account_editting_save:
				if (mLayoutId == R.layout.dlg_account_editting
						&& mOldUA != null) {
					EditText oldPass = (EditText) findViewById(R.id.ed_dlg_account_editting_oldpass);
					EditText edName = (EditText) findViewById(R.id.ed_dlg_account_editting_name);
					EditText edLastName = (EditText) findViewById(R.id.ed_dlg_account_editting_lastname);

					String strNewPass = null;
					String strOldPass = oldPass.getText().toString();
					String strLastName = edLastName.getText().toString();
					String strName = edName.getText().toString();
					String strPass = mETPass.getText().toString();
					String strPass2 = mETPass2.getText().toString();

					if (strOldPass.length() == 0) {
						UIUtils.showToast(
								getContext(),
								getContext()
										.getResources()
										.getString(
												R.string.dlg_account_editting_error_forgot_oldpass));
						break;
					}

					if (!strPass2.equals(strPass)) {
						UIUtils.showToast(
								getContext(),
								getContext()
										.getResources()
										.getString(
												R.string.dlg_account_error_register_unmatchpass));
						break;
					}

					if (strPass.length() > 0) {
						strNewPass = strPass;

						if (!validatePassFormat(strPass)) {
							UIUtils.showToast(
									getContext(),
									getContext().getResources().getString(
											R.string.errcode_305));
							break;
						}
					}

					if (strName.length() == 0 || strLastName.length() == 0) {
						UIUtils.showToast(
								getContext(),
								getContext().getResources().getString(
										R.string.dlg_account_error_forgot_name));
						break;
					}

					if (!validateFieldsName(strName, strLastName)) {
						break;
					}

					if (mProgressDialog != null && !mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}
					if (strNewPass != null) {
						mSubmitPass = strNewPass;
					}
					RequestData req = RequestDataFactory
							.makeAccountUpdateRequest(
									String.valueOf(mOldUA.getUserId()),
									strOldPass, strName, strLastName,
									strNewPass);
					mTNAccountDownloader.addDownload(req);
				}
				break;
			case R.id.imv_dlg_account_register_avartar:
				if (mLayoutId == R.layout.dlg_account_register) {
					// UIUtils.showToast(getContext(), "avartar");
				}
				break;
			default:
				break;
			}
		}
	};

	private DataDownloader mTNAccountDownloader = new DataDownloader(
			new DataDownloader.OnDownloadCompletedListener() {
				@Override
				public void onCompleted(Object key, String result) {
					GKIMLog.lf(null, 0,
							TAG + "=>onCompleted: " + key.toString());
					if (mProgressDialog != null && mProgressDialog.isShowing()
							&& mTNAccountDownloader != null) {
						mProgressDialog.dismiss();
					}
					RequestData contentKey = (RequestData) key;
					int type = contentKey.type;
					if (result == null || result.length() <= 0) {
						if (RequestDataFactory
								.isLoginRequest((RequestData) key)) {
							// Login failed.
							UIUtils.showToast(null, getContext().getResources()
									.getString(R.string.dlg_login_failed));
						} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_UPDATE) {
							UIUtils.showToast(null, getContext().getResources()
									.getString(R.string.dlg_update_failed));
						}
						return;
					}

					Gson gson = new GsonBuilder().registerTypeAdapter(
							GenericResponse.class,
							new GenericResponse.GenericResponseConverter())
							.create();
					GenericResponse gr = gson.fromJson(result,
							GenericResponse.class);

					if (gr != null && gr.isSucceed() && gr.isHasData()) {
						UserAccount ua = new Gson().fromJson(gr.getData(),
								UserAccount.class);
						if (ua != null) {
							mUserAccount = ua;
							if (RequestDataFactory
									.isLoginRequest((RequestData) key)) {
								UIUtils.showToast(
										null,
										getContext().getResources().getString(
												R.string.dlg_login_succeed));
								notifyLoggingStateChanged();
							} else if (RequestDataFactory
									.isForgotPasswordRequest((RequestData) key)) {
								UIUtils.showToast(
										null,
										getContext()
												.getResources()
												.getString(
														R.string.dlg_forgot_password_succeed)
												+ mETEmail.getText().toString());
								notifyLoggingStateChanged();
							} else if (RequestDataFactory
									.isRegisterRequest((RequestData) key)) {
								HashMap<String, String> map = new HashMap<String, String>();
								map.put(TNPreferenceManager.EVENT_KEY_UID,
										ua.getUserId() + "");
								// FlurryAgent.onEvent(TNPreferenceManager.EVENT_USER_REGISTERED,
								// map);
								Tracking.sendEvent(
										TNPreferenceManager.EVENT_USER_REGISTERED,
										map);
								UIUtils.showToast(
										null,
										getContext().getResources().getString(
												R.string.dlg_register_succeed));
							} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_UPDATE) {
								UIUtils.showToast(
										null,
										getContext().getResources().getString(
												R.string.dlg_update_succeed));
							}
							// save the state to SharedPreference
							// ua.setPass(mSubmitPass);

							// Checking has login Facebook to app
							if (TNPreferenceManager.getLoginFBState() == 1) {
								TNPreferenceManager.setLoginFBState(2);
							}

							String strUA = (new Gson()).toJson(ua);
							GKIMLog.lf(null, 0, TAG + "=>login/reg: " + strUA);
							TNPreferenceManager.setUserId(String.valueOf(ua
									.getUserId()));
							TNPreferenceManager.setUserInfo(strUA);

							GUIAccountDialog.this.dismiss();
						}
					} else {
						if (RequestDataFactory
								.isRegisterRequest((RequestData) key)) {
							// Register failed.
							String strResult = getContext().getResources()
									.getString(R.string.dlg_register_failed);
							if (gr.resultMsg != null) {
								strResult = gr.resultMsg;
							}
							UIUtils.showToast(null, strResult);
							return;
						} else if (RequestDataFactory
								.isForgotPasswordRequest((RequestData) key)) {
							String strResult = getContext().getResources()
									.getString(
											R.string.dlg_forgotpassword_failed);
							if (gr.resultCode == RESULT_CODE_SUCCESS) {
								// Forgot Password failed.
								strResult = getContext()
										.getResources()
										.getString(
												R.string.dlg_forgot_password_succeed)
										+ " " + mETEmail.getText().toString();
								GUIAccountDialog.this.dismiss();
							} else {
								if (gr.resultMsg != null) {
									strResult = gr.resultMsg;
								}
							}

							UIUtils.showToast(null, strResult);
							return;
						} else {
							UIUtils.showToast(getContext(), TNPreferenceManager
									.getErrorMessageFromCode(gr.resultCode));
						}
					}
				}

				@Override
				public String doInBackgroundDebug(Object... params) {
					return null;
				}
			});

	/**
	 * 09-01-2013
	 */
	public GUIAccountDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		initDialog();
	}

	protected boolean validateFieldsName(String strName, String strLastName) {
		if (strName == null || strName.length() <= 0 || strLastName == null
				|| strLastName.length() <= 0) {
			UIUtils.showToast(
					null,
					getContext().getResources().getString(
							R.string.dlg_account_error_forgot_name));
			return false;
		}
		int nlen = strName.length();
		int llen = strLastName.length();
		boolean bname = (((nlen > 0) && (nlen < 24)) ? true : false);
		boolean blname = (((llen > 0) && (llen < 24)) ? true : false);
		if (!bname || !blname) {
			UIUtils.showToast(
					null,
					getContext().getResources().getString(
							R.string.dlg_account_error_register_toolongname));
			return false;
		}
		return true;
	}

	protected boolean validateFieldsEmailPass(String strEmail, String strPass) {
		if (strEmail == null || strEmail.length() <= 0 || strPass == null
				|| strPass.length() <= 0) {
			UIUtils.showToast(
					null,
					getContext().getResources().getString(
							R.string.dlg_account_error_forgot_emailpass));
			return false;
		}
		if (!validateEmailFormat(strEmail)) {
			UIUtils.showToast(getContext(), getContext().getResources()
					.getString(R.string.errcode_306));
			return false;
		}
		if (!validatePassFormat(strPass)) {
			// UIUtils.showToast(getContext(), getContext().getResources()
			// .getString(R.string.errcode_305));
			(new AlertDialog.Builder(this.getContext()))
					.setTitle(R.string.errcode_305_title)
					.setMessage(R.string.errcode_305)
					.setPositiveButton("OK", null).create().show();
			return false;
		}
		return true;
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

	protected boolean validatePassFormat(String strPass) {
		boolean pattern = true;
		pattern = strPass.matches("^[a-zA-Z0-9]{6,20}$");
		return pattern;
	}

	/**
	 * 09-01-2013
	 */
	public GUIAccountDialog(Context context, int theme) {
		super(context, theme);
		initDialog();
	}

	/**
	 * 09-01-2013
	 */
	public GUIAccountDialog(Context context) {
		super(context);
		mContext = context;
		initDialog();
	}

	public void setLayoutId(int resId) {
		GKIMLog.lf(getContext(), 0, TAG + "=>setLayoutId to: " + resId);
		mLayoutId = resId;
	}

	public void setOldUserAccount(UserAccount au) {
		if (au != null) {
			mOldUA = au;
			mLayoutId = R.layout.dlg_account_editting;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Dialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GKIMLog.lf(getContext(), 0, TAG + "=>onCreate.");
		mProgressDialog = new ProgressDialog(getContext());
		mProgressDialog.setTitle(R.string.please_wait);
		inflateLayout();
	}

	public void StopDownloader() {
		if (mTNAccountDownloader != null) {
			mTNAccountDownloader.setExitTasksEarly(true);
			mTNAccountDownloader = null;
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		super.onStop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Dialog#onSaveInstanceState()
	 */
	@Override
	public Bundle onSaveInstanceState() {
		GKIMLog.lf(getContext(), 0, TAG + "=>onSaveInstanceState.");
		return super.onSaveInstanceState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Dialog#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		GKIMLog.lf(getContext(), 0, TAG + "=>onRestoreInstanceState.");
		super.onRestoreInstanceState(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Dialog#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		GKIMLog.lf(getContext(), 0, TAG + "=>onBackPressed.");
		if (mETEmail != null) {
			mETEmail.setOnFocusChangeListener(null);
		}
		super.onBackPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Dialog#onAttachedToWindow()
	 */
	@Override
	public void onAttachedToWindow() {
		GKIMLog.lf(getContext(), 0, TAG + "=>onAttachedToWindow.");
		super.onAttachedToWindow();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Dialog#onDetachedFromWindow()
	 */
	@Override
	public void onDetachedFromWindow() {
		GKIMLog.lf(getContext(), 0, TAG + "=>onDetachedFromWindow.");
		super.onDetachedFromWindow();
	}

	private void initDialog() {
		GKIMLog.lf(getContext(), 0, TAG + "=>initDialog.");
		setCanceledOnTouchOutside(true);
		Window w = getWindow();

		w.requestFeature(Window.FEATURE_NO_TITLE);
		w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		ColorDrawable color = new ColorDrawable(Color.argb(0, 255, 255, 255));
		w.setBackgroundDrawable(color);

		mDefaultTF = TNPreferenceManager.getTNTypeface();
		// inflateLayout(R.layout.dlg_account_login);
		if (mLayoutId == -1) {
			mLayoutId = R.layout.dlg_account_login;
		}
	}

	private void switchLayoutToRegisterDialog() {
		// Note: load R.layout.dlg_account_register
		if (mETEmail != null) {
			mETEmail.setOnFocusChangeListener(null);
		}
		inflateLayout(R.layout.dlg_account_register);
	}

	private void switchLayoutForgotPasswordDialog() {
		// Note: load R.layout.dlg_account_register
		if (mETEmail != null) {
			mETEmail.setOnFocusChangeListener(null);
		}
		inflateLayout(R.layout.dlg_account_forgot_password);
	}

	private void inflateLayout(int layoutId) {
		if (mLayoutId != layoutId) {
			mLayoutId = layoutId;
		}
		inflateLayout();
	}

	private void inflateLayout() {
		setContentView(mLayoutId);
		if (mLayoutId == R.layout.dlg_account_login) {
			TextView tvLinkFB = (TextView) findViewById(R.id.tv_dlg_account_register_linkfb);
			tvLinkFB.setOnClickListener(mOnClickListener);
			tvLinkFB.setTypeface(mDefaultTF, Typeface.NORMAL);

			TextView tvTitle = (TextView) findViewById(R.id.tv_dlg_account_login_title);
			tvTitle.setTypeface(mDefaultTF, Typeface.NORMAL);
			tvTitle = (TextView) findViewById(R.id.tv_dlg_account_login_text_or);
			tvTitle.setTypeface(mDefaultTF);

			TextView tvRegister = (TextView) findViewById(R.id.tv_dlg_account_login_register);
			tvRegister.setOnClickListener(mOnClickListener);
			tvRegister.setTypeface(mDefaultTF, Typeface.NORMAL);

			TextView tvForgotPass = (TextView) findViewById(R.id.tv_dlg_account_login_text_forgot_password);
			tvForgotPass.setOnClickListener(mOnClickListener);
			tvForgotPass.setTypeface(mDefaultTF, Typeface.NORMAL);

			Button btnLogin = (Button) findViewById(R.id.btn_dlg_account_login_dologin);
			btnLogin.setTypeface(TNPreferenceManager.getTNTypefaceBOLD());
			btnLogin.setOnClickListener(mOnClickListener);

			mETEmail = (EditText) findViewById(R.id.ed_dlg_account_login_email);
			mETEmail.setTypeface(mDefaultTF, Typeface.NORMAL);
			mETPass = (EditText) findViewById(R.id.ed_dlg_account_password);
			mETPass.setTypeface(mDefaultTF, Typeface.NORMAL);
		} else if (mLayoutId == R.layout.dlg_account_forgot_password) {
			TextView tvTitle = (TextView) findViewById(R.id.tv_dlg_account_forgot_pass_title);
			tvTitle.setTypeface(mDefaultTF, Typeface.NORMAL);

			Button btnLogin = (Button) findViewById(R.id.btn_dlg_account_forgot_pass_send);
			btnLogin.setTypeface(TNPreferenceManager.getTNTypefaceBOLD());
			btnLogin.setOnClickListener(mOnClickListener);

			mETEmail = (EditText) findViewById(R.id.ed_dlg_account_forgot_pass_email);
			mETEmail.setTypeface(mDefaultTF, Typeface.NORMAL);
		} else if (mLayoutId == R.layout.dlg_account_register) {
			ImageView img_btBack = (ImageView) findViewById(R.id.id_dlg_create_back);
			if (img_btBack != null) {
				img_btBack.setOnClickListener(mOnClickListener);
			}
			TextView tvTitle = (TextView) findViewById(R.id.tv_dlg_account_register_title);
			tvTitle.setTypeface(mDefaultTF, Typeface.NORMAL);

			// Nam.nguyen close
			// TextView tvLinkFB = (TextView)
			// findViewById(R.id.tv_dlg_account_register_linkfb);
			// tvLinkFB.setOnClickListener(mOnClickListener);
			// tvLinkFB.setTypeface(mDefaultTF, Typeface.NORMAL);
			Button btnRegister = (Button) findViewById(R.id.btn_dlg_account_register_doregister);
			btnRegister.setOnClickListener(mOnClickListener);
			btnRegister.setTypeface(TNPreferenceManager.getTNTypefaceBOLD());
			ImageView imvAvatar = (ImageView) findViewById(R.id.imv_dlg_account_register_avartar);
			imvAvatar.setOnClickListener(mOnClickListener);

			mETEmail = (EditText) findViewById(R.id.ed_dlg_account_register_email);
			mETEmail.setTypeface(mDefaultTF, Typeface.NORMAL);
			mETPass = (EditText) findViewById(R.id.ed_dlg_account_register_password1);
			mETPass.setTypeface(mDefaultTF, Typeface.NORMAL);
			mETPass2 = (EditText) findViewById(R.id.ed_dlg_account_register_password2);
			mETPass2.setTypeface(mDefaultTF, Typeface.NORMAL);
			EditText edName = (EditText) findViewById(R.id.ed_dlg_account_register_name);
			edName.setTypeface(mDefaultTF, Typeface.NORMAL);
			EditText edLastname = (EditText) findViewById(R.id.ed_dlg_account_register_lastname);
			edLastname.setTypeface(mDefaultTF, Typeface.NORMAL);
		} else if (mLayoutId == R.layout.dlg_account_editting) {
			// Nam.nguyen
			// TextView tvLinkFB = (TextView)
			// findViewById(R.id.tv_dlg_account_editting_linkfb);
			// tvLinkFB.setTypeface(mDefaultTF, Typeface.NORMAL);
			TextView tvTitle = (TextView) findViewById(R.id.tv_dlg_account_editting_title);
			tvTitle.setTypeface(mDefaultTF, Typeface.NORMAL);
			EditText oldpass = (EditText) findViewById(R.id.ed_dlg_account_editting_oldpass);
			oldpass.setTypeface(mDefaultTF, Typeface.NORMAL);
			mETPass = (EditText) findViewById(R.id.ed_dlg_account_editting_password1);
			mETPass.setTypeface(mDefaultTF, Typeface.NORMAL);
			mETPass2 = (EditText) findViewById(R.id.ed_dlg_account_editting_password2);
			mETPass2.setTypeface(mDefaultTF, Typeface.NORMAL);

			EditText edName = (EditText) findViewById(R.id.ed_dlg_account_editting_name);
			edName.setText(mOldUA.getFName());
			edName.setTypeface(mDefaultTF, Typeface.NORMAL);
			EditText edLastname = (EditText) findViewById(R.id.ed_dlg_account_editting_lastname);
			edLastname.setText(mOldUA.getLName());
			edLastname.setTypeface(mDefaultTF, Typeface.NORMAL);
			Button btnUpdate = (Button) findViewById(R.id.btn_dlg_account_editting_save);
			btnUpdate.setOnClickListener(mOnClickListener);
			btnUpdate.setTypeface(TNPreferenceManager.getTNTypefaceBOLD());
		}
		ImageView img_bt = (ImageView) findViewById(R.id.id_bt_close_dialog);
		if (img_bt != null) {
			img_bt.setOnClickListener(mOnClickListener);
		}
	}

	@SuppressWarnings("unused")
	private void setViewOnClickListener(View view, View.OnClickListener l) {
		if (view != null) {
			view.setOnClickListener(l);
		}
	}

	public UserAccount getUserAccount() {
		return mUserAccount;
	}

	private void notifyLoggingStateChanged() {
		Context context = mContext;
		Handler hld = null;
		if (context != null) {
			if (context instanceof SectionActivity) {
				hld = ((SectionActivity) context).getHandler();
			} else if (context instanceof StoryDetailActivity) {
				hld = ((StoryDetailActivity) context).getHandler();
			} else if (context instanceof StoryDetailFragmentActivity) {
				hld = ((StoryDetailFragmentActivity) context).getHandler();
			}

			if (hld != null) {
				hld.sendEmptyMessage(TNPreferenceManager.HANDLER_MSG_HAS_LOGGIN_CHANGED);
			}
		}
	}

	public void setUserFacebook(UserAccount user) {
		GKIMLog.log("setUserFacebook id:" + user.getUserId() + " email:"
				+ user.getEmail() + " fName:" + user.getFName() + " lName:"
				+ user.getLName());
		RequestData req = RequestDataFactory.makeFBAccountLoginRequest(user);
		// This invoke when the Dialog has been close or dismissed. In this
		// context, only handle for the register by FB
		DataDownloader loginByFB = new DataDownloader(
				new OnDownloadCompletedListener() {

					@Override
					public void onCompleted(Object key, String result) {
						GKIMLog.lf(null, 0,
								TAG + "=>onCompleted: " + key.toString());
						if (result == null || result.length() <= 0) {
							// Login failed.
							UIUtils.showToast(null, getContext().getResources()
									.getString(R.string.dlg_login_failed));
							return;
						}

						try {
							Gson gson = new GsonBuilder()
									.registerTypeAdapter(
											GenericResponse.class,
											new GenericResponse.GenericResponseConverter())
									.create();
							GenericResponse gr = gson.fromJson(result,
									GenericResponse.class);

							if (gr != null && gr.isSucceed() && gr.isHasData()) {
								UserAccount ua = new Gson().fromJson(
										gr.getData(), UserAccount.class);
								if (ua != null) {
									mUserAccount = ua;
									if (RequestDataFactory
											.isLoginRequest((RequestData) key)) {
										// Note: tracking for login by Facebook
										HashMap<String, String> map = new HashMap<String, String>();
										map.put(TNPreferenceManager.EVENT_USER_FBLOGIN,
												ua.getUserId() + "");
										Tracking.sendEvent(
												TNPreferenceManager.EVENT_USER_FBLOGIN,
												map);
										notifyLoggingStateChanged();
									}
									// Checking has login Facebook to app
									if (TNPreferenceManager.getLoginFBState() == 1) {
										TNPreferenceManager.setLoginFBState(2);
									}

									String strUA = (new Gson()).toJson(ua);
									GKIMLog.lf(null, 0, TAG
											+ "=>login information: " + strUA);
									TNPreferenceManager.setUserId(String
											.valueOf(ua.getUserId()));
									TNPreferenceManager.setUserInfo(strUA);
									if (GUIAccountDialog.this.isShowing()) {
										GUIAccountDialog.this.dismiss();
									} else {
										if(mViewToken != null){
											mViewToken.updateAccountView();
										}
									}
								}
							}
						} catch (Exception e) {
							GKIMLog.lf(
									getContext(),
									4,
									TAG + "=>setUserFacebook failed: "
											+ e.getMessage());
						}
					}

					@Override
					public String doInBackgroundDebug(Object... params) {
						return null;
					}
				});

		loginByFB.addDownload(req);
	}

	public void handleActivityResult(int reqcode, int rescode, Intent data) {
		GKIMLog.lf(getContext(), 0, TAG + "=>handleActivityResult.");
		SocialShare provider = SocialHelper.getLastInstance().getSNSInstance();
		if (provider instanceof FacebookShare) {
			FacebookShare fs = (FacebookShare) provider;
			fs.handlingActivityForResult(reqcode, rescode, data);
		}
	}

	public Message createMessageToPostStory() {
		Message msg = new Message();
		msg.what = 1;
		msg.setTarget(new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				GKIMLog.lf(getContext(), 0, TAG + "=>handleMessage.");
				setUserFacebook((UserAccount) msg.obj);
				return false;
			}
		}));
		return msg;
	}

	public void setViewToken(GUIListMenuVideoListView guiListMenuVideoListView) {
		mViewToken = guiListMenuVideoListView;
	}
}
