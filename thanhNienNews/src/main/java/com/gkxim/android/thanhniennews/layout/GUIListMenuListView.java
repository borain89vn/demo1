/**
 * File: GUIListMenuListView.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 30-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.*;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.models.UserAccount;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Timon Trinh
 */
public class GUIListMenuListView extends LinearLayout {

	protected static final String TAG = "GUIListMenuListView";
	private GUIListMenuHeader mHeader = null;
	private GUIListMenuFooter mFooter = null;
	private OnClickListener mOnClickListener = null;
	private ListView mList = null;
	private GUIMenuSettingDialog mMenuSettingDlg;
	private UserAccount mUserAccount;
	private GUIListMenuAdapter mGuiMenuListAdapter;
	private Button mBtnLogout;
	private boolean mTabletVersion = false;
	private GUIAccountDialog mDialog;
	private ProgressDialog mProgressDialog;
	// private DialogFragment mSupportDialog;
	private GUIPNSCheckDialog mPNSCheckDialog;
	private Dialog mPolicyDialog;

	private OnClickListener mOnClickHeaderAndFooter = new OnClickListener() {
		@Override
		public void onClick(View v) {
			GUIListMenuListView.this.onClick(v);
		}
	};
	private OnDismissListener mOnAccountDlgDismissListener = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface dialog) {
			if (dialog instanceof GUIAccountDialog) {
				UserAccount ua = ((GUIAccountDialog) dialog).getUserAccount();
				if (ua != null) {
					setDisplayUser(ua);
				}
			}
		}
	};

	public void CloseDialog() {
		if (mDialog != null) {
			if (mDialog.isShowing()) {
				mDialog.dismiss();
				mDialog.StopDownloader();
			}
			mDialog = null;
		}
	}

	/**
	 * 30-11-2012
	 */
	public GUIListMenuListView(Context context) {
		super(context);
		initListViewItems();
	}

	/**
	 * 30-11-2012
	 */
	public GUIListMenuListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initListViewItems();
	}

	@Override
	protected void onAnimationStart() {
		// TODO Auto-generated method stub
		boolean hasLoggedIn = TNPreferenceManager.checkLoggedIn();
		if (mFooter != null) {
			if (!hasLoggedIn) {
				mFooter.setAccountName(getResources().getString(
						R.string.dlg_login_button_login));
			} else {
				UserAccount ua = TNPreferenceManager.getUserInfo();
				if (ua != null) {
					setDisplayUser(ua);
				}
			}
		}
		super.onAnimationStart();
	}

	/**
	 * 30-11-2012
	 */
	@SuppressLint("NewApi")
	public GUIListMenuListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initListViewItems();
	}

	private void initListViewItems() {
		GKIMLog.lf(this.getContext(), 0, TAG
				+ "=>initListViewItems from context: "
				+ getContext().getClass().getSimpleName());
		mProgressDialog = new ProgressDialog(getContext());
		inflate(getContext(), R.layout.menu_list_layout, this);

		mHeader = (GUIListMenuHeader) findViewById(R.id.menulist_header);
		if (mHeader != null) {
			mHeader.setOnClickListener(mOnClickHeaderAndFooter);
		}

		boolean hasLoggedIn = TNPreferenceManager.checkLoggedIn();
		mFooter = (GUIListMenuFooter) findViewById(R.id.menulist_footer);
		if (mFooter != null) {
			mFooter.setOnClickListener(mOnClickHeaderAndFooter);
			if (!hasLoggedIn) {
				mFooter.setAccountName(getResources().getString(
						R.string.dlg_login_button_login));
			} else {
				UserAccount ua = TNPreferenceManager.getUserInfo();
				if (ua != null) {
					setDisplayUser(ua);
				}
			}
		}

		mList = (ListView) findViewById(R.id.menu_in_list);
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				GKIMLog.lf(null, 0, TAG + "=>onItemClick: " + position + ", "
						+ id + ", view: " + view);
				view.setId((int) id);
				onClick(view);
			}
		});
		mGuiMenuListAdapter = new GUIListMenuAdapter(getContext());
        if (GUIListMenuAdapter.HAS_ENABLE_WEBEVENT) {
            GKIMLog.l(0, TAG + "=>has enabled Web's Event item");
            mGuiMenuListAdapter.addMenuSection(TNPreferenceManager.EXTRAVALUE_ID_WORLDCUP_BANNER,
                    "WorldCup 2014", Color.WHITE, getResources().getDrawable(R.drawable.ic_launcher), 0);
        }
        String[] secIds = TNPreferenceManager.getSectionIDs();
        if (secIds != null && secIds.length > 0) {
			int colorTextHover = getResources().getColor(
					R.color.menu_text_hover);
			int textLeftPadding = (int) getResources().getDimension(
					R.dimen.menu_list_item_text_lefticon_padding);
			for (String secId : secIds) {
				String textTitle = TNPreferenceManager
						.getSectionTitleFromPref(secId);
				mGuiMenuListAdapter.addMenuSection(secId, textTitle,
						colorTextHover,
						TNPreferenceManager.getSectionIcon(secId),
						textLeftPadding);
			}
		}
		setAdapter(mGuiMenuListAdapter);
		mTabletVersion = getResources().getBoolean(R.bool.istablet);
		if (mTabletVersion) {
			View v = findViewById(R.id.imv_menulist_bg);
			v.setOnClickListener(mOnClickHeaderAndFooter);
		}
	}

	public void onClick(View v) {
		GKIMLog.lf(getContext(), 0, TAG + "=>onClick: " + v.getId());
		boolean hasProcessed = false;
		switch (v.getId()) {
		case R.id.btn_menu_setting_feedback:
			dissmisMenuSettingDialog();
			showFeedBackDialog();
			break;
		case R.id.btn_menu_setting_support:
			dissmisMenuSettingDialog();
			hasProcessed = showSupportActivity();
			break;
		case R.id.btn_menu_setting_policy:
			dissmisMenuSettingDialog();
			hasProcessed = showPolicyActivity();
			break;
		case R.id.btn_menu_setting_logout:
			dissmisMenuSettingDialog();
			hasProcessed = processLogout();
			break;
		case R.id.menu_list_footer_name:
			hasProcessed = showAccountSetting();
			break;
		case R.id.menu_list_footer_setting:
			hasProcessed = showMenuSetting();
			break;
		case R.id.menu_list_header_ivmyhome:
			if (!TNPreferenceManager.checkLoggedIn()) {
				UIUtils.showToast(getContext(),
						getResources().getString(R.string.request_for_login));
				showLoginDialog();
				hasProcessed = true;
			}
			break;
		case R.id.menu_list_header_ivstored:
//			if (!TNPreferenceManager.checkLoggedIn()) {
//				UIUtils.showToast(getContext(),
//						getResources().getString(R.string.request_for_login));
//				showLoginDialog();
//				hasProcessed = true;
//			}
			break;
		case R.id.btn_menu_setting_pnscheck:
			dissmisMenuSettingDialog();
			hasProcessed = showPNSCheckSetting();
			break;
        case R.id.menu_in_list:
            //Note: this case is handling only for specified EVENT/CAMPAIGN from TNM
            //ex: Xuan 2014, WorldCup 2014
            if (v instanceof LinearLayout) {
                GUIListMenuAdapter.ViewHolder menuItem = (com.gkxim.android.thanhniennews.layout.GUIListMenuAdapter.ViewHolder) v
                        .getTag();
                if (menuItem != null) {
                    if (TNPreferenceManager.EXTRAVALUE_ID_WORLDCUP_BANNER.equals(menuItem.id)) {
                    	//NOTE: handling for Worldcup Banner in Main Menu list
                        String url = v.getResources().getString(R.string.webevent_worldcup2014_url);
                        GKIMLog.lf(v.getContext(), 1, TAG + "=>Open Worldcup 2014 campaign: " + url);
                        if (!TextUtils.isEmpty(url)) {
                            try {
//                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                Intent i = new Intent(getContext(), EventFullWebViewWorldCupActivity.class);
                                i.putExtra(TNPreferenceManager.EXTRAKEY_EVENTWEBVIEW_URL, url);
                                getContext().startActivity(i);
                                Tracking.sendEvent(TNPreferenceManager.EVENT_WORLDCUP_MENU_OPENED, null);
                                hasProcessed = true;
                            } catch (Exception e) {
                                GKIMLog.l(4, TAG + "Open Worldcup 2014 campaign exception: " + url);
                            }
                        }
                    } else if (TNPreferenceManager.getMediaSectionId().equalsIgnoreCase(menuItem.id)) {
                    	//NOTE: handling for special section - VIDEO
                    	GKIMLog.l(1, TAG + "=>Openning VIDEO section");
                    	hasProcessed = true;
                    	startVideoSectionActivity(menuItem.id);
                    }
                }
            }
            break;
		default:
			break;
		}
		if (mOnClickListener != null && !hasProcessed) {
			mOnClickListener.onClick(v);
		}
	}

	private boolean processLogout() {
		if (mUserAccount == null) {
			return false;
		}
		// Update state login facebook
		TNPreferenceManager.setLoginFBState(0);
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		Editor ed = sp.edit();
		ed.putString("pref_keyname_account_userid", "");
		ed.putString("pref_keyname_account_userinfo", "");
		ed.commit();
		notifyLoggingStateChanged();
		if (mFooter != null) {
			mFooter.setAccountName(getResources().getString(
					R.string.dlg_login_button_login));
		}
		if (mBtnLogout != null) {
			mBtnLogout.setVisibility(GONE);
		}
		return true;
	}

	private void notifyLoggingStateChanged() {
		Context context = getContext();
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

	private boolean showAccountSetting() {
		GKIMLog.log("Nam.Nguyen checkLoggedIn:"+ TNPreferenceManager.checkLoggedIn() + " mUserAccount:"+mUserAccount + " iFBLogin:"+ TNPreferenceManager.getLoginFBState());
		if (TNPreferenceManager.checkLoggedIn() && mUserAccount != null) {
			if (TNPreferenceManager.getLoginFBState() == 2) {
				UIUtils.showToast(getContext(), getContext().getResources()
						.getString(R.string.dlg_login_fb_edit));
			} else {
				showEdittingDialog();
			}
		} else {
			showLoginDialog();
		}
		return true;
	}

	private void showEdittingDialog() {
		if (mDialog != null) {
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			mDialog = null;
		}
		mDialog = new GUIAccountDialog(this.getContext());
		// dlg.setLayoutId(R.layout.dlg_account_editting);
		mDialog.setOldUserAccount(this.mUserAccount);
		mDialog.setOnDismissListener(mOnAccountDlgDismissListener);
		mDialog.show();
	}

	private void showLoginDialog() {
		if (mDialog != null) {
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			mDialog = null;
		}
		mDialog = new GUIAccountDialog(this.getContext());
		mDialog.setOnDismissListener(mOnAccountDlgDismissListener);
		mDialog.show();
	}

	private void showFeedBackDialog() {
		GUIFeedbackDialog dlg = new GUIFeedbackDialog(this.getContext());
		dlg.show();
	}

	private boolean showPolicyActivity() {
		if (mPolicyDialog == null) {
			mPolicyDialog = new Dialog(getContext());
			mPolicyDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			mPolicyDialog.setCanceledOnTouchOutside(true);
			mPolicyDialog.getWindow().setBackgroundDrawableResource(
					R.drawable.bg_dialog_login);
			DataDownloader downloader = new DataDownloader(
					new OnDownloadCompletedListener() {

						@Override
						public void onCompleted(Object key, String result) {
							GKIMLog.lf(getContext(), 0, TAG + "=>onCompleted: "
									+ key);
							if (result == null || result.length() <= 0) {
								return;
							}
							int type = ((RequestData) key).type;
							if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STATIC_CONTENT) {
								Gson gson = new GsonBuilder()
										.registerTypeAdapter(
												GenericResponse.class,
												new GenericResponse.GenericResponseConverter())
										.create();
								GenericResponse gres = gson.fromJson(result,
										GenericResponse.class);
								if (gres != null && gres.isSucceed()
										&& gres.isHasData()) {
									JsonElement je = gres.getDataElement();
									if (je != null) {
										JsonObject jo = je.getAsJsonObject();
										if (jo.has("content")) {
											showPolicyWebView(jo.get("content")
													.getAsString());
										}
									}

								} else {
									// NOTE: failed to fetch Policy data from
									// server.
									GKIMLog.lf(
											getContext(),
											0,
											TAG
													+ "=> can't fetch data from server: "
													+ result);
								}
							}
						}

						@Override
						public String doInBackgroundDebug(Object... params) {
							return null;
						}
					});
			if (mProgressDialog != null && !mProgressDialog.isShowing()) {
				mProgressDialog.show();
			}
			downloader.addDownload(RequestDataFactory
					.makeStaticContentRequest(1));
		} else {
			if (!mPolicyDialog.isShowing()) {
				mPolicyDialog.show();
			}
		}
		// mSupportDialog.setTitle(getContext().getString(R.string.menu_setting_policy));
		// NOTE: set content for Policy dialog.
		return true;
	}

	/**
	 * @param data
	 */
	protected void showPolicyWebView(String htmlData) {
		GKIMLog.lf(getContext(), 0, TAG + "=>showPolicyWebView.");
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		WebView wv = new WebView(getContext());
		mPolicyDialog.setContentView(wv);
		wv.loadDataWithBaseURL("file:///android_asset/",
				String.format("<html><body>%1s</body></html>", htmlData),
				"text/html", "utf-8", null);

		if (!mPolicyDialog.isShowing()) {
			mPolicyDialog.show();
		}
	}

	private boolean showPNSCheckSetting() {
		GKIMLog.lf(getContext(), 0, TAG + "=>showPNSCheckSetting.");
		if (mPNSCheckDialog == null) {
			mPNSCheckDialog = new GUIPNSCheckDialog(getContext());
		}
		if (!mPNSCheckDialog.isShowing()) {
			mPNSCheckDialog.updateCheckState();
			mPNSCheckDialog.show();
		}
		return true;
	}

	private boolean showSupportActivity() {
		// TODO: for tutorial
		return true;
	}

	public void setAdapter(ListAdapter adapter) {
		if (mList != null) {
			mList.setAdapter(adapter);
		}
	}

	protected void setDisplayUser(UserAccount ua) {
		mUserAccount = ua;
		if (mFooter != null) {
			mFooter.setAccountName(ua.getLName() + " " + ua.getFName());
		}
		if (mBtnLogout != null) {
			mBtnLogout.setVisibility(VISIBLE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.View#setOnClickListener(android.view.View.OnClickListener)
	 */
	@Override
	public void setOnClickListener(OnClickListener l) {
		mOnClickListener = l;
	}

	public boolean showMenuSetting() {
		// Note: dlg if not has HONEYCOMB, otherwise POPUPWindow or Fragment
		if (mMenuSettingDlg == null) {
			mMenuSettingDlg = new GUIMenuSettingDialog(this.getContext());
			mMenuSettingDlg.setOnShareClickListener(mOnClickHeaderAndFooter);
		}
		if (!mMenuSettingDlg.isShowing()) {
			mMenuSettingDlg.show();
		}
		return true;
	}

	public void dissmisMenuSettingDialog() {
		if (mMenuSettingDlg.isShowing()) {
			mMenuSettingDlg.dismiss();
		}
	}


	/**
	 * NEW FEATURE: special Video section
	 * Show Video's Home section 
	 * @param String ID of Video section.
	 */
	private void startVideoSectionActivity(String id) {
		if (TextUtils.isEmpty(id)) {
			return;
		}
		//NOTE: "id" should be the same with TNPreferenceManager.getMediaSectionId()
		Context context = this.getContext();
		Intent intent = new Intent(context, VideoSectionActivity.class);
		context.startActivity(intent);
	}

	
}
