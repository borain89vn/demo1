/**
 * 
 */
package com.gkxim.android.thanhniennews.spring;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.StoryDetailImageReviewActivity;
import com.gkxim.android.thanhniennews.layout.GUIHeader;
import com.gkxim.android.thanhniennews.layout.GUIListMenuAdapter;
import com.gkxim.android.thanhniennews.layout.GUIListMenuListView;
import com.gkxim.android.thanhniennews.layout.GUIStoryCommentDialog;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.social.SocialHelper;
import com.gkxim.android.thanhniennews.social.SocialShare;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;

/**
 * @author Timon
 * 
 */
public class StoryDetailSpringFragmentActivity extends FragmentActivity {
	private static final String TAG = StoryDetailSpringFragmentActivity.class
			.getSimpleName();
	private CSpring mSpring = new CSpring();
	private ArrayList<CSpringData> lstData = new ArrayList<CSpringData>();
	private StoryDetailSpringAdapter mSpringAdapter;
	private PullToRefreshListView mListView;
	private OnRefreshListener<ListView> mOnRefreshListener = getOnRefreshListener();
	// private AlertDialog mProcessingDialog;
	private GUIHeader mGuiHeader = null;
	private GUIStoryFooterSpring mGuiFooter = null;
	private GUIListMenuListView mGuiMenu = null;
	private GUIStoryShareDialogSpring mShareDialog;
	private GUIStoryTextModeDialogSpring mTextModeDialog = null;
	private GUIStoryCommentDialog mCommentDialog;
	private boolean mMenuShown = false;
	private boolean mTabletVersion = false;
	private Animation mOutAnimation2Left;
	private Animation mInAnimationFromRight;
	private Animation mInAnimationFromLeft;
	private Animation mOutAnimation2Right;

	private Intent mFinishData = null;
	private OnClickListener mOnClickListener = getOnClickListener();
	private Intent mIntentComment;
	private OnDismissListener mOnDialogDismissListener = getOnCommentDialogDissmisListener();
	private boolean mHasFromPNS;
	private UiLifecycleHelper uiHelper;
	private int checksharelikefb = 0;
	private DataDownloader mTNDownloader = getDataDownloader();

	private ProgressBar mProgressbar;
	private Typeface mDefaultTF;
	private TextView mTvCategory;
	private TextView mTvTitle;
	private RelativeLayout mRootLayout;
	private TextView mTVFbLikeCount;
	// Text zoom
	private int mTextZoomForPhone = 0;
	private int mTextZoomSize = 100; // the fixed value is 100 = 100% zoom size

    public static Activity mActivity;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		GKIMLog.lf(this, 1, TAG + "=>onSessionStateChange.Actity");
		GKIMLog.l(4, "onSessionStateChange");
		if (session != null && session.isOpened()) {
			if (checksharelikefb == 1) {
				checksharelikefb = 0;
				socialShare(1, 0);
			} else if (checksharelikefb == 2) {
				checksharelikefb = 0;
				socialLike();
			}
		}

	}

	@Override
	protected void onActivityResult(int reqCode, int resCode, Intent data) {
		GKIMLog.lf(this, 1, TAG + "=>onActivityResult :" + reqCode);
		uiHelper.onActivityResult(reqCode, resCode, data);
		if (TNPreferenceManager.REQ_CODE_USER_POST == reqCode
				&& TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO == resCode
				|| TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO_DETAIL_IMAGE == resCode) {
			setResult(resCode, data);
			mFinishData = null;
			StoryDetailSpringFragmentActivity.this.finish();
		} else {
			SocialShare provider = SocialHelper.getLastInstance()
					.getSNSInstance();
			if (provider != null) {
				if (!provider.handlingActivityForResult(reqCode, resCode, data)) {
					super.onActivityResult(reqCode, resCode, data);
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GKIMLog.lf(this, 1, TAG + "=>onCreate");
		setContentView(R.layout.activity_storydetail_springgreetings);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		mTabletVersion = UIUtils.isTablet(this);
		String params = "";
		if (!mTabletVersion) {
			params = RequestDataFactory.DEFAULT_TESTING_PARAM_DEVICE_PHONE;
			mTextZoomForPhone = 20;
		} else {
			params = RequestDataFactory.DEFAULT_TESTING_PARAM_DEVICE_TABLET;
			mTextZoomForPhone = 0;
		}

		if (getResources().getBoolean(R.bool.portrait_only)) {
			GKIMLog.lf(this, 0, TAG + "=>Not support for rotation");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		mSpringAdapter = new StoryDetailSpringAdapter(this, lstData);
		initLayout();
		mTNDownloader.addDownload(false,
				RequestDataFactory.makeGetSpringGreetings(params));

	}

	private void initLayout() {
		GKIMLog.l(1, TAG + " initLayout");
		mRootLayout = (RelativeLayout) findViewById(R.id.storydetail_relayout_root);
		mGuiHeader = (GUIHeader) findViewById(R.id.guiheader);

		mGuiFooter = (GUIStoryFooterSpring) findViewById(R.id.guifooter);

		mGuiMenu = (GUIListMenuListView) findViewById(R.id.guimenu);

		mListView = (PullToRefreshListView) findViewById(R.id.pager);

		mDefaultTF = TNPreferenceManager.getTNTypefaceBOLD();
		if (mTabletVersion) {
			mGuiFooter.setVisibility(View.GONE);
			mProgressbar = (ProgressBar) findViewById(R.id.pb_storydetail_processing);
			mTvCategory = (TextView) findViewById(R.id.tv_storydetail_category);
			mTvCategory.setOnClickListener(mOnClickListener);
			mTvTitle = (TextView) findViewById(R.id.tv_storydetail_title);

			ImageButton btn = (ImageButton) findViewById(R.id.imb_storyfooter_textsize);
			btn.setOnClickListener(getOnClickListener());
			btn = (ImageButton) findViewById(R.id.imb_storyfooter_share);
			btn.setOnClickListener(getOnClickListener());
			mTVFbLikeCount = (TextView) findViewById(R.id.tv_storydetail_fblike_count);
			mTVFbLikeCount.setOnClickListener(getOnClickListener());
			btn = (ImageButton) findViewById(R.id.imb_storyfooter_back);
			btn.setOnClickListener(getOnClickListener());

		} else {
			// Instantiate a listview.
			View mheaderView = LinearLayout.inflate(getApplicationContext(),
					R.layout.spring_header, null);
			mTvCategory = (TextView) mheaderView
					.findViewById(R.id.tv_storydetail_category);
			mTvCategory.setOnClickListener(mOnClickListener);
			mTvTitle = (TextView) mheaderView
					.findViewById(R.id.tv_storydetail_title);
			mTvTitle.setTypeface(mDefaultTF);
			if (mListView != null) {
				// when change from ListView to PullToRefreshListView,
				// can't addHeaderView
				// mListView.addHeaderView(mheaderView);
				mListView.getRefreshableView().addHeaderView(mheaderView);
			}
			mProgressbar = (ProgressBar) mheaderView
					.findViewById(R.id.pb_storydetail_processing);
		}
		mTvCategory.setTypeface(mDefaultTF);
		mTvTitle.setTypeface(mDefaultTF);

		// Can only set list adapter after addHeaderView
		mListView.setAdapter(mSpringAdapter);

		mOutAnimation2Left = AnimationUtils.loadAnimation(this,
				R.anim.push_left_out);
		mOutAnimation2Left.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mMenuShown = false;

			}
		});
		mInAnimationFromLeft = AnimationUtils.loadAnimation(this,
				R.anim.push_right_in);
		mOutAnimation2Right = AnimationUtils.loadAnimation(this,
				R.anim.push_right_out);
		mInAnimationFromRight = AnimationUtils.loadAnimation(this,
				R.anim.push_left_in);
		boolean bChecked = TNPreferenceManager.isNightMode();
		int idColorbg = R.color.storydetail_background_white;
		if (bChecked) {
			idColorbg = R.color.storydetail_background_black;
		}
		mRootLayout.setBackgroundColor(getResources().getColor(idColorbg));
		mListView.setBackgroundColor(getResources().getColor(idColorbg));
	}

	private void updateLayout() {
		if (mSpring != null) {
			mTvCategory.setText(mSpring.getSectiontitle());
			mTvTitle.setText(mSpring.getTitle());
			if (TNPreferenceManager.isNightMode()) {
				mTvTitle.setTextColor(Color.WHITE);
			} else {
                GKIMLog.l(1, " nam.nguyen:1111");
				mTvTitle.setTextColor(Color.RED); // Color.BLACK
			}
			mProgressbar.setVisibility(View.GONE);
			mSpringAdapter.notifyDataSetChanged();
			int fblikecount = mSpring.getFblike_count();
			getSharedPreferences(TNPreferenceManager.SHAREDPREF_SPRING,
					Context.MODE_PRIVATE)
					.edit()
					.putInt(TNPreferenceManager.EXTRAKEY_SPRING_WISH_LIKECOUNT,
							fblikecount).commit();
			updateFBLikeCount();
			updateGui();
		}
	}

	@Override
	protected void onStart() {
		GKIMLog.lf(this, 1, TAG + "=>onStart");
		super.onStart();
        mActivity = this;
	}

	@Override
	protected void onPause() {
		uiHelper.onPause();
		super.onPause();
        mActivity = this;
	}

	@Override
	protected void onResume() {
		GKIMLog.lf(this, 1, TAG + "=>onResume");
        mActivity = this;
		if (mFinishData != null) {
			mFinishData = null;
		}
		mFinishData = new Intent();

		if (mGuiHeader != null) {
			mGuiHeader.setOnClickListener(mOnClickListener);
		}
		if (mGuiFooter != null && !mTabletVersion) {
			mGuiFooter.setOnClickListener(mOnClickListener);
		}
		if (mGuiMenu != null) {
			mGuiMenu.setOnClickListener(mOnClickListener);
		}
		if (mListView != null) {
			mListView.setOnRefreshListener(mOnRefreshListener);
		}
		uiHelper.onResume();
		updateLayout();
		checkinPage();
		super.onResume();
	}

	@Override
	protected void onStop() {
		StoryDetailImageReviewActivity.mVideoFirst = false;
		if (mGuiHeader != null) {
			mGuiHeader.setOnClickListener(null);
		}
		if (mGuiFooter != null && !mTabletVersion) {
			mGuiFooter.setOnClickListener(null);
		}
		if (mGuiMenu != null) {
			hideGUIListMenu();
			mGuiMenu.setOnClickListener(null);
		}
		checkoutPage();
        mActivity = null;
		super.onStop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (mGuiMenu.getVisibility() == View.VISIBLE) {
			hideGUIListMenu();
		} else {
			super.onBackPressed();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		mOnRefreshListener = null;
		mListView = null;
        mActivity = null;
		uiHelper.onDestroy();
		super.onDestroy();
	}

	private DataDownloader getDataDownloader() {
		return (new DataDownloader(new OnDownloadCompletedListener() {

			@Override
			public void onCompleted(Object key, String result) {
				GKIMLog.lf(null, 0, TAG + "=>onCompleted: " + key.toString());
				if (result == null || result.length() <= 0) {
					UIUtils.showToast(
							null,
							StoryDetailSpringFragmentActivity.this
									.getResources()
									.getString(
											R.string.close_application_no_connection));
					return;
				}
				int type = ((RequestData) key).type;
				if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_SPRING_GREETINGS) {
					GKIMLog.lf(null, 0, TAG
							+ "=> process for get spring greetings.:" + result);
					Gson gson = new Gson();
					CSpring data = gson.fromJson(result, CSpring.class);
					if (data != null && data.getData() != null
							&& data.getData().size() > 0) {
						GKIMLog.l(1, TAG + " data.getData().size():"
								+ data.getData().size());
						mSpring = data;
						if (lstData != null && mListView != null) {
							lstData.clear();
							lstData.addAll(data.getData());
							mListView.onRefreshComplete();
							updateLayout();
						}
					}
				} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_COUNT_FB) {
					GKIMLog.l(4, TAG + "=> like count on spring : " + result);
					JsonParser jp = new JsonParser();
					JsonElement je = jp.parse(result);
					int likecount = 0;
					if (je != null && je.isJsonArray()) {
						JsonArray ja = je.getAsJsonArray();
						int length = ja.size();
						if (length > 0) {
							JsonElement jse = ja.get(0);
							likecount = jse.getAsJsonObject().get("like_count")
									.getAsInt();
						}
					} else if (je != null && je.isJsonObject()) {
						likecount = je.getAsJsonObject().get("like_count")
								.getAsInt();
					}
					updateFBLikeCount(likecount);
				}
			}

			@Override
			public String doInBackgroundDebug(Object... params) {
				return null;
			}
		}));
	}

	private View.OnClickListener getOnClickListener() {
		return (new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GKIMLog.lf(null, 1, TAG + "=>onClick: " + v);
				switch (v.getId()) {
				case R.id.header_iv_logo:
				case R.id.menu_list_header_ivhome:
					// backToSection(TNPreferenceManager.EXTRAVALUE_SECTION_HOME,
					// RESULT_OK);
					// break;
					backToSection(TNPreferenceManager.EXTRAVALUE_SECTION_HOME,
							TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO);
					break;
				case R.id.menu_in_list:
					if (v instanceof LinearLayout) {
						GUIListMenuAdapter.ViewHolder menuItem = (com.gkxim.android.thanhniennews.layout.GUIListMenuAdapter.ViewHolder) v
								.getTag();
						backToSection(
								menuItem.id,
								TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO);
					}
					break;

				case R.id.menu_list_header_ivmyhome:
					backToSection(
							TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE,
							TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO);
					break;
				case R.id.menu_list_header_ivstored:
					backToSection(
							TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED,
							TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO);
					break;
				case R.id.menu_list_header_ivsearch:
					backToSection(
							TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE,
							TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO);
					break;
				case R.id.imb_storyfooter_back:
					onBackPressed();
					break;
				case R.id.imb_storydetail_back:
					break;
				case R.id.imb_storyfooter_textsize:
					showTextModeDialog();
					break;
				case R.id.imb_storyfooter_fontsmaller:
					changeTextZoom(false);
					break;
				case R.id.imb_storyfooter_fontbigger:
					changeTextZoom(true);
					break;
				case R.id.tbtn_storyfooter_textmode:
					if (v instanceof ToggleButton) {
						boolean bChecked = ((ToggleButton) v).isChecked();
						GKIMLog.lf(null, 0, TAG + "=> toggle is checked: "
								+ bChecked);
						changeTextMode(bChecked);
					}
					break;
				case R.id.header_ib_startstory:
					showCommentDialog();
					break;
				case R.id.imb_storyfooter_share:
					showShareDialog();
					break;
				case R.id.imv_storydetail_dlg_shareby_comment:
					break;
				case R.id.imb_storyfooter_addcomment:
				case R.id.imv_storydetail_shareby_comment:
					break;
				case R.id.imv_storydetail_shareby_email:
					socialShare(0, 0);
					break;
				case R.id.imv_storydetail_shareby_facebook:
					checksharelikefb = 1;
					socialShare(1, 0);
					break;
				case R.id.imv_storydetail_shareby_twitter:
					socialShare(2, 1);
					break;
				case R.id.imv_menulist_bg:
					if (mMenuShown) {
						hideGUIListMenu();
					}
					return;
				case R.id.header_ib_menu:
					if (mMenuShown) {
						hideGUIListMenu();
					} else {
						showGUIListMenu();
					}
					return;
				case R.id.tv_storydetail_fblike_count:
					if (!mTabletVersion) {
						checksharelikefb = 2;
						socialLike();
					}
					break;
				case R.id.tv_storydetail_category:
					StoryDetailSpringFragmentActivity.this.finish();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * true = zoom in (bigger), else zoom out (smaller)
	 * 
	 * @param bZoomIn
	 */
	protected void changeTextZoom(boolean bZoomIn) {
		int TextZoomMax = TNPreferenceManager.WEBVIEW_TEXTZOOM_MAX
				+ mTextZoomForPhone;
		int TextZoomMin = TNPreferenceManager.WEBVIEW_TEXTZOOM_MIN
				- mTextZoomForPhone;

		if (bZoomIn) {
			mTextZoomSize += TNPreferenceManager.WEBVIEW_TEXTZOOM_STEP;
			if (mTextZoomSize > TextZoomMax) {
				mTextZoomSize = TextZoomMax;
			}
		} else {
			mTextZoomSize -= TNPreferenceManager.WEBVIEW_TEXTZOOM_STEP;
			if (mTextZoomSize < TextZoomMin) {
				mTextZoomSize = TextZoomMin;
			}
		}

		TNPreferenceManager.setTextSizeMode(mTextZoomSize);
		mSpringAdapter.notifyDataSetChanged();
	}

	protected void changeTextMode(boolean bChecked) {
		TNPreferenceManager.setTextMode(bChecked);
		int idColorbg = getResources().getColor(
				R.color.storydetail_background_white);
		if (bChecked) {
			idColorbg = getResources().getColor(
					R.color.storydetail_background_black);
			mTvTitle.setTextColor(Color.WHITE);
		} else {
			mTvTitle.setTextColor(Color.RED);
		}
		mRootLayout.setBackgroundColor(idColorbg);
		mListView.setBackgroundColor(idColorbg);
		mSpringAdapter.notifyDataSetChanged();
	}

	protected void showShareDialog() {
		if (mShareDialog == null) {
			mShareDialog = new GUIStoryShareDialogSpring(this);
			mShareDialog.setOnShareClickListener(mOnClickListener);
		}
		mShareDialog.show();
	}

	/**
	 * 
	 */
	protected void showTextModeDialog() {
		if (mTextModeDialog == null) {
			mTextModeDialog = new GUIStoryTextModeDialogSpring(this);
			mTextModeDialog.setOnShareClickListener(mOnClickListener);
		}
		mTextModeDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
			}
		});
		mTextModeDialog.setToggleChecked(TNPreferenceManager.isNightMode());
		mTextModeDialog.show();
	}

	protected void socialShare(int networkId, int isstatus) {
		if (mShareDialog != null && mShareDialog.isShowing()) {
			mShareDialog.dismiss();
		}

		// Collect story's information
		String[] data = new String[4];
		data[0] = mSpring.getSectiontitle();
		data[1] = mSpring.getTitle();
		data[2] = "";
		data[3] = mSpring.getWap_story_url();
		if (data != null && data.length >= 3) {
			// Progress SNS's strategy
			SocialHelper helper = SocialHelper.getInstance(this, networkId);
			// FIXME: add callback for listening result.
			helper.post(data, isstatus);
		}
	}

	private void showGUIListMenu() {
		if (mGuiMenu != null && mGuiMenu.getVisibility() != View.VISIBLE) {
			mGuiMenu.setVisibility(View.VISIBLE);
			mGuiMenu.startAnimation(mInAnimationFromLeft);
			if (!mTabletVersion) {
				mGuiFooter.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
				mListView.startAnimation(mOutAnimation2Right);
			}
			mMenuShown = true;
		}
	}

	protected void hideGUIListMenu() {
		if (mGuiMenu != null && mGuiMenu.getVisibility() == View.VISIBLE) {
			mGuiMenu.setVisibility(View.GONE);
			mGuiMenu.startAnimation(mOutAnimation2Left);
			if (!mTabletVersion) {
				mGuiFooter.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.VISIBLE);
				mListView.startAnimation(mInAnimationFromRight);
			}
			mMenuShown = false;

			InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (im != null && im.isActive()) {
				im.hideSoftInputFromWindow(mGuiMenu.getWindowToken(), 0);
			}
		}
	}

	protected void socialLike() {
		// Collect story's information
		String[] data = new String[4];
		GKIMLog.lf(this, 0, TAG + "=>socialLike: " + data.toString());
		data[0] = mSpring.getSectiontitle();
		data[1] = mSpring.getTitle();
		data[2] = "";
		data[3] = mSpring.getWap_story_url();
		if (data != null && data.length >= 4) {
			SocialHelper helper = SocialHelper.getInstance(this, 1);
			helper.like(data[3], data);
		}
	}

	private OnDismissListener getOnCommentDialogDissmisListener() {
		return (new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// GKIMLog.lf(null, 1,
				// TAG + "=>onDismiss : " + dialog + "(xuan story): "
				// + (mSpring != null? mSpring.getStoryid():"<>"));
				// if (mDataDownloader != null) {
				// mDataDownloader.setExitTasksEarly(true);
				// mDataDownloader = null;
				// mDataDownloader = getDataDownloader();
				// }
				// mDataDownloader
				// .addDownload(RequestDataFactory.makeStoryCommentsRequest(
				// TNPreferenceManager.getUserId(),
				// (mSpring != null? mSpring.getStoryid()
				// :TNPreferenceManager.EXTRAVALUE_SECTION_SPRING_WISHES_STORY)));
			}
		});
	}

	public void updateFBLikeCount(int likecount) {
		getSharedPreferences(TNPreferenceManager.SHAREDPREF_SPRING,
				Context.MODE_PRIVATE)
				.edit()
				.putInt(TNPreferenceManager.EXTRAKEY_SPRING_WISH_LIKECOUNT,
						likecount).commit();
		if (mTabletVersion) {
			mTVFbLikeCount.setText(String.valueOf(likecount));
		} else {
			mGuiFooter.setFbLikeCountView(likecount);
		}

	}

	public void updateFBLikeCount() {
		int likecount = getSharedPreferences(
				TNPreferenceManager.SHAREDPREF_SPRING, Context.MODE_PRIVATE)
				.getInt(TNPreferenceManager.EXTRAKEY_SPRING_WISH_LIKECOUNT,
						mSpring.getFblike_count());
		if (mTabletVersion) {
			mTVFbLikeCount.setText(String.valueOf(likecount));
		} else {
			mGuiFooter.setFbLikeCountView(likecount);
		}
	}

	public void updateGui() {
		boolean ischeck = this.getSharedPreferences(
				TNPreferenceManager.SHAREDPREF_SPRING, Context.MODE_PRIVATE)
				.getBoolean(TNPreferenceManager.EXTRAKEY_SPRING_WISH_LIKE,
						false);
		if (ischeck) {
			if (mTabletVersion) {
				if (mTVFbLikeCount != null) {
					mTVFbLikeCount
							.setBackgroundResource(R.drawable.ic_storydetail_fb_like_over);
				}
			} else {
				mGuiFooter.setFbLiked(ischeck);
			}
		} else {
			if (mTabletVersion) {
				if (mTVFbLikeCount != null) {
					mTVFbLikeCount
							.setBackgroundResource(R.drawable.ic_storydetail_fb_like);
				}
			} else {
				mGuiFooter.setFbLiked(ischeck);
			}
		}
	}

	private void checkoutPage() {
		saveStandingInPage(TNPreferenceManager.getXuanWishesStoryId(), false);
	}

	private void checkinPage() {
		saveStandingInPage(TNPreferenceManager.getXuanWishesStoryId(), true);
	}

	private void saveStandingInPage(String pKey, boolean bValue) {
		getSharedPreferences(TNPreferenceManager.SHAREDPREF_SPRING,
				Context.MODE_PRIVATE).edit().putBoolean(pKey, bValue).commit();
	}

	protected void showCommentDialog() {
		String storyId = TNPreferenceManager.getXuanWishesStoryId();
		String storyTitle = TNPreferenceManager
				.getSectionTitleFromPref(TNPreferenceManager.getXuanSectionId());
		if (!TextUtils.isEmpty(mSpring.getStoryid())) {
			storyId = mSpring.getStoryid();
		}
		if (mCommentDialog != null) {
			mCommentDialog = null;
		}
		mCommentDialog = new GUIStoryCommentDialog(this,
				R.layout.dlg_storycomment_spring_wish_compose);
		mCommentDialog.setStoryId(storyId);
		mCommentDialog.setStoryTitle(storyTitle);
		mCommentDialog.show();
		mCommentDialog.setOnDismissListener(mOnDialogDismissListener);
	}

	private OnRefreshListener getOnRefreshListener() {
		return (new OnRefreshListener<ListView>() {
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				GKIMLog.lf(StoryDetailSpringFragmentActivity.this, 1, TAG
						+ "=>onRefresh");
				if (mTNDownloader != null) {
					mTNDownloader.setExitTasksEarly(true);
					mTNDownloader = null;
					mTNDownloader = getDataDownloader();
				}
				mTabletVersion = UIUtils
						.isTablet(StoryDetailSpringFragmentActivity.this);
				String params = "";
				if (!mTabletVersion) {
					params = RequestDataFactory.DEFAULT_TESTING_PARAM_DEVICE_PHONE;
					mTextZoomForPhone = 20;
				} else {
					params = RequestDataFactory.DEFAULT_TESTING_PARAM_DEVICE_TABLET;
					mTextZoomForPhone = 0;
				}
				mTNDownloader.addDownload(false,
						RequestDataFactory.makeGetSpringGreetings(params));
			}
		});
	}

	private void backToSection(String sectionId, int resultCode) {
		mFinishData.putExtra(TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK, true);
		mFinishData.putExtra(TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
				sectionId);
		setResult(resultCode, mFinishData);
		finish();
	}
}
